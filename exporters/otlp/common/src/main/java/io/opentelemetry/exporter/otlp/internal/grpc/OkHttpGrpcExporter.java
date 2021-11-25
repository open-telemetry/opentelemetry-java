/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

/*
 * Copyright 2014 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A {@link GrpcExporter} which uses OkHttp instead of grpc-java.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpGrpcExporter<T extends Marshaler> implements GrpcExporter<T> {

  private static final String GRPC_STATUS = "grpc-status";
  private static final String GRPC_MESSAGE = "grpc-message";

  private static final String GRPC_STATUS_CANCELLED = "1";
  private static final String GRPC_STATUS_DEADLINE_EXCEEDED = "4";
  private static final String GRPC_STATUS_RESOURCE_EXHAUSTED = "8";
  private static final String GRPC_STATUS_ABORTED = "10";
  private static final String GRPC_STATUS_OUT_OF_RANGE = "11";
  private static final String GRPC_STATUS_UNIMPLEMENTED = "12";
  private static final String GRPC_STATUS_UNAVAILABLE = "14";
  private static final String GRPC_STATUS_DATA_LOSS = "15";

  private static final Set<String> RETRIABLE_GRPC_CODES = new HashSet<>();

  static {
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_CANCELLED);
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_DEADLINE_EXCEEDED);
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_RESOURCE_EXHAUSTED);
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_ABORTED);
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_OUT_OF_RANGE);
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_UNAVAILABLE);
    RETRIABLE_GRPC_CODES.add(GRPC_STATUS_DATA_LOSS);
  }

  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(OkHttpGrpcExporter.class.getName()));

  private final String type;
  private final OkHttpClient client;
  private final String endpoint;
  private final Headers headers;
  private final boolean compressionEnabled;

  private final LongCounter seen;
  private final LongCounter exported;

  private final Attributes seenAttrs;
  private final Attributes successAttrs;
  private final Attributes failedAttrs;

  /** Creates a new {@link OkHttpGrpcExporter}. */
  OkHttpGrpcExporter(
      String type,
      OkHttpClient client,
      MeterProvider meterProvider,
      String endpoint,
      Headers headers,
      boolean compressionEnabled) {
    this.type = type;
    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;

    Meter meter = meterProvider.get("io.opentelemetry.exporters.otlp-grpc-okhttp");
    seenAttrs = Attributes.builder().put("type", type).build();
    seen = meter.counterBuilder("otlp.exporter.seen").build();

    exported = meter.counterBuilder("otlp.exported.exported").build();
    successAttrs = seenAttrs.toBuilder().put("success", true).build();
    failedAttrs = seenAttrs.toBuilder().put("success", false).build();
  }

  @Override
  public CompletableResultCode export(T exportRequest, int numItems) {
    seen.add(numItems, seenAttrs);

    Request.Builder requestBuilder = new Request.Builder().url(endpoint).headers(headers);

    RequestBody requestBody = new GrpcRequestBody(exportRequest, compressionEnabled);
    requestBuilder.post(requestBody);

    CompletableResultCode result = new CompletableResultCode();

    client
        .newCall(requestBuilder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                exported.add(numItems, failedAttrs);
                logger.log(
                    Level.SEVERE,
                    "Failed to export "
                        + type
                        + "s. The request could not be executed. Full error message: "
                        + e.getMessage());
                result.fail();
              }

              @Override
              public void onResponse(Call call, Response response) {
                // Response body is empty but must be consumed to access trailers.
                try {
                  response.body().bytes();
                } catch (IOException e) {
                  logger.log(
                      Level.WARNING,
                      "Failed to export " + type + "s, could not consume server response.",
                      e);
                  exported.add(numItems, failedAttrs);
                  result.fail();
                  return;
                }

                String status = grpcStatus(response);
                if ("0".equals(status)) {
                  exported.add(numItems, successAttrs);
                  result.succeed();
                  return;
                }

                exported.add(numItems, failedAttrs);

                String codeMessage =
                    status != null
                        ? "gRPC status code " + status
                        : "HTTP status code " + response.code();
                String errorMessage = grpcMessage(response);

                if (GRPC_STATUS_UNIMPLEMENTED.equals(status)) {
                  logger.log(
                      Level.SEVERE,
                      "Failed to export "
                          + type
                          + "s. Server responded with UNIMPLEMENTED. "
                          + "This usually means that your collector is not configured with an otlp "
                          + "receiver in the \"pipelines\" section of the configuration. "
                          + "Full error message: "
                          + errorMessage);
                } else if (GRPC_STATUS_UNAVAILABLE.equals(status)) {
                  logger.log(
                      Level.SEVERE,
                      "Failed to export "
                          + type
                          + "s. Server is UNAVAILABLE. "
                          + "Make sure your collector is running and reachable from this network. "
                          + "Full error message:"
                          + errorMessage);
                } else {
                  logger.log(
                      Level.WARNING,
                      "Failed to export "
                          + type
                          + "s. Server responded with "
                          + codeMessage
                          + ". Error message: "
                          + errorMessage);
                }
                result.fail();
              }
            });

    return result;
  }

  @Nullable
  private static String grpcStatus(Response response) {
    // Status can either be in the headers or trailers depending on error
    String grpcStatus = response.header(GRPC_STATUS);
    if (grpcStatus == null) {
      try {
        grpcStatus = response.trailers().get(GRPC_STATUS);
      } catch (IOException e) {
        // Could not read a status, this generally means the HTTP status is the error.
        return null;
      }
    }
    return grpcStatus;
  }

  private static String grpcMessage(Response response) {
    String message = response.header(GRPC_MESSAGE);
    if (message == null) {
      try {
        message = response.trailers().get(GRPC_MESSAGE);
      } catch (IOException e) {
        // Fall through
      }
    }
    if (message != null) {
      return unescape(message);
    }
    // Couldn't get message for some reason, use the HTTP status.
    return response.message();
  }

  @Override
  public CompletableResultCode shutdown() {
    client.dispatcher().cancelAll();
    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
    return CompletableResultCode.ofSuccess();
  }

  static boolean isRetriable(Response response) {
    // Only retry on gRPC codes which will always come with an HTTP success
    if (!response.isSuccessful()) {
      return false;
    }

    // We don't check trailers for retry since retryable error codes always come with response
    // headers, not trailers, in practice.
    String grpcStatus = response.header(GRPC_STATUS);
    return RETRIABLE_GRPC_CODES.contains(grpcStatus);
  }

  // From grpc-java

  /** Unescape the provided ascii to a unicode {@link String}. */
  private static String unescape(String value) {
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      if (c < ' ' || c >= '~' || (c == '%' && i + 2 < value.length())) {
        return doUnescape(value.getBytes(StandardCharsets.US_ASCII));
      }
    }
    return value;
  }

  private static String doUnescape(byte[] value) {
    final ByteBuffer buf = ByteBuffer.allocate(value.length);
    for (int i = 0; i < value.length; ) {
      if (value[i] == '%' && i + 2 < value.length) {
        try {
          buf.put((byte) Integer.parseInt(new String(value, i + 1, 2, StandardCharsets.UTF_8), 16));
          i += 3;
          continue;
        } catch (NumberFormatException e) {
          // ignore, fall through, just push the bytes.
        }
      }
      buf.put(value[i]);
      i += 1;
    }
    return new String(buf.array(), 0, buf.position(), StandardCharsets.UTF_8);
  }
}
