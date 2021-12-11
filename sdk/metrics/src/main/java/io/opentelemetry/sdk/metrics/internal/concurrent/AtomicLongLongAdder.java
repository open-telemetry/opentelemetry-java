/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

import java.util.concurrent.atomic.AtomicLong;

final class AtomicLongLongAdder implements LongAdder {

  private final AtomicLong atomicLong = new AtomicLong();

  @Override
  public void add(long x) {
    while (true) {
      long current = atomicLong.get();
      long next = current + x;
      if (atomicLong.compareAndSet(current, next)) {
        return;
      }
    }
  }

  @Override
  public long sum() {
    return atomicLong.get();
  }

  @Override
  public void reset() {
    atomicLong.set(0);
  }

  @Override
  public long sumThenReset() {
    long prev;
    do {
      prev = atomicLong.get();
    } while (!atomicLong.compareAndSet(prev, 0));
    return prev;
  }

  @Override
  public String toString() {
    return Long.toString(sum());
  }
}
