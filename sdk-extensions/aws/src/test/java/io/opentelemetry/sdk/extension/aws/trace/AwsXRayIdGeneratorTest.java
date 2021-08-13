/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.trace.IdGenerator;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AwsXrayIdGenerator}. */
@SuppressWarnings("deprecation") // Moved to contrib
class AwsXRayIdGeneratorTest {

  @Test
  void shouldGenerateValidIds() {
    AwsXrayIdGenerator generator = AwsXrayIdGenerator.getInstance();
    for (int i = 0; i < 1000; i++) {
      String traceId = generator.generateTraceId();
      assertThat(TraceId.isValid(traceId)).isTrue();
      String spanId = generator.generateSpanId();
      assertThat(SpanId.isValid(spanId)).isTrue();
    }
  }

  @Test
  void shouldGenerateTraceIdsWithTimestampsWithAllowedXrayTimeRange() {
    AwsXrayIdGenerator generator = AwsXrayIdGenerator.getInstance();
    for (int i = 0; i < 1000; i++) {
      String traceId = generator.generateTraceId();
      long unixSeconds = Long.valueOf(traceId.subSequence(0, 8).toString(), 16);
      long ts = unixSeconds * 1000L;
      long currentTs = System.currentTimeMillis();
      assertThat(ts).isLessThanOrEqualTo(currentTs);
      long month = 86400000L * 30L;
      assertThat(ts).isGreaterThan(currentTs - month);
    }
  }

  @Test
  void shouldGenerateUniqueIdsInMultithreadedEnvironment()
      throws BrokenBarrierException, InterruptedException {
    AwsXrayIdGenerator generator = AwsXrayIdGenerator.getInstance();
    Set<String> traceIds = new CopyOnWriteArraySet<>();
    Set<String> spanIds = new CopyOnWriteArraySet<>();
    int threads = 8;
    int generations = 128;
    CyclicBarrier barrier = new CyclicBarrier(threads + 1);
    Executor executor = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
      executor.execute(new GenerateRunner(generations, generator, barrier, traceIds, spanIds));
    }
    barrier.await();
    barrier.await();
    assertThat(traceIds).hasSize(threads * generations);
    assertThat(spanIds).hasSize(threads * generations);
  }

  static class GenerateRunner implements Runnable {

    private final int generations;
    private final IdGenerator idsGenerator;
    private final CyclicBarrier barrier;
    private final Set<String> traceIds;
    private final Set<String> spanIds;

    GenerateRunner(
        int generations,
        IdGenerator idsGenerator,
        CyclicBarrier barrier,
        Set<String> traceIds,
        Set<String> spanIds) {
      this.generations = generations;
      this.idsGenerator = idsGenerator;
      this.barrier = barrier;
      this.traceIds = traceIds;
      this.spanIds = spanIds;
    }

    @Override
    public void run() {
      try {
        barrier.await();
        for (int i = 0; i < generations; i++) {
          traceIds.add(idsGenerator.generateTraceId());
          spanIds.add(idsGenerator.generateSpanId());
        }
        barrier.await();
      } catch (InterruptedException | BrokenBarrierException cause) {
        throw new IllegalStateException(cause);
      }
    }
  }
}
