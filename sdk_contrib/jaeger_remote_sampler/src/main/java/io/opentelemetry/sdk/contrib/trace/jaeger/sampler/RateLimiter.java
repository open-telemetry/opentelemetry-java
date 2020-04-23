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

package io.opentelemetry.sdk.contrib.trace.jaeger.sampler;

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
