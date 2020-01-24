package io.opentelemetry.sdk.metrics;

import java.util.concurrent.atomic.AtomicLong;

final class LongSumAggregator
    implements BaseAggregator.LongBaseAggregator<LongSumAggregator> {
  // TODO: Change to use LongAdder when changed to java8.
  private final AtomicLong value;

  LongSumAggregator() {
    this.value = new AtomicLong();
  }

  @Override
  public void merge(LongSumAggregator other) {
    this.value.addAndGet(other.value.get());
  }

  @Override
  public void update(long value) {
    this.value.addAndGet(value);
  }
}
