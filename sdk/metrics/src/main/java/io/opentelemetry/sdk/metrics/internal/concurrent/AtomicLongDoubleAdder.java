/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

import java.util.concurrent.atomic.AtomicLong;

final class AtomicLongDoubleAdder implements DoubleAdder {

  private final AtomicLong atomicLong = new AtomicLong();

  AtomicLongDoubleAdder() {}

  @Override
  public void add(double x) {
    while (true) {
      long currentLongBits = atomicLong.get();
      double currentDouble = Double.longBitsToDouble(currentLongBits);
      double nextDouble = currentDouble + x;
      long nextLongBits = Double.doubleToLongBits(nextDouble);
      if (atomicLong.compareAndSet(currentLongBits, nextLongBits)) {
        return;
      }
    }
  }

  @Override
  public double sum() {
    return Double.longBitsToDouble(atomicLong.get());
  }

  @Override
  public void reset() {
    atomicLong.set(0);
  }

  @Override
  public double sumThenReset() {
    long prev;
    do {
      prev = atomicLong.get();
    } while (!atomicLong.compareAndSet(prev, 0));
    return Double.longBitsToDouble(prev);
  }

  @Override
  public String toString() {
    return Double.toString(sum());
  }
}
