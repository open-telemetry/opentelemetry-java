/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.testing.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongGauge;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;

/** Unit tests for No-op {@link Meter}. */
@SuppressLogger
public abstract class AbstractDefaultMeterTest {
  private final Meter meter = getMeter();

  protected abstract Meter getMeter();

  protected abstract MeterProvider getMeterProvider();

  @Test
  void noopMeterProvider_getDoesNotThrow() {
    MeterProvider provider = getMeterProvider();
    provider.get("user-instrumentation");
  }

  @Test
  void noopMeterProvider_builderDoesNotThrow() {
    MeterProvider provider = getMeterProvider();
    provider.meterBuilder("user-instrumentation").build();
    provider.meterBuilder("advanced-instrumetnation").setInstrumentationVersion("1.0").build();
    provider.meterBuilder("schema-instrumentation").setSchemaUrl("myschema://url").build();
    provider
        .meterBuilder("schema-instrumentation")
        .setInstrumentationVersion("1.0")
        .setSchemaUrl("myschema://url")
        .build();
  }

  @Test
  void noopLongCounter_doesNotThrow() {
    LongCounter counter =
        meter.counterBuilder("size").setDescription("The size I'm measuring").setUnit("1").build();
    counter.add(1);
    counter.add(1, Attributes.of(stringKey("thing"), "car"));
    counter.add(1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopDoubleCounter_doesNotThrow() {
    DoubleCounter counter =
        meter
            .counterBuilder("size")
            .ofDoubles()
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build();
    counter.add(1.2);
    counter.add(2.5, Attributes.of(stringKey("thing"), "car"));
    counter.add(2.5, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopLongUpDownCounter_doesNotThrow() {
    LongUpDownCounter counter =
        meter
            .upDownCounterBuilder("size")
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build();
    counter.add(-1);
    counter.add(1, Attributes.of(stringKey("thing"), "car"));
    counter.add(1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopDoubleUpDownCounter_doesNotThrow() {
    DoubleUpDownCounter counter =
        meter
            .upDownCounterBuilder("size")
            .ofDoubles()
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build();
    counter.add(-2e4);
    counter.add(1.0e-1, Attributes.of(stringKey("thing"), "car"));
    counter.add(1.0e-1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopLongHistogram_doesNotThrow() {
    LongHistogram histogram =
        meter
            .histogramBuilder("size")
            .ofLongs()
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build();
    histogram.record(-1);
    histogram.record(1, Attributes.of(stringKey("thing"), "car"));
    histogram.record(1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopDoubleHistogram_doesNotThrow() {
    DoubleHistogram histogram =
        meter
            .histogramBuilder("size")
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build();
    histogram.record(-2e4);
    histogram.record(1.0e-1, Attributes.of(stringKey("thing"), "car"));
    histogram.record(1.0e-1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopLongGauage_doesNotThrow() {
    LongGauge gauge =
        meter
            .gaugeBuilder("temperature")
            .ofLongs()
            .setDescription("The current temperature")
            .setUnit("C")
            .build();
    gauge.set(1);
    gauge.set(2, Attributes.of(stringKey("thing"), "engine"));
    gauge.set(2, Attributes.of(stringKey("thing"), "engine"), Context.current());

    ObservableLongMeasurement measurement =
        meter
            .gaugeBuilder("temperature")
            .ofLongs()
            .setDescription("The current temperature")
            .setUnit("C")
            .buildObserver();
    measurement.record(1);
    measurement.record(1, Attributes.of(stringKey("thing"), "engine"));
  }

  @Test
  void noopObservableLongGauage_doesNotThrow() {
    meter
        .gaugeBuilder("temperature")
        .ofLongs()
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.record(1);
              m.record(2, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  void noopDoubleGauage_doesNotThrow() {
    DoubleGauge gauge =
        meter
            .gaugeBuilder("temperature")
            .setDescription("The current temperature")
            .setUnit("C")
            .build();
    gauge.set(1);
    gauge.set(2, Attributes.of(stringKey("thing"), "engine"));
    gauge.set(2, Attributes.of(stringKey("thing"), "engine"), Context.current());

    ObservableDoubleMeasurement measurement =
        meter
            .gaugeBuilder("temperature")
            .setDescription("The current temperature")
            .setUnit("C")
            .buildObserver();
    measurement.record(1.0);
    measurement.record(1.0, Attributes.of(stringKey("thing"), "engine"));
  }

  @Test
  void noopObservableDoubleGauage_doesNotThrow() {
    meter
        .gaugeBuilder("temperature")
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.record(1.0e1);
              m.record(-27.4, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  void noopObservableLongCounter_doesNotThrow() {
    meter
        .counterBuilder("temperature")
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.record(1);
              m.record(2, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  void noopObservableDoubleCounter_doesNotThrow() {
    meter
        .counterBuilder("temperature")
        .ofDoubles()
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.record(1.0e1);
              m.record(-27.4, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  void noopObservableLongUpDownCounter_doesNotThrow() {
    meter
        .upDownCounterBuilder("temperature")
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.record(1);
              m.record(2, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  void noopObservableDoubleUpDownCounter_doesNotThrow() {
    meter
        .upDownCounterBuilder("temperature")
        .ofDoubles()
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.record(1.0e1);
              m.record(-27.4, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  @SuppressWarnings("NullAway")
  void noopBatchCallback_doesNotThrow() {
    meter.batchCallback(() -> {}, null);
  }
}
