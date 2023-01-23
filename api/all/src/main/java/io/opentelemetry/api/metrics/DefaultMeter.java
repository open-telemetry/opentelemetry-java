/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementation of {@link Meter}.
 *
 * <p>This implementation should induce as close to zero overhead as possible.
 */
@ThreadSafe
class DefaultMeter implements Meter {

  private static final Meter INSTANCE = new DefaultMeter();

  private static final LongCounterBuilder NOOP_LONG_COUNTER_BUILDER = new NoopLongCounterBuilder();
  private static final LongUpDownCounterBuilder NOOP_LONG_UP_DOWN_COUNTER_BUILDER =
      new NoopLongUpDownCounterBuilder();
  private static final DoubleHistogramBuilder NOOP_DOUBLE_HISTOGRAM_BUILDER =
      new NoopDoubleHistogramBuilder();
  private static final DoubleGaugeBuilder NOOP_DOUBLE_GAUGE_BUILDER = new NoopDoubleGaugeBuilder();
  private static final BatchCallback NOOP_BATCH_CALLBACK = new BatchCallback() {};
  private static final ObservableDoubleMeasurement NOOP_OBSERVABLE_DOUBLE_MEASUREMENT =
      new NoopObservableDoubleMeasurement();
  private static final ObservableLongMeasurement NOOP_OBSERVABLE_LONG_MEASUREMENT =
      new NoopObservableLongMeasurement();

  static Meter getInstance() {
    return INSTANCE;
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return NOOP_LONG_COUNTER_BUILDER;
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return NOOP_LONG_UP_DOWN_COUNTER_BUILDER;
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return NOOP_DOUBLE_HISTOGRAM_BUILDER;
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return NOOP_DOUBLE_GAUGE_BUILDER;
  }

  @Override
  public BatchCallback batchCallback(
      Runnable callback,
      ObservableMeasurement observableMeasurement,
      ObservableMeasurement... additionalMeasurements) {
    return NOOP_BATCH_CALLBACK;
  }

  private DefaultMeter() {}

  private static class NoopLongCounter implements LongCounter {
    @Override
    public void add(long value, Attributes attributes, Context context) {}

    @Override
    public void add(long value, Attributes attributes) {}

    @Override
    public void add(long value) {}
  }

  private static class NoopDoubleCounter implements DoubleCounter {
    @Override
    public void add(double value, Attributes attributes, Context context) {}

    @Override
    public void add(double value, Attributes attributes) {}

    @Override
    public void add(double value) {}
  }

  private static class NoopLongCounterBuilder implements LongCounterBuilder {
    private static final LongCounter NOOP_COUNTER = new NoopLongCounter();
    private static final ObservableLongCounter NOOP_OBSERVABLE_COUNTER =
        new ObservableLongCounter() {};
    private static final DoubleCounterBuilder NOOP_DOUBLE_COUNTER_BUILDER =
        new NoopDoubleCounterBuilder();

    @Override
    public LongCounterBuilder setDescription(String description) {
      return this;
    }

    @Override
    public LongCounterBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public DoubleCounterBuilder ofDoubles() {
      return NOOP_DOUBLE_COUNTER_BUILDER;
    }

    @Override
    public LongCounter build() {
      return NOOP_COUNTER;
    }

    @Override
    public ObservableLongCounter buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return NOOP_OBSERVABLE_COUNTER;
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return NOOP_OBSERVABLE_LONG_MEASUREMENT;
    }
  }

  private static class NoopDoubleCounterBuilder implements DoubleCounterBuilder {
    private static final DoubleCounter NOOP_COUNTER = new NoopDoubleCounter();
    private static final ObservableDoubleCounter NOOP_OBSERVABLE_COUNTER =
        new ObservableDoubleCounter() {};

    @Override
    public DoubleCounterBuilder setDescription(String description) {
      return this;
    }

    @Override
    public DoubleCounterBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public DoubleCounter build() {
      return NOOP_COUNTER;
    }

    @Override
    public ObservableDoubleCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return NOOP_OBSERVABLE_COUNTER;
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return NOOP_OBSERVABLE_DOUBLE_MEASUREMENT;
    }
  }

  private static class NoopLongUpDownCounter implements LongUpDownCounter {
    @Override
    public void add(long value, Attributes attributes, Context context) {}

    @Override
    public void add(long value, Attributes attributes) {}

    @Override
    public void add(long value) {}
  }

  private static class NoopDoubleUpDownCounter implements DoubleUpDownCounter {
    @Override
    public void add(double value, Attributes attributes, Context context) {}

    @Override
    public void add(double value, Attributes attributes) {}

    @Override
    public void add(double value) {}
  }

  private static class NoopLongUpDownCounterBuilder implements LongUpDownCounterBuilder {
    private static final LongUpDownCounter NOOP_UP_DOWN_COUNTER = new NoopLongUpDownCounter() {};
    private static final ObservableLongUpDownCounter NOOP_OBSERVABLE_UP_DOWN_COUNTER =
        new ObservableLongUpDownCounter() {};
    private static final DoubleUpDownCounterBuilder NOOP_DOUBLE_UP_DOWN_COUNTER_BUILDER =
        new NoopDoubleUpDownCounterBuilder();

    @Override
    public LongUpDownCounterBuilder setDescription(String description) {
      return this;
    }

    @Override
    public LongUpDownCounterBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public DoubleUpDownCounterBuilder ofDoubles() {
      return NOOP_DOUBLE_UP_DOWN_COUNTER_BUILDER;
    }

    @Override
    public LongUpDownCounter build() {
      return NOOP_UP_DOWN_COUNTER;
    }

    @Override
    public ObservableLongUpDownCounter buildWithCallback(
        Consumer<ObservableLongMeasurement> callback) {
      return NOOP_OBSERVABLE_UP_DOWN_COUNTER;
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return NOOP_OBSERVABLE_LONG_MEASUREMENT;
    }
  }

  private static class NoopDoubleUpDownCounterBuilder implements DoubleUpDownCounterBuilder {
    private static final DoubleUpDownCounter NOOP_UP_DOWN_COUNTER =
        new NoopDoubleUpDownCounter() {};
    private static final ObservableDoubleUpDownCounter NOOP_OBSERVABLE_UP_DOWN_COUNTER =
        new ObservableDoubleUpDownCounter() {};

    @Override
    public DoubleUpDownCounterBuilder setDescription(String description) {
      return this;
    }

    @Override
    public DoubleUpDownCounterBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public DoubleUpDownCounter build() {
      return NOOP_UP_DOWN_COUNTER;
    }

    @Override
    public ObservableDoubleUpDownCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return NOOP_OBSERVABLE_UP_DOWN_COUNTER;
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return NOOP_OBSERVABLE_DOUBLE_MEASUREMENT;
    }
  }

  private static class NoopDoubleHistogram implements DoubleHistogram {
    @Override
    public void record(double value, Attributes attributes, Context context) {}

    @Override
    public void record(double value, Attributes attributes) {}

    @Override
    public void record(double value) {}
  }

  private static class NoopLongHistogram implements LongHistogram {
    @Override
    public void record(long value, Attributes attributes, Context context) {}

    @Override
    public void record(long value, Attributes attributes) {}

    @Override
    public void record(long value) {}
  }

  private static class NoopDoubleHistogramBuilder implements DoubleHistogramBuilder {
    private static final DoubleHistogram NOOP = new NoopDoubleHistogram();
    private static final LongHistogramBuilder NOOP_LONG_HISTOGRAM_BUILDER =
        new NoopLongHistogramBuilder();

    @Override
    public DoubleHistogramBuilder setDescription(String description) {
      return this;
    }

    @Override
    public DoubleHistogramBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public LongHistogramBuilder ofLongs() {
      return NOOP_LONG_HISTOGRAM_BUILDER;
    }

    @Override
    public DoubleHistogram build() {
      return NOOP;
    }
  }

  private static class NoopLongHistogramBuilder implements LongHistogramBuilder {
    private static final LongHistogram NOOP = new NoopLongHistogram();

    @Override
    public LongHistogramBuilder setDescription(String description) {
      return this;
    }

    @Override
    public LongHistogramBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public LongHistogram build() {
      return NOOP;
    }
  }

  private static class NoopDoubleGaugeBuilder implements DoubleGaugeBuilder {
    private static final ObservableDoubleGauge NOOP = new ObservableDoubleGauge() {};
    private static final LongGaugeBuilder NOOP_LONG_GAUGE_BUILDER = new NoopLongGaugeBuilder();

    @Override
    public DoubleGaugeBuilder setDescription(String description) {
      return this;
    }

    @Override
    public DoubleGaugeBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public LongGaugeBuilder ofLongs() {
      return NOOP_LONG_GAUGE_BUILDER;
    }

    @Override
    public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      return NOOP;
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return NOOP_OBSERVABLE_DOUBLE_MEASUREMENT;
    }
  }

  private static class NoopLongGaugeBuilder implements LongGaugeBuilder {
    private static final ObservableLongGauge NOOP = new ObservableLongGauge() {};

    @Override
    public LongGaugeBuilder setDescription(String description) {
      return this;
    }

    @Override
    public LongGaugeBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public ObservableLongGauge buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return NOOP;
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return NOOP_OBSERVABLE_LONG_MEASUREMENT;
    }
  }

  private static class NoopObservableDoubleMeasurement implements ObservableDoubleMeasurement {
    @Override
    public void record(double value) {}

    @Override
    public void record(double value, Attributes attributes) {}
  }

  private static class NoopObservableLongMeasurement implements ObservableLongMeasurement {
    @Override
    public void record(long value) {}

    @Override
    public void record(long value, Attributes attributes) {}
  }
}
