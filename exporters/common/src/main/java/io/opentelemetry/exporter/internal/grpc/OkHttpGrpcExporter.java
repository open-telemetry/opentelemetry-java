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

package io.opentelemetry.exporter.internal.grpc;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
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

  private static final Logger internalLogger = Logger.getLogger(OkHttpGrpcExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  // We only log unimplemented once since it's a configuration issue that won't be recovered.
  private final AtomicBoolean loggedUnimplemented = new AtomicBoolean();
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final String type;
  private final ExporterMetrics exporterMetrics;
  private final OkHttpClient client;
  private final HttpUrl url;
  private final Headers headers;
  private final boolean compressionEnabled;

  /** Creates a new {@link OkHttpGrpcExporter}. */
  OkHttpGrpcExporter(
      String exporterName,
      String type,
      OkHttpClient client,
      Supplier<MeterProvider> meterProviderSupplier,
      String endpoint,
      Headers headers,
      boolean compressionEnabled) {
    this.type = type;
    this.exporterMetrics =
        ExporterMetrics.createGrpcOkHttp(exporterName, type, meterProviderSupplier);
    this.client = client;
    this.url = HttpUrl.get(endpoint);
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;
  }

  @Override
  public CompletableResultCode export(T exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    exporterMetrics.addSeen(numItems);

    Request.Builder requestBuilder = new Request.Builder().url(url).headers(headers);

    RequestBody requestBody = new GrpcRequestBody(exportRequest, compressionEnabled);
    requestBuilder.post(requestBody);

    CompletableResultCode result = new CompletableResultCode();

    client
        .newCall(requestBuilder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                exporterMetrics.addFailed(numItems);
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
                  exporterMetrics.addFailed(numItems);
                  result.fail();
                  return;
                }

                String status = grpcStatus(response);
                if ("0".equals(status)) {
                  exporterMetrics.addSuccess(numItems);
                  result.succeed();
                  return;
                }

                exporterMetrics.addFailed(numItems);

                String codeMessage =
                    status != null
                        ? "gRPC status code " + status
                        : "HTTP status code " + response.code();
                String errorMessage = grpcMessage(response);

                if (GrpcStatusUtil.GRPC_STATUS_UNIMPLEMENTED.equals(status)) {
                  if (loggedUnimplemented.compareAndSet(false, true)) {
                    GrpcExporterUtil.logUnimplemented(internalLogger, type, errorMessage);
                  }
                } else if (GrpcStatusUtil.GRPC_STATUS_UNAVAILABLE.equals(status)) {
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
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    client.dispatcher().cancelAll();
    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
    return CompletableResultCode.ofSuccess();
  }

  /** Whether response is retriable or not. */
  public static boolean isRetryable(Response response) {
    // Only retry on gRPC codes which will always come with an HTTP success
    if (!response.isSuccessful()) {
      return false;
    }

    // We don't check trailers for retry since retryable error codes always come with response
    // headers, not trailers, in practice.
    String grpcStatus = response.header(GRPC_STATUS);
    return RetryUtil.retryableGrpcStatusCodes().contains(grpcStatus);
  }

  // From grpc-java

  /** Unescape the provided ascii to a unicode {@link String}. */
  private static String unescape(String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c < ' ' || c >= '~' || (c == '%' && i + 2 < value.length())) {
        return doUnescape(value.getBytes(StandardCharsets.US_ASCII));
      }
    }
    return value;
  }

  private static String doUnescape(byte[] value) {
    ByteBuffer buf = ByteBuffer.allocate(value.length);
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
