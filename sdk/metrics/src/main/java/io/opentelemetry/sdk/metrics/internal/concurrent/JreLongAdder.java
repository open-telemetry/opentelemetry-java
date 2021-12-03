/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

final class JreLongAdder implements LongAdder {

  private final java.util.concurrent.atomic.LongAdder delegate =
      new java.util.concurrent.atomic.LongAdder();

  JreLongAdder() {}

  @Override
  public void add(long x) {
    delegate.add(x);
  }

  @Override
  public long sum() {
    return delegate.sum();
  }

  @Override
  public void reset() {
    delegate.reset();
  }

  @Override
  public long sumThenReset() {
    return delegate.sumThenReset();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
