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

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.api.internal.InstrumentationUtil;
import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.Compressor;
import io.opentelemetry.sdk.common.export.GrpcResponse;
import io.opentelemetry.sdk.common.export.GrpcSender;
import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A {@link GrpcSender} which uses OkHttp instead of grpc-java.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpGrpcSender implements GrpcSender {

  private static final Logger logger = Logger.getLogger(OkHttpGrpcSender.class.getName());

  private static final String GRPC_STATUS = "grpc-status";
  private static final String GRPC_MESSAGE = "grpc-message";

  private final boolean managedExecutor;
  private final OkHttpClient client;
  private final HttpUrl url;
  @Nullable private final Compressor compressor;
  private final Supplier<Map<String, List<String>>> headersSupplier;

  /** Creates a new {@link OkHttpGrpcSender}. */
  @SuppressWarnings("TooManyParameters")
  public OkHttpGrpcSender(
      String endpoint,
      @Nullable Compressor compressor,
      Duration timeout,
      Duration connectTimeout,
      Supplier<Map<String, List<String>>> headersSupplier,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService) {
    int callTimeoutMillis = (int) Math.min(timeout.toMillis(), Integer.MAX_VALUE);
    int connectTimeoutMillis = (int) Math.min(connectTimeout.toMillis(), Integer.MAX_VALUE);

    Dispatcher dispatcher;
    if (executorService == null) {
      dispatcher = OkHttpUtil.newDispatcher();
      this.managedExecutor = true;
    } else {
      dispatcher = new Dispatcher(executorService);
      this.managedExecutor = false;
    }

    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .callTimeout(Duration.ofMillis(callTimeoutMillis))
            .connectTimeout(Duration.ofMillis(connectTimeoutMillis));
    if (retryPolicy != null) {
      clientBuilder.addInterceptor(
          new RetryInterceptor(retryPolicy, OkHttpGrpcSender::isRetryable));
    }

    boolean isPlainHttp = endpoint.startsWith("http://");
    if (isPlainHttp) {
      clientBuilder.connectionSpecs(Collections.singletonList(ConnectionSpec.CLEARTEXT));
      clientBuilder.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE));
    } else {
      clientBuilder.protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1));
      if (sslContext != null && trustManager != null) {
        clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
      }
    }

    this.client = clientBuilder.build();
    this.compressor = compressor;
    this.headersSupplier = headersSupplier;
    this.url = HttpUrl.get(endpoint);
  }

  @Override
  public void send(
      MessageWriter messageWriter, Consumer<GrpcResponse> onResponse, Consumer<Throwable> onError) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    Map<String, List<String>> headers = headersSupplier.get();
    if (headers != null) {
      headers.forEach(
          (key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value)));
    }
    requestBuilder.addHeader("te", "trailers");
    if (compressor != null) {
      requestBuilder.addHeader("grpc-encoding", compressor.getEncoding());
    }
    RequestBody requestBody = new GrpcRequestBody(messageWriter, compressor);
    requestBuilder.post(requestBody);

    InstrumentationUtil.suppressInstrumentation(
        () ->
            client
                .newCall(requestBuilder.build())
                .enqueue(
                    new Callback() {
                      @Override
                      public void onFailure(Call call, IOException e) {
                        onError.accept(e);
                      }

                      @Override
                      public void onResponse(Call call, Response response) {
                        try (ResponseBody body = response.body()) {
                          // Must consume body before accessing trailers
                          byte[] bodyBytes = null;
                          try {
                            bodyBytes = body.bytes();
                          } catch (IOException e) {
                            bodyBytes = new byte[0];
                            logger.log(Level.WARNING, "Failed to read response body", e);
                          }
                          byte[] resolvedBodyBytes = bodyBytes;
                          GrpcStatusCode status = grpcStatus(response);
                          String description = grpcMessage(response);
                          onResponse.accept(
                              new GrpcResponse() {
                                @Override
                                public GrpcStatusCode getStatusCode() {
                                  return status;
                                }

                                @Override
                                public String getStatusDescription() {
                                  return description;
                                }

                                @Override
                                public byte[] getResponseMessage() {
                                  return resolvedBodyBytes;
                                }
                              });
                        }
                      }
                    }));
  }

  private static GrpcStatusCode grpcStatus(Response response) {
    // Status can either be in the headers or trailers depending on error
    String grpcStatus = response.header(GRPC_STATUS);
    if (grpcStatus == null) {
      try {
        grpcStatus = response.trailers().get(GRPC_STATUS);
      } catch (IOException e) {
        // Could not read a status, this generally means the HTTP status is the error.
        return GrpcStatusCode.UNKNOWN;
      }
    }
    if (grpcStatus == null) {
      return GrpcStatusCode.UNKNOWN;
    }
    try {
      return GrpcStatusCode.fromValue(Integer.parseInt(grpcStatus));
    } catch (NumberFormatException ex) {
      return GrpcStatusCode.UNKNOWN;
    }
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
    client.connectionPool().evictAll();

    if (managedExecutor) {
      ExecutorService executorService = client.dispatcher().executorService();
      // Use shutdownNow() to interrupt idle threads immediately since we've cancelled all work
      executorService.shutdownNow();

      // Wait for threads to terminate in a background thread
      CompletableResultCode result = new CompletableResultCode();
      Thread terminationThread =
          new Thread(
              () -> {
                try {
                  // Wait up to 5 seconds for threads to terminate
                  // Even if timeout occurs, we succeed since these are daemon threads
                  boolean terminated = executorService.awaitTermination(5, TimeUnit.SECONDS);
                  if (!terminated) {
                    logger.log(
                        Level.WARNING,
                        "Executor did not terminate within 5 seconds, proceeding with shutdown since threads are daemon threads.");
                  }
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                } finally {
                  result.succeed();
                }
              },
              "okhttp-shutdown");
      terminationThread.setDaemon(true);
      terminationThread.start();
      return result;
    }

    return CompletableResultCode.ofSuccess();
  }

  /** Whether response is retriable or not. */
  public static boolean isRetryable(Response response) {
    // We don't check trailers for retry since retryable error codes always come with response
    // headers, not trailers, in practice.
    String grpcStatus = response.header(GRPC_STATUS);
    if (grpcStatus == null) {
      return false;
    }
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
