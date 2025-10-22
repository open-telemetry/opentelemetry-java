/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.exporter.grpc.GrpcStatusCode;
import io.opentelemetry.exporter.internal.RetryUtil;
import java.util.Set;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OkHttpGrpcSenderTest {

  private static final String GRPC_STATUS = "grpc-status";
  private static final MediaType TEXT_PLAIN = MediaType.get("text/plain");

  static Set<String> provideRetryableGrpcStatusCodes() {
    return RetryUtil.retryableGrpcStatusCodes();
  }

  @ParameterizedTest(name = "isRetryable should return true for GRPC status code: {0}")
  @MethodSource("provideRetryableGrpcStatusCodes")
  void isRetryable_RetryableGrpcStatus(String retryableGrpcStatus) {
    Response response = createResponse(503, retryableGrpcStatus, "Retryable");
    boolean isRetryable = OkHttpGrpcSender.isRetryable(response);
    assertTrue(isRetryable);
  }

  @Test
  void isRetryable_NonRetryableGrpcStatus() {
    String nonRetryableGrpcStatus =
        Integer.valueOf(GrpcStatusCode.UNKNOWN.getValue()).toString(); // INVALID_ARGUMENT
    Response response = createResponse(503, nonRetryableGrpcStatus, "Non-retryable");
    boolean isRetryable = OkHttpGrpcSender.isRetryable(response);
    assertFalse(isRetryable);
  }

  private static Response createResponse(int httpCode, String grpcStatus, String message) {
    return new Response.Builder()
        .request(new Request.Builder().url("http://localhost/").build())
        .protocol(Protocol.HTTP_2)
        .code(httpCode)
        .body(ResponseBody.create("body", TEXT_PLAIN))
        .message(message)
        .header(GRPC_STATUS, grpcStatus)
        .build();
  }
}
