/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.jaeger.sampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertTrue(limiter.checkCredit(1.0));
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
    // move time 250ms forward, not enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(250);
    clock.timeNanos = currentTime;
    assertFalse(limiter.checkCredit(1.0));

    // move time 500ms forward, now enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(500);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));

    // move time 5s forward, enough to accumulate credits for 10 messages, but it should still be
    // capped at 2
    currentTime += TimeUnit.MILLISECONDS.toNanos(5000);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(1.0));
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
  }

  @Test
  void testRateLimiterLessThanOne() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(0.5, 0.5, clock);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertTrue(limiter.checkCredit(0.25));
    assertTrue(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
    // move time 250ms forward, not enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(250);
    clock.timeNanos = currentTime;
    assertFalse(limiter.checkCredit(0.25));

    // move time 500ms forward, now enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(500);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));

    // move time 5s forward, enough to accumulate credits for 10 messages, but it should still be
    // capped at 2
    currentTime += TimeUnit.MILLISECONDS.toNanos(5000);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(0.25));
    assertTrue(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
  }

  @Test
  void testRateLimiterMaxBalance() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(0.1, 1.0, clock);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));

    // move time 20s forward, enough to accumulate credits for 2 messages, but it should still be
    // capped at 1
    currentTime += TimeUnit.MILLISECONDS.toNanos(20000);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
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

    assertTrue(limiter.checkCredit(100)); // consume initial (max) balance
    assertFalse(limiter.checkCredit(1));

    clock.timeNanos += TimeUnit.MILLISECONDS.toNanos(49); // add 49 credits
    assertFalse(limiter.checkCredit(50));

    clock.timeNanos += TimeUnit.MILLISECONDS.toNanos(1); // add one credit
    assertTrue(limiter.checkCredit(50)); // consume accrued balance
    assertFalse(limiter.checkCredit(1));

    clock.timeNanos +=
        TimeUnit.MILLISECONDS.toNanos(1_000_000); // add a lot of credits (max out balance)
    assertTrue(limiter.checkCredit(1)); // take one credit

    clock.timeNanos +=
        TimeUnit.MILLISECONDS.toNanos(1_000_000); // add a lot of credits (max out balance)
    assertFalse(limiter.checkCredit(101)); // can't consume more than max balance
    assertTrue(limiter.checkCredit(100)); // consume max balance
    assertFalse(limiter.checkCredit(1));
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
    assertEquals(
        numWorkers * creditsPerWorker,
        count.get(),
        "Exactly the allocated number of credits must be consumed");
    assertFalse(limiter.checkCredit(1));
  }
}
