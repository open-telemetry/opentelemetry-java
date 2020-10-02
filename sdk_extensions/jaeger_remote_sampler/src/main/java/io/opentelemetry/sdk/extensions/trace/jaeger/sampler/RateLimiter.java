/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.jaeger.sampler;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class was taken from Jaeger java client.
 * https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/src/main/java/io/jaegertracing/internal/samplers/RateLimitingSampler.java
 */
class RateLimiter {
  private final Clock clock;
  private final double creditsPerNanosecond;
  private final long maxBalance; // max balance in nano ticks
  private final AtomicLong debit; // last op nano time less remaining balance

  RateLimiter(double creditsPerSecond, double maxBalance, Clock clock) {
    this.clock = clock;
    this.creditsPerNanosecond = creditsPerSecond / 1.0e9;
    this.maxBalance = (long) (maxBalance / creditsPerNanosecond);
    this.debit = new AtomicLong(clock.nanoTime() - this.maxBalance);
  }

  public boolean checkCredit(double itemCost) {
    long cost = (long) (itemCost / creditsPerNanosecond);
    long credit;
    long currentDebit;
    long balance;
    do {
      currentDebit = debit.get();
      credit = clock.nanoTime();
      balance = credit - currentDebit;
      if (balance > maxBalance) {
        balance = maxBalance;
      }
      balance -= cost;
      if (balance < 0) {
        return false;
      }
    } while (!debit.compareAndSet(currentDebit, credit - balance));
    return true;
  }
}
