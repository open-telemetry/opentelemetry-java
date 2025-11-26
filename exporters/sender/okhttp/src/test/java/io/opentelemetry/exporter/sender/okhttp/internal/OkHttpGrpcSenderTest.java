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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

  @Test
  void shutdown_NonManagedExecutor_ReturnsImmediately() {
    // This test verifies that when using a non-managed executor (custom ExecutorService),
    // shutdown() returns an already-completed CompletableResultCode immediately.

    // Create a custom ExecutorService - this makes the executor non-managed
    ExecutorService customExecutor = Executors.newSingleThreadExecutor();

    try {
      OkHttpGrpcSender<TestMarshaler> sender =
          new OkHttpGrpcSender<>(
              "http://localhost:8080",
              null,
              Duration.ofSeconds(10).toNanos(),
              Duration.ofSeconds(10).toNanos(),
              Collections::emptyMap,
              null,
              null,
              null,
              customExecutor); // Pass custom executor -> managedExecutor = false

      CompletableResultCode shutdownResult = sender.shutdown();

      // Should complete immediately since executor is not managed
      assertTrue(
          shutdownResult.isDone(),
          "CompletableResultCode should be done immediately for non-managed executor");
      assertTrue(shutdownResult.isSuccess(), "Shutdown should complete successfully");
    } finally {
      // Clean up the custom executor
      customExecutor.shutdownNow();
    }
  }

  @Test
  void shutdown_ExecutorDoesNotTerminateInTime_LogsWarningButSucceeds() throws Exception {
    // This test verifies that when threads don't terminate within 5 seconds,
    // a warning is logged but shutdown still succeeds.

    // Allocate an ephemeral port
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    // Create sender with managed executor (default)
    OkHttpGrpcSender<TestMarshaler> sender =
        new OkHttpGrpcSender<>(
            "http://localhost:" + port,
            null,
            Duration.ofSeconds(10).toNanos(),
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            null,
            null,
            null,
            null); // null executor = managed

    // Start multiple requests to ensure threads are busy
    CountDownLatch blockCallbacks = new CountDownLatch(1);
    for (int i = 0; i < 3; i++) {
      sender.send(
          new TestMarshaler(),
          response -> {
            try {
              // Block in callback for longer than the 5-second timeout
              blockCallbacks.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          },
          error -> {
            try {
              // Block in callback for longer than the 5-second timeout
              blockCallbacks.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          });
    }

    // Give threads time to start (same pattern as existing test)
    Thread.sleep(500);

    // Shutdown will now try to terminate threads that are blocked
    CompletableResultCode shutdownResult = sender.shutdown();

    // The shutdown should eventually complete successfully
    // even though threads didn't terminate in 5 seconds
    assertTrue(
        shutdownResult.join(10, TimeUnit.SECONDS).isSuccess(),
        "Shutdown should succeed even when threads don't terminate quickly");

    // Release the blocking callbacks
    blockCallbacks.countDown();
  }

  @Test
  void shutdown_InterruptedWhileWaiting_StillSucceeds() throws Exception {
    // This test verifies that if the shutdown thread is interrupted while waiting
    // for termination, it still marks the shutdown as successful.

    // Allocate an ephemeral port
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    OkHttpGrpcSender<TestMarshaler> sender =
        new OkHttpGrpcSender<>(
            "http://localhost:" + port,
            null,
            Duration.ofSeconds(10).toNanos(),
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            null,
            null,
            null,
            null);

    // Trigger some activity
    sender.send(new TestMarshaler(), response -> {}, error -> {});

    // Give threads time to start (same pattern as existing test)
    Thread.sleep(500);

    // Start shutdown
    CompletableResultCode shutdownResult = sender.shutdown();

    // Give the shutdown thread a moment to start
    Thread.sleep(100);

    // Find and interrupt the okhttp-shutdown thread to trigger the InterruptedException path
    Thread[] threads = new Thread[Thread.activeCount() + 10];
    int count = Thread.enumerate(threads);
    for (int i = 0; i < count; i++) {
      Thread thread = threads[i];
      if (thread != null && thread.getName().equals("okhttp-shutdown")) {
        // Interrupt the shutdown thread to test the InterruptedException handling
        thread.interrupt();
        break;
      }
    }

    // Even with interruption, shutdown should still succeed
    assertTrue(
        shutdownResult.join(10, TimeUnit.SECONDS).isSuccess(),
        "Shutdown should succeed even when interrupted");
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
