/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Collections;
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
        Integer.valueOf(GrpcExporterUtil.GRPC_STATUS_UNKNOWN).toString(); // INVALID_ARGUMENT
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

  @Test
  void shutdown_CompletableResultCodeShouldWaitForThreads() throws Exception {
    // This test verifies that shutdown() returns a CompletableResultCode that only
    // completes AFTER threads terminate, not immediately.

    // Allocate an ephemeral port and immediately close it to get a port with nothing listening
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    OkHttpGrpcSender<TestMarshaler> sender =
        new OkHttpGrpcSender<>(
            "http://localhost:" + port, // Non-existent endpoint to trigger thread creation
            null,
            Duration.ofSeconds(10).toNanos(),
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            null,
            null,
            null,
            null);

    CompletableResultCode sendResult = new CompletableResultCode();
    sender.send(new TestMarshaler(), response -> sendResult.succeed(), error -> sendResult.fail());

    // Give threads time to start
    Thread.sleep(500);

    CompletableResultCode shutdownResult = sender.shutdown();

    // The key test: the CompletableResultCode should NOT be done() immediately
    // because we need to wait for threads to terminate.
    // Before #7840, this would fail.
    assertFalse(
        shutdownResult.isDone(),
        "CompletableResultCode should not be done immediately - it should wait for thread termination");

    // Now wait for it to complete
    shutdownResult.join(10, java.util.concurrent.TimeUnit.SECONDS);
    assertTrue(shutdownResult.isDone(), "CompletableResultCode should be done after waiting");
    assertTrue(shutdownResult.isSuccess(), "Shutdown should complete successfully");
  }

  /** Simple test marshaler for testing purposes. */
  private static class TestMarshaler extends Marshaler {
    @Override
    public int getBinarySerializedSize() {
      return 0;
    }

    @Override
    protected void writeTo(io.opentelemetry.exporter.internal.marshal.Serializer output)
        throws IOException {
      // Empty marshaler
    }
  }
}
