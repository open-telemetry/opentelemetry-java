/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

final class JreDoubleAdder implements DoubleAdder {

  private final java.util.concurrent.atomic.DoubleAdder delegate;

  JreDoubleAdder() {
    delegate = new java.util.concurrent.atomic.DoubleAdder();
  }

  @Override
  public void add(double x) {
    delegate.add(x);
  }

  @Override
  public double sum() {
    return delegate.sum();
  }

  @Override
  public void reset() {
    delegate.reset();
  }

  @Override
  public double sumThenReset() {
    return delegate.sumThenReset();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
