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
 * No-op implementations of {@link Meter}.
 *
 * <p>This implementation should induce as close to zero overhead as possible.
 *
 * <p>A few notes from the specification on allowed behaviors leading to this design [<a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument">Instrument
 * Spec</a>]:
 *
 * <ul>
 *   <li>Multiple Instruments with the same name under the same Meter MUST return an error
 *   <li>Different Meters MUST be treated as separate namespaces
 *   <li>Implementations MUST NOT require users to repeatedly obtain a Meter again with the same
 *       name+version+schema_url to pick up configuration changes. This can be achieved either by
 *       allowing to work with an outdated configuration or by ensuring that new configuration
 *       applies also to previously returned Meters.
 *   <li>A MeterProvider could also return a no-op Meter here if application owners configure the
 *       SDK to suppress telemetry produced by this library
 *   <li>In case an invalid name (null or empty string) is specified, a working Meter implementation
 *       MUST be returned as a fallback rather than returning null or throwing an exception,
 * </ul>
 */
@ThreadSafe
class DefaultMeter implements Meter {

  private static final DefaultMeter INSTANCE = new DefaultMeter();

  public static Meter getInstance() {
    return INSTANCE;
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return new NoopLongCounterBuilder();
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return new NoopLongUpDownCounterBuilder();
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return new NoopDoubleHistogramBuilder();
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return new NoopDoubleObservableInstrumentBuilder();
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

    private static final ObservableLongCounter NOOP = new ObservableLongCounter() {};

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
      return new NoopDoubleCounterBuilder();
    }

    @Override
    public LongCounter build() {
      return new NoopLongCounter();
    }

    @Override
    public ObservableLongCounter buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return NOOP;
    }
  }

  private static class NoopDoubleCounterBuilder implements DoubleCounterBuilder {
    private static final ObservableDoubleCounter NOOP = new ObservableDoubleCounter() {};

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
      return new NoopDoubleCounter();
    }

    @Override
    public ObservableDoubleCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return NOOP;
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
    private static final ObservableLongUpDownCounter NOOP = new ObservableLongUpDownCounter() {};

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
      return new NoopDoubleUpDownCounterBuilder();
    }

    @Override
    public LongUpDownCounter build() {
      return new NoopLongUpDownCounter();
    }

    @Override
    public ObservableLongUpDownCounter buildWithCallback(
        Consumer<ObservableLongMeasurement> callback) {
      return NOOP;
    }
  }

  private static class NoopDoubleUpDownCounterBuilder implements DoubleUpDownCounterBuilder {
    private static final ObservableDoubleUpDownCounter NOOP =
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
      return new NoopDoubleUpDownCounter();
    }

    @Override
    public ObservableDoubleUpDownCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return NOOP;
    }
  }

  public static class NoopDoubleHistogram implements DoubleHistogram {
    @Override
    public void record(double value, Attributes attributes, Context context) {}

    @Override
    public void record(double value, Attributes attributes) {}

    @Override
    public void record(double value) {}
  }

  public static class NoopLongHistogram implements LongHistogram {
    @Override
    public void record(long value, Attributes attributes, Context context) {}

    @Override
    public void record(long value, Attributes attributes) {}

    @Override
    public void record(long value) {}
  }

  public static class NoopDoubleHistogramBuilder implements DoubleHistogramBuilder {
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
      return new NoopLongHistogramBuilder();
    }

    @Override
    public DoubleHistogram build() {
      return new NoopDoubleHistogram();
    }
  }

  public static class NoopLongHistogramBuilder implements LongHistogramBuilder {
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
      return new NoopLongHistogram();
    }
  }

  public static class NoopDoubleObservableInstrumentBuilder implements DoubleGaugeBuilder {
    private static final ObservableDoubleGauge NOOP = new ObservableDoubleGauge() {};

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
      return new NoopLongObservableInstrumentBuilder();
    }

    @Override
    public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      return NOOP;
    }
  }

  public static class NoopLongObservableInstrumentBuilder implements LongGaugeBuilder {
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
  }
}
