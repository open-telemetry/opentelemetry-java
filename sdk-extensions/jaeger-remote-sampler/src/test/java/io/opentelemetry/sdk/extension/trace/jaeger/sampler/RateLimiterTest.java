/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * This class was taken from Jaeger java client.
 * https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/src/test/java/io/jaegertracing/internal/utils/RateLimiterTest.java
 */
class RateLimiterTest {

  private static class MockClock implements Clock {

    long timeNanos;

    @Override
    public long now() {
      return 0;
    }

    @Override
    public long nanoTime() {
      return timeNanos;
    }
  }

  @Test
  void testRateLimiterWholeNumber() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(2.0, 2.0, clock);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isFalse();
    // move time 250ms forward, not enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(250);
    clock.timeNanos = currentTime;
    assertThat(limiter.checkCredit(1.0)).isFalse();

    // move time 500ms forward, now enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(500);
    clock.timeNanos = currentTime;

    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isFalse();

    // move time 5s forward, enough to accumulate credits for 10 messages, but it should still be
    // capped at 2
    currentTime += TimeUnit.MILLISECONDS.toNanos(5000);
    clock.timeNanos = currentTime;

    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isFalse();
    assertThat(limiter.checkCredit(1.0)).isFalse();
    assertThat(limiter.checkCredit(1.0)).isFalse();
  }

  @Test
  void testRateLimiterLessThanOne() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(0.5, 0.5, clock);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertThat(limiter.checkCredit(0.25)).isTrue();
    assertThat(limiter.checkCredit(0.25)).isTrue();
    assertThat(limiter.checkCredit(0.25)).isFalse();
    // move time 250ms forward, not enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(250);
    clock.timeNanos = currentTime;
    assertThat(limiter.checkCredit(0.25)).isFalse();

    // move time 500ms forward, now enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(500);
    clock.timeNanos = currentTime;

    assertThat(limiter.checkCredit(0.25)).isTrue();
    assertThat(limiter.checkCredit(0.25)).isFalse();

    // move time 5s forward, enough to accumulate credits for 10 messages, but it should still be
    // capped at 2
    currentTime += TimeUnit.MILLISECONDS.toNanos(5000);
    clock.timeNanos = currentTime;

    assertThat(limiter.checkCredit(0.25)).isTrue();
    assertThat(limiter.checkCredit(0.25)).isTrue();
    assertThat(limiter.checkCredit(0.25)).isFalse();
    assertThat(limiter.checkCredit(0.25)).isFalse();
    assertThat(limiter.checkCredit(0.25)).isFalse();
  }

  @Test
  void testRateLimiterMaxBalance() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(0.1, 1.0, clock);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isFalse();

    // move time 20s forward, enough to accumulate credits for 2 messages, but it should still be
    // capped at 1
    currentTime += TimeUnit.MILLISECONDS.toNanos(20000);
    clock.timeNanos = currentTime;

    assertThat(limiter.checkCredit(1.0)).isTrue();
    assertThat(limiter.checkCredit(1.0)).isFalse();
  }

  /**
   * Validates rate limiter behavior with {@link System#nanoTime()}-like (non-zero) initial nano
   * ticks.
   */
  @Test
  void testRateLimiterInitial() {
    MockClock clock = new MockClock();
    clock.timeNanos = TimeUnit.MILLISECONDS.toNanos(-1_000_000);
    RateLimiter limiter = new RateLimiter(1000, 100, clock);

    assertThat(limiter.checkCredit(100)).isTrue(); // consume initial (max) balance
    assertThat(limiter.checkCredit(1)).isFalse();

    clock.timeNanos += TimeUnit.MILLISECONDS.toNanos(49); // add 49 credits
    assertThat(limiter.checkCredit(50)).isFalse();

    clock.timeNanos += TimeUnit.MILLISECONDS.toNanos(1); // add one credit
    assertThat(limiter.checkCredit(50)).isTrue(); // consume accrued balance
    assertThat(limiter.checkCredit(1)).isFalse();

    clock.timeNanos +=
        TimeUnit.MILLISECONDS.toNanos(1_000_000); // add a lot of credits (max out balance)
    assertThat(limiter.checkCredit(1)).isTrue(); // take one credit

    clock.timeNanos +=
        TimeUnit.MILLISECONDS.toNanos(1_000_000); // add a lot of credits (max out balance)
    assertThat(limiter.checkCredit(101)).isFalse(); // can't consume more than max balance
    assertThat(limiter.checkCredit(100)).isTrue(); // consume max balance
    assertThat(limiter.checkCredit(1)).isFalse();
  }

  /** Validates concurrent credit check correctness. */
  @Test
  void testRateLimiterConcurrency() throws InterruptedException, ExecutionException {
    int numWorkers = 8;
    ExecutorService executorService = Executors.newFixedThreadPool(numWorkers);
    final int creditsPerWorker = 1000;
    MockClock clock = new MockClock();
    final RateLimiter limiter = new RateLimiter(1, numWorkers * creditsPerWorker, clock);
    final AtomicInteger count = new AtomicInteger();
    List<Future<?>> futures = new ArrayList<>(numWorkers);
    for (int w = 0; w < numWorkers; ++w) {
      Future<?> future =
          executorService.submit(
              () -> {
                for (int i = 0; i < creditsPerWorker * 2; ++i) {
                  if (limiter.checkCredit(1)) {
                    count.getAndIncrement(); // count allowed operations
                  }
                }
              });
      futures.add(future);
    }
    for (Future<?> future : futures) {
      future.get();
    }
    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.SECONDS);
    assertThat(count.get())
        .withFailMessage("Exactly the allocated number of credits must be consumed")
        .isEqualTo(numWorkers * creditsPerWorker);
    assertThat(limiter.checkCredit(1)).isFalse();
  }
}
