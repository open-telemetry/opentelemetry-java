/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleCounter;
import io.opentelemetry.api.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.metrics.BoundDoubleUpDownCounter;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.BoundLongHistogram;
import io.opentelemetry.api.metrics.BoundLongUpDownCounter;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link Meter}.
 *
 * <p>This implementation should induce as close to zero overhead as possible.
 *
 * <p>A few notes from the specificaiton on allowed behaviors leading to this deasign [<a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument">Instrument
 * Spec</a>]:
 *
 * <ul>
 *   <li>Multiple Insturments with the same name under the same Meter MUST return an error
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
public class NoopMeter implements Meter {

  private static final NoopMeter INSTANCE = new NoopMeter();

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

  private NoopMeter() {}

  private static class NoopLongCounter implements LongCounter {
    @Override
    public void add(long value, Attributes attributes, Context context) {}

    @Override
    public void add(long value, Attributes attributes) {}

    @Override
    public void add(long value) {}

    @Override
    public BoundLongCounter bind(Attributes attributes) {
      return new NoopBoundLongCounter();
    }
  }

  private static class NoopBoundLongCounter implements BoundLongCounter {
    @Override
    public void add(long value) {}

    @Override
    public void add(long value, Context context) {}

    @Override
    public void unbind() {}
  }

  private static class NoopDoubleCounter implements DoubleCounter {
    @Override
    public void add(double value, Attributes attributes, Context context) {}

    @Override
    public void add(double value, Attributes attributes) {}

    @Override
    public void add(double value) {}

    @Override
    public BoundDoubleCounter bind(Attributes attributes) {
      return new NoopBoundDoubleCounter();
    }
  }

  private static class NoopBoundDoubleCounter implements BoundDoubleCounter {
    @Override
    public void add(double value) {}

    @Override
    public void add(double value, Context context) {}

    @Override
    public void unbind() {}
  }

  private static class NoopLongCounterBuilder implements LongCounterBuilder {
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
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {}
  }

  private static class NoopDoubleCounterBuilder implements DoubleCounterBuilder {
    @Override
    public DoubleCounterBuilder setDescription(String description) {
      return this;
    }

    @Override
    public DoubleCounterBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public LongCounterBuilder ofLongs() {
      return new NoopLongCounterBuilder();
    }

    @Override
    public DoubleCounter build() {
      return new NoopDoubleCounter();
    }

    @Override
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {}
  }

  private static class NoopLongUpDownCounter implements LongUpDownCounter {
    @Override
    public void add(long value, Attributes attributes, Context context) {}

    @Override
    public void add(long value, Attributes attributes) {}

    @Override
    public void add(long value) {}

    @Override
    public BoundLongUpDownCounter bind(Attributes attributes) {
      return new NoopBoundLongUpDownCounter();
    }
  }

  private static class NoopBoundLongUpDownCounter implements BoundLongUpDownCounter {
    @Override
    public void add(long value, Context context) {}

    @Override
    public void add(long value) {}

    @Override
    public void unbind() {}
  }

  private static class NoopDoubleUpDownCounter implements DoubleUpDownCounter {
    @Override
    public void add(double value, Attributes attributes, Context context) {}

    @Override
    public void add(double value, Attributes attributes) {}

    @Override
    public void add(double value) {}

    @Override
    public BoundDoubleUpDownCounter bind(Attributes attributes) {
      return new NoopBoundDoubleUpDownCounter();
    }
  }

  private static class NoopBoundDoubleUpDownCounter implements BoundDoubleUpDownCounter {
    @Override
    public void add(double value, Context context) {}

    @Override
    public void add(double value) {}

    @Override
    public void unbind() {}
  }

  private static class NoopLongUpDownCounterBuilder implements LongUpDownCounterBuilder {
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
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {}
  }

  private static class NoopDoubleUpDownCounterBuilder implements DoubleUpDownCounterBuilder {
    @Override
    public DoubleUpDownCounterBuilder setDescription(String description) {
      return this;
    }

    @Override
    public DoubleUpDownCounterBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public LongUpDownCounterBuilder ofLongs() {
      return new NoopLongUpDownCounterBuilder();
    }

    @Override
    public DoubleUpDownCounter build() {
      return new NoopDoubleUpDownCounter();
    }

    @Override
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {}
  }

  public static class NoopDoubleHistogram implements DoubleHistogram {
    @Override
    public void record(double value, Attributes attributes, Context context) {}

    @Override
    public void record(double value, Attributes attributes) {}

    @Override
    public void record(double value) {}

    @Override
    public BoundDoubleHistogram bind(Attributes attributes) {
      return new NoopBoundDoubleHistogram();
    }
  }

  public static class NoopBoundDoubleHistogram implements BoundDoubleHistogram {
    @Override
    public void record(double value, Context context) {}

    @Override
    public void record(double value) {}

    @Override
    public void unbind() {}
  }

  public static class NoopLongHistogram implements LongHistogram {
    @Override
    public void record(long value, Attributes attributes, Context context) {}

    @Override
    public void record(long value, Attributes attributes) {}

    @Override
    public void record(long value) {}

    @Override
    public BoundLongHistogram bind(Attributes attributes) {
      return new NoopBoundLongHistogram();
    }
  }

  public static class NoopBoundLongHistogram implements BoundLongHistogram {
    @Override
    public void record(long value, Context context) {}

    @Override
    public void record(long value) {}

    @Override
    public void unbind() {}
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
    public DoubleHistogramBuilder ofDoubles() {
      return new NoopDoubleHistogramBuilder();
    }

    @Override
    public LongHistogram build() {
      return new NoopLongHistogram();
    }
  }

  public static class NoopDoubleObservableInstrumentBuilder implements DoubleGaugeBuilder {
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
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {}
  }

  public static class NoopLongObservableInstrumentBuilder implements LongGaugeBuilder {
    @Override
    public LongGaugeBuilder setDescription(String description) {
      return this;
    }

    @Override
    public LongGaugeBuilder setUnit(String unit) {
      return this;
    }

    @Override
    public DoubleGaugeBuilder ofDoubles() {
      return new NoopDoubleObservableInstrumentBuilder();
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {}
  }
}
