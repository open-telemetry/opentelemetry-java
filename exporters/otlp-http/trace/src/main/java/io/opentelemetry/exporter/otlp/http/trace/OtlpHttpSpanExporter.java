/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.internal.ProtoRequestBody;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcStatusUtil;
import io.opentelemetry.exporter.otlp.internal.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/** Exports spans using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpSpanExporter implements SpanExporter {

  private static final Logger internalLogger =
      Logger.getLogger(OtlpHttpSpanExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final BoundLongCounter seen;
  private final BoundLongCounter success;
  private final BoundLongCounter failed;

  private final OkHttpClient client;
  private final String endpoint;
  @Nullable private final Headers headers;
  private final boolean compressionEnabled;

  OtlpHttpSpanExporter(
      OkHttpClient client, String endpoint, @Nullable Headers headers, boolean compressionEnabled) {
    Meter meter = GlobalMeterProvider.get().get("io.opentelemetry.exporters.otlp-http");
    Attributes attributes = Attributes.builder().put(stringKey("type"), "span").build();
    seen = meter.counterBuilder("otlp.exporter.seen").build().bind(attributes);
    LongCounter exported = meter.counterBuilder("otlp.exported.exported").build();
    success = exported.bind(attributes.toBuilder().put(booleanKey("success"), true).build());
    failed = exported.bind(attributes.toBuilder().put(booleanKey("success"), false).build());

    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    seen.add(spans.size());

    TraceRequestMarshaler exportRequest = TraceRequestMarshaler.create(spans);

    Request.Builder requestBuilder = new Request.Builder().url(endpoint);
    if (headers != null) {
      requestBuilder.headers(headers);
    }
    RequestBody requestBody = new ProtoRequestBody(exportRequest);
    if (compressionEnabled) {
      requestBuilder.addHeader("Content-Encoding", "gzip");
      requestBuilder.post(gzipRequestBody(requestBody));
    } else {
      requestBuilder.post(requestBody);
    }

    CompletableResultCode result = new CompletableResultCode();

    client
        .newCall(requestBuilder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                failed.add(spans.size());
                logger.log(
                    Level.SEVERE,
                    "Failed to export spans. The request could not be executed. Full error message: "
                        + e.getMessage());
                result.fail();
              }

              @Override
              public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                  success.add(spans.size());
                  result.succeed();
                  return;
                }

                failed.add(spans.size());
                int code = response.code();

                String status = extractErrorStatus(response);

                logger.log(
                    Level.WARNING,
                    "Failed to export spans. Server responded with HTTP status code "
                        + code
                        + ". Error message: "
                        + status);
                result.fail();
              }
            });

    return result;
  }

  private static RequestBody gzipRequestBody(RequestBody requestBody) {
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return requestBody.contentType();
      }

      @Override
      public long contentLength() {
        return -1;
      }

      @Override
      public void writeTo(BufferedSink bufferedSink) throws IOException {
        BufferedSink gzipSink = Okio.buffer(new GzipSink(bufferedSink));
        requestBody.writeTo(gzipSink);
        gzipSink.close();
      }
    };
  }

  private static String extractErrorStatus(Response response) {
    ResponseBody responseBody = response.body();
    if (responseBody == null) {
      return "Response body missing, HTTP status message: " + response.message();
    }
    try {
      return GrpcStatusUtil.getStatusMessage(responseBody.bytes());
    } catch (IOException e) {
      return "Unable to parse response body, HTTP status message: " + response.message();
    }
  }

  /**
   * The OTLP exporter does not batch spans, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpSpanExporterBuilder builder() {
    return new OtlpHttpSpanExporterBuilder();
  }

  /**
   * Returns a new {@link OtlpHttpSpanExporter} using the default values.
   *
   * @return a new {@link OtlpHttpSpanExporter} instance.
   */
  public static OtlpHttpSpanExporter getDefault() {
    return builder().build();
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    client.dispatcher().cancelAll();
    this.seen.unbind();
    this.success.unbind();
    this.failed.unbind();
    return result;
  }
}
