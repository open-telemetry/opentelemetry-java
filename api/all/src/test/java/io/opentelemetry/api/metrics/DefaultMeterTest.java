/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

public class DefaultMeterTest {
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void noopLongCounter_doesNotThrow() {
    LongCounter counter =
        meter.counterBuilder("size").setDescription("The size I'm measuring").setUnit("1").build();
    counter.add(1);
    counter.add(1, Attributes.of(stringKey("thing"), "car"));
    counter.add(1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopBoundLongCounter_doesNotThrow() {
    BoundLongCounter counter =
        meter
            .counterBuilder("size")
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build()
            .bind(Attributes.of(stringKey("thing"), "car"));
    counter.add(1);
    counter.add(1, Context.current());
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
  void noopBoundDoubleCounter_doesNotThrow() {
    BoundDoubleCounter counter =
        meter
            .counterBuilder("size")
            .ofDoubles()
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build()
            .bind(Attributes.of(stringKey("thing"), "car"));
    counter.add(1.2);
    counter.add(2.5, Context.current());
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
  void noopBoundLongUpDownCounter_doesNotThrow() {
    BoundLongUpDownCounter counter =
        meter
            .upDownCounterBuilder("size")
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build()
            .bind(Attributes.of(stringKey("thing"), "car"));
    counter.add(-1);
    counter.add(1, Context.current());
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
  void noopBoundDoubleUpDownCounter_doesNotThrow() {
    BoundDoubleUpDownCounter counter =
        meter
            .upDownCounterBuilder("size")
            .ofDoubles()
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build()
            .bind(Attributes.of(stringKey("thing"), "car"));
    counter.add(-2e4);
    counter.add(1.0e-1, Context.current());
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
  void noopBoundLongHistogram_doesNotThrow() {
    BoundLongHistogram histogram =
        meter
            .histogramBuilder("size")
            .ofLongs()
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build()
            .bind(Attributes.of(stringKey("thing"), "car"));
    histogram.record(-1);
    histogram.record(1, Context.current());
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
  void noopBoundDoubleHistogram_doesNotThrow() {
    BoundDoubleHistogram histogram =
        meter
            .histogramBuilder("size")
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build()
            .bind(Attributes.of(stringKey("thing"), "car"));
    histogram.record(-2e4);
    histogram.record(1.0e-1, Context.current());
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
              m.observe(1);
              m.observe(2, Attributes.of(stringKey("thing"), "engine"));
            });
  }

  @Test
  void noopObservableDoubleGauage_doesNotThrow() {
    meter
        .gaugeBuilder("temperature")
        .setDescription("The current temperature")
        .setUnit("C")
        .buildWithCallback(
            m -> {
              m.observe(1.0e1);
              m.observe(-27.4, Attributes.of(stringKey("thing"), "engine"));
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
              m.observe(1);
              m.observe(2, Attributes.of(stringKey("thing"), "engine"));
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
              m.observe(1.0e1);
              m.observe(-27.4, Attributes.of(stringKey("thing"), "engine"));
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
              m.observe(1);
              m.observe(2, Attributes.of(stringKey("thing"), "engine"));
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
              m.observe(1.0e1);
              m.observe(-27.4, Attributes.of(stringKey("thing"), "engine"));
            });
  }
}
