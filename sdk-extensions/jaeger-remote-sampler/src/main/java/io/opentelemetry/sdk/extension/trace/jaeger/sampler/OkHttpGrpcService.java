/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcRequestBody;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcStatusUtil;
import io.opentelemetry.exporter.otlp.internal.retry.RetryUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;

final class OkHttpGrpcService<ReqT extends Marshaler, ResT extends UnMarshaller>
    implements GrpcService<ReqT, ResT> {

  private static final String GRPC_STATUS = "grpc-status";
  private static final String GRPC_MESSAGE = "grpc-message";

  private static final Logger logger = Logger.getLogger(OkHttpGrpcService.class.getName());

  private final String type;
  private final OkHttpClient client;
  private final String endpoint;
  private final Headers headers;
  private final boolean compressionEnabled;

  /** Creates a new {@link OkHttpGrpcService}. */
  OkHttpGrpcService(
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
  }

  @Override
  public ResT execute(ReqT exportRequest, ResT responseUnmarshaller) {
    Request.Builder requestBuilder = new Request.Builder().url(endpoint).headers(headers);

    RequestBody requestBody = new GrpcRequestBody(exportRequest, compressionEnabled);
    requestBuilder.post(requestBody);

    try {
      Response response = client.newCall(requestBuilder.build()).execute();

      try {
        InputStream inputStream = response.body().byteStream();
        byte[] arrCompressionAndLength = new byte[5];
        int bytesRead = inputStream.read(arrCompressionAndLength, 0, 5);
        if (bytesRead < 5) {
          return responseUnmarshaller;
        }

        if (arrCompressionAndLength[0] == '1') {
          Buffer buffer = new Buffer();
          buffer.readFrom(inputStream);
          GzipSource gzipSource = new GzipSource(buffer);
          inputStream = Okio.buffer(gzipSource).inputStream();
        } // else do nothing data are not compressed
        responseUnmarshaller.read(inputStream);
      } catch (IOException e) {
        logger.log(
            Level.WARNING,
            "Failed to execute " + type + "s, could not consume server response.",
            e);
        return responseUnmarshaller;
      }

      String status = grpcStatus(response);
      if ("0".equals(status)) {
        return responseUnmarshaller;
      }

      String codeMessage =
          status != null ? "gRPC status code " + status : "HTTP status code " + response.code();
      String errorMessage = grpcMessage(response);

      if (GrpcStatusUtil.GRPC_STATUS_UNIMPLEMENTED.equals(status)) {
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server responded with UNIMPLEMENTED. "
                + "Full error message: "
                + errorMessage);
      } else if (GrpcStatusUtil.GRPC_STATUS_UNAVAILABLE.equals(status)) {
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your service is running and reachable from this network. "
                + "Full error message:"
                + errorMessage);
      } else {
        logger.log(
            Level.WARNING,
            "Failed to execute "
                + type
                + "s. Server responded with "
                + codeMessage
                + ". Error message: "
                + errorMessage);
      }
    } catch (IOException e) {
      logger.log(
          Level.SEVERE,
          "Failed to execute "
              + type
              + "s. The request could not be executed. Full error message: "
              + e.getMessage());
    }

    return responseUnmarshaller;
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

  static boolean isRetryable(Response response) {
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
