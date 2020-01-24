package io.opentelemetry.sdk.metrics;

import com.google.common.util.concurrent.AtomicDouble;

final class DoubleSumAggregator
    implements BaseAggregator.DoubleBaseAggregator<DoubleSumAggregator> {
  // TODO: Change to use DoubleAdder when changed to java8.
  private final AtomicDouble value;

  DoubleSumAggregator() {
    this.value = new AtomicDouble();
  }

  @Override
  public void merge(DoubleSumAggregator other) {
    this.value.addAndGet(other.value.get());
  }

  @Override
  public void update(double value) {
    this.value.addAndGet(value);
  }
}
