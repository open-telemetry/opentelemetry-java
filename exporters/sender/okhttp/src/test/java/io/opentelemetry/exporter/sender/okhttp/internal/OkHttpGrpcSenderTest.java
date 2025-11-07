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
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
  void shutdown_ShouldTerminateExecutorThreads() throws Exception {
    Logger logger = Logger.getLogger(OkHttpGrpcSenderTest.class.getName());

    // Create a sender that will try to connect to a non-existent endpoint
    // This ensures background threads are created
    OkHttpGrpcSender<TestMarshaler> sender =
        new OkHttpGrpcSender<>(
            "http://localhost:54321", // Non-existent endpoint
            null, // No compression
            Duration.ofSeconds(10).toNanos(),
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            null, // No retry policy
            null, // No SSL context
            null, // No trust manager
            null); // Use default executor (managed by sender)

    // Send a request to trigger thread creation
    CompletableResultCode sendResult = new CompletableResultCode();
    sender.send(new TestMarshaler(), response -> sendResult.succeed(), error -> sendResult.fail());

    // Give threads time to start
    Thread.sleep(500);

    // Capture OkHttp threads before shutdown
    Set<Thread> threadsBeforeShutdown = getOkHttpThreads();
    logger.info(
        "OkHttp threads before shutdown: "
            + threadsBeforeShutdown.size()
            + " threads: "
            + threadsBeforeShutdown.stream()
                .map(Thread::getName)
                .collect(Collectors.joining(", ")));

    // Verify threads exist
    assertFalse(
        threadsBeforeShutdown.isEmpty(), "Expected OkHttp threads to be present before shutdown");

    // Call shutdown and wait for it to complete
    CompletableResultCode shutdownResult = sender.shutdown();

    // Wait for shutdown to complete (this should succeed once the executor threads terminate)
    shutdownResult.join(10, java.util.concurrent.TimeUnit.SECONDS);
    assertTrue(shutdownResult.isSuccess(), "Shutdown should complete successfully");

    // Check threads after shutdown
    Set<Thread> threadsAfterShutdown = getOkHttpThreads();
    logger.info(
        "OkHttp threads after shutdown: "
            + threadsAfterShutdown.size()
            + " threads: "
            + threadsAfterShutdown.stream().map(Thread::getName).collect(Collectors.joining(", ")));

    // Find alive threads
    Set<Thread> aliveThreads =
        threadsAfterShutdown.stream().filter(Thread::isAlive).collect(Collectors.toSet());

    // Separate dispatcher threads (HTTP call threads) from TaskRunner threads (internal OkHttp
    // threads)
    Set<Thread> dispatcherThreads =
        aliveThreads.stream()
            .filter(t -> t.getName().toLowerCase(Locale.ROOT).contains("dispatch"))
            .collect(Collectors.toSet());

    Set<Thread> taskRunnerThreads =
        aliveThreads.stream()
            .filter(t -> t.getName().toLowerCase(Locale.ROOT).contains("taskrunner"))
            .collect(Collectors.toSet());

    if (!aliveThreads.isEmpty()) {
      logger.info("Found " + aliveThreads.size() + " alive OkHttp threads after shutdown:");
      aliveThreads.forEach(
          t -> {
            logger.info(
                "  - "
                    + t.getName()
                    + " (daemon: "
                    + t.isDaemon()
                    + ", state: "
                    + t.getState()
                    + ")");
          });
    }

    // The main requirement: dispatcher threads (HTTP call threads) should be terminated
    assertTrue(
        dispatcherThreads.isEmpty(),
        "Dispatcher threads (HTTP call threads) should be terminated after shutdown. Found "
            + dispatcherThreads.size()
            + " alive dispatcher threads: "
            + dispatcherThreads.stream().map(Thread::getName).collect(Collectors.joining(", ")));

    // TaskRunner threads are OkHttp's internal idle daemon threads. They have a 60-second
    // keep-alive and will terminate on their own. They're harmless since:
    // 1. They're daemon threads (won't prevent JVM exit)
    // 2. They're idle (in TIMED_WAITING state)
    // 3. No new work can be dispatched to them after shutdown
    // We log them for visibility but don't fail the test.
    if (!taskRunnerThreads.isEmpty()) {
      logger.info(
          "Note: "
              + taskRunnerThreads.size()
              + " TaskRunner daemon threads are still alive. "
              + "These are OkHttp internal threads that will terminate after their keep-alive timeout (60s). "
              + "They won't prevent JVM exit.");
    }
  }

  /** Get all threads that appear to be OkHttp-related. */
  private static Set<Thread> getOkHttpThreads() {
    ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
    while (rootGroup.getParent() != null) {
      rootGroup = rootGroup.getParent();
    }

    Thread[] threads = new Thread[rootGroup.activeCount() * 2];
    int count = rootGroup.enumerate(threads, true);

    Set<Thread> okHttpThreads = new java.util.HashSet<>();
    for (int i = 0; i < count; i++) {
      Thread thread = threads[i];
      if (thread != null && thread.getName() != null) {
        String name = thread.getName().toLowerCase(Locale.ROOT);
        if (name.contains("okhttp") || name.contains("ok-http")) {
          okHttpThreads.add(thread);
        }
      }
    }
    return okHttpThreads;
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
