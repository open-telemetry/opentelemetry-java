/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.grpc.GrpcStatusCode;
import io.opentelemetry.exporter.sender.okhttp.internal.GrpcRequestBody;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;

final class OkHttpGrpcService implements GrpcService {

  private static final String GRPC_STATUS = "grpc-status";
  private static final String GRPC_MESSAGE = "grpc-message";

  private static final Logger logger = Logger.getLogger(OkHttpGrpcService.class.getName());

  private final String type;
  private final OkHttpClient client;
  private final HttpUrl url;
  private final Headers headers;

  /** Creates a new {@link OkHttpGrpcService}. */
  OkHttpGrpcService(String type, OkHttpClient client, String endpoint, Headers headers) {
    this.type = type;
    this.client = client;
    this.url = HttpUrl.get(endpoint);
    this.headers = headers;
  }

  @Override
  public SamplingStrategyResponseUnMarshaler execute(
      SamplingStrategyParametersMarshaler exportRequest,
      SamplingStrategyResponseUnMarshaler responseUnmarshaller) {
    Request.Builder requestBuilder = new Request.Builder().url(url).headers(headers);

    RequestBody requestBody = new GrpcRequestBody(exportRequest.toBinaryMessageWriter(), null);
    requestBuilder.post(requestBody);

    try {
      Response response = client.newCall(requestBuilder.build()).execute();

      byte[] bodyBytes = new byte[0];
      try {
        bodyBytes = response.body().bytes();
      } catch (IOException ignored) {
        // It's unlikely a transport exception would actually be useful in debugging. There may
        // be gRPC status information available handled below though, so ignore this exception
        // and continue through gRPC error handling logic. In the worst case we will record the
        // HTTP error.
      }

      GrpcStatusCode status = grpcStatus(response);
      if (GrpcStatusCode.OK == status) {
        if (bodyBytes.length > 5) {
          ByteArrayInputStream bodyStream = new ByteArrayInputStream(bodyBytes);
          bodyStream.skip(5);
          if (bodyBytes[0] == '1') {
            Buffer buffer = new Buffer();
            buffer.readFrom(bodyStream);
            GzipSource gzipSource = new GzipSource(buffer);
            bodyBytes = Okio.buffer(gzipSource).getBuffer().readByteArray();
          } else {
            bodyBytes = Arrays.copyOfRange(bodyBytes, 5, bodyBytes.length);
          }
          responseUnmarshaller.read(bodyBytes);
          return responseUnmarshaller;
        }
        return responseUnmarshaller;
      }

      // handle non OK status codes
      String errorMessage = grpcMessage(response);
      if (GrpcStatusCode.UNIMPLEMENTED == status) {
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server responded with UNIMPLEMENTED. "
                + "Full error message: "
                + errorMessage);
      } else if (GrpcStatusCode.UNAVAILABLE == status) {
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your service is running and reachable from this network. "
                + "Full error message:"
                + errorMessage);
      } else {
        String codeMessage =
            status != null ? "gRPC status code " + status : "HTTP status code " + response.code();
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
  private static GrpcStatusCode grpcStatus(Response response) {
    // Status can either be in the headers or trailers depending on error
    String grpcStatus = response.header(GRPC_STATUS);
    if (grpcStatus == null) {
      try {
        grpcStatus = response.trailers().get(GRPC_STATUS);
        if (grpcStatus == null) {
          return null;
        }
      } catch (IOException e) {
        // Could not read a status, this generally means the HTTP status is the error.
        return null;
      }
    }
    try {
      return GrpcStatusCode.fromValue(Integer.parseInt(grpcStatus));
    } catch (NumberFormatException ex) {
      // If grpcStatus is not an integer, it's not a valid grpc status code
      return null;
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
    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
    return CompletableResultCode.ofSuccess();
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
