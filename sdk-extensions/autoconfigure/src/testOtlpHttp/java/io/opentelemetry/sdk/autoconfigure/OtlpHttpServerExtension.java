/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.protobuf.Message;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.internal.common.util.SelfSignedCertificate;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;

class OtlpHttpServerExtension extends ServerExtension {

  final SelfSignedCertificate selfSignedCertificate;

  final Queue<ExportTraceServiceRequest> traceRequests = new ArrayDeque<>();
  final Queue<ExportMetricsServiceRequest> metricRequests = new ArrayDeque<>();
  final Queue<ExportLogsServiceRequest> logRequests = new ArrayDeque<>();
  final Queue<HttpResponse> responses = new ArrayDeque<>();
  final Queue<RequestHeaders> requestHeaders = new ArrayDeque<>();

  OtlpHttpServerExtension() {
    try {
      selfSignedCertificate = new SelfSignedCertificate();
    } catch (CertificateException e) {
      throw new IllegalStateException("Unable to setup certificate.", e);
    }
  }

  @Override
  protected void configure(ServerBuilder sb) {
    sb.service(
            "/v1/traces",
            httpService(traceRequests, ExportTraceServiceRequest.getDefaultInstance()))
        .service(
            "/v1/metrics",
            httpService(metricRequests, ExportMetricsServiceRequest.getDefaultInstance()))
        .service(
            "/v1/logs", httpService(logRequests, ExportLogsServiceRequest.getDefaultInstance()));
    sb.tls(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey());
  }

  @SuppressWarnings("unchecked")
  private <T extends Message> HttpService httpService(Queue<T> queue, T defaultMessage) {
    return (ctx, req) ->
        HttpResponse.from(
            req.aggregate()
                .thenApply(
                    aggReq -> {
                      requestHeaders.add(aggReq.headers());
                      try {
                        byte[] requestBody =
                            maybeGzipInflate(aggReq.headers(), aggReq.content().array());
                        queue.add((T) defaultMessage.getParserForType().parseFrom(requestBody));
                      } catch (IOException e) {
                        return HttpResponse.of(HttpStatus.BAD_REQUEST);
                      }
                      return responses.peek() != null
                          ? responses.poll()
                          : HttpResponse.of(HttpStatus.OK);
                    }));
  }

  private static byte[] maybeGzipInflate(RequestHeaders requestHeaders, byte[] content)
      throws IOException {
    if (!requestHeaders.contains("content-encoding", "gzip")) {
      return content;
    }
    Buffer buffer = new Buffer();
    GzipSource gzipSource = new GzipSource(Okio.source(new ByteArrayInputStream(content)));
    gzipSource.read(buffer, Integer.MAX_VALUE);
    return buffer.readByteArray();
  }

  void reset() {
    traceRequests.clear();
    metricRequests.clear();
    logRequests.clear();
    requestHeaders.clear();
    responses.clear();
  }

  static SpanData generateFakeSpan() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setName("name")
        .setStartEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
        .setEndEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
        .setKind(SpanKind.SERVER)
        .setStatus(StatusData.error())
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0)
        .build();
  }

  static MetricData generateFakeMetric() {
    return MetricData.createLongSum(
        Resource.empty(),
        InstrumentationScopeInfo.empty(),
        "metric_name",
        "metric_description",
        "ms",
        ImmutableSumData.create(
            false,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                LongPointData.create(
                    MILLISECONDS.toNanos(System.currentTimeMillis()),
                    MILLISECONDS.toNanos(System.currentTimeMillis()),
                    Attributes.of(stringKey("key"), "value"),
                    10))));
  }

  static LogData generateFakeLog() {
    return LogDataBuilder.create(Resource.empty(), InstrumentationScopeInfo.empty())
        .setEpoch(Instant.now())
        .setBody("log body")
        .build();
  }
}
