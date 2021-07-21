/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class was taken from Jaeger java client.
 * https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/src/main/java/io/jaegertracing/internal/samplers/RateLimitingSampler.java
 *
 * <p>Variables have been renamed for clarity.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class RateLimiter {
  private final Clock clock;
  private final double creditsPerNanosecond;
  private final long maxBalance; // max balance in nano ticks
  private final AtomicLong currentBalance; // last op nano time less remaining balance

  /**
   * Create a new RateLimiter with the provided parameters.
   *
   * @param creditsPerSecond How many credits to accrue per second.
   * @param maxBalance The maximum balance that the limiter can hold, which corresponds to the rate
   *     that is being limited to.
   * @param clock An implementation of the {@link Clock} interface.
   */
  public RateLimiter(double creditsPerSecond, double maxBalance, Clock clock) {
    this.clock = clock;
    this.creditsPerNanosecond = creditsPerSecond / 1.0e9;
    this.maxBalance = (long) (maxBalance / creditsPerNanosecond);
    this.currentBalance = new AtomicLong(clock.nanoTime() - this.maxBalance);
  }

  /**
   * Check to see if the provided cost can be spent within the current limits. Will deduct the cost
   * from the current balance if it can be spent.
   */
  public boolean trySpend(double itemCost) {
    long cost = (long) (itemCost / creditsPerNanosecond);
    long currentNanos;
    long currentBalanceNanos;
    long availableBalanceAfterWithdrawal;
    do {
      currentBalanceNanos = this.currentBalance.get();
      currentNanos = clock.nanoTime();
      long currentAvailableBalance = currentNanos - currentBalanceNanos;
      if (currentAvailableBalance > maxBalance) {
        currentAvailableBalance = maxBalance;
      }
      availableBalanceAfterWithdrawal = currentAvailableBalance - cost;
      if (availableBalanceAfterWithdrawal < 0) {
        return false;
      }
    } while (!this.currentBalance.compareAndSet(
        currentBalanceNanos, currentNanos - availableBalanceAfterWithdrawal));
    return true;
  }
}
