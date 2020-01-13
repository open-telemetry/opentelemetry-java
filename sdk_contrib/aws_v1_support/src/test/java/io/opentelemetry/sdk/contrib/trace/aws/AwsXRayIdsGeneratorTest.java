/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.contrib.trace.aws;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.IdsGenerator;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AwsXRayIdsGenerator}. */
@RunWith(JUnit4.class)
public class AwsXRayIdsGeneratorTest {

  @Test
  public void shouldGenerateValidIds() {
    AwsXRayIdsGenerator generator = new AwsXRayIdsGenerator();
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId.isValid()).isTrue();
    SpanId spanId = generator.generateSpanId();
    assertThat(spanId.isValid()).isTrue();
  }

  @Test
  public void shouldGenerateTraceIdsWithTimestampsWithAllowedXrayTimeRange() {
    AwsXRayIdsGenerator generator = new AwsXRayIdsGenerator();
    TraceId traceId = generator.generateTraceId();
    Long unixSeconds = Long.valueOf(traceId.toLowerBase16().substring(0, 8), 16);
    long ts = unixSeconds.longValue() * 1000L;
    long currentTs = System.currentTimeMillis();
    assertThat(ts <= currentTs).isTrue();
    long month = 86400000L * 30L;
    assertThat(ts > currentTs - month).isTrue();
  }

  @Test
  public void shouldGenerateUniqueIdsInMultithreadedEnvironment()
      throws BrokenBarrierException, InterruptedException {
    AwsXRayIdsGenerator generator = new AwsXRayIdsGenerator();
    Set<TraceId> traceIds = new CopyOnWriteArraySet<>();
    Set<SpanId> spanIds = new CopyOnWriteArraySet<>();
    int threads = 8;
    int generations = 128;
    CyclicBarrier barrier = new CyclicBarrier(threads + 1);
    Executor executor = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
      executor.execute(new GenerateRunner(generations, generator, barrier, traceIds, spanIds));
    }
    barrier.await();
    barrier.await();
    assertThat(traceIds.size()).isEqualTo(threads * generations);
    assertThat(spanIds.size()).isEqualTo(threads * generations);
  }

  static class GenerateRunner implements Runnable {

    private final int generations;
    private final IdsGenerator idsGenerator;
    private final CyclicBarrier barrier;
    private final Set<TraceId> traceIds;
    private final Set<SpanId> spanIds;

    GenerateRunner(
        int generations,
        IdsGenerator idsGenerator,
        CyclicBarrier barrier,
        Set<TraceId> traceIds,
        Set<SpanId> spanIds) {
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
