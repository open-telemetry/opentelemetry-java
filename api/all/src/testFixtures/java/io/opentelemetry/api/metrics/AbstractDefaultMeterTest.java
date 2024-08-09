/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.Test;

@SuppressLogger()
public abstract class AbstractDefaultMeterTest {
  private final Meter meter = getMeter();

  protected abstract Meter getMeter();

  protected abstract MeterProvider getMeterProvider();

  @Test
  public void noopMeterProvider_getDoesNotThrow() {
    MeterProvider provider = getMeterProvider();
    provider.get("user-instrumentation");
  }

  @Test
  public void noopMeterProvider_builderDoesNotThrow() {
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
  public void noopLongCounter_doesNotThrow() {
    LongCounter counter =
        meter.counterBuilder("size").setDescription("The size I'm measuring").setUnit("1").build();
    counter.add(1);
    counter.add(1, Attributes.of(stringKey("thing"), "car"));
    counter.add(1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  public void noopDoubleCounter_doesNotThrow() {
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
  public void noopLongUpDownCounter_doesNotThrow() {
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
  public void noopDoubleUpDownCounter_doesNotThrow() {
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
  public void noopLongHistogram_doesNotThrow() {
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
  public void noopDoubleHistogram_doesNotThrow() {
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
  public void noopLongGauage_doesNotThrow() {
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
  }

  @Test
  public void noopObservableLongGauage_doesNotThrow() {
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
  }

  @Test
  public void noopObservableDoubleGauage_doesNotThrow() {
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
  public void noopObservableLongCounter_doesNotThrow() {
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
  public void noopObservableDoubleCounter_doesNotThrow() {
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
  public void noopObservableLongUpDownCounter_doesNotThrow() {
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
  public void noopObservableDoubleUpDownCounter_doesNotThrow() {
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
}
