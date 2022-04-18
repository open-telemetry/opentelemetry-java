/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.internal.ValidationUtil.API_USAGE_LOGGER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.LoggingEvent;

@SuppressLogger(loggerName = API_USAGE_LOGGER_NAME)
public class DefaultMeterTest {
  private static final Meter METER = DefaultMeter.getInstance();

  @RegisterExtension
  LogCapturer apiUsageLogs = LogCapturer.create().captureForLogger(API_USAGE_LOGGER_NAME);

  @Test
  void builder_InvalidUnit() {
    String unit = "日";
    // Counter
    METER.counterBuilder("my-instrument").setUnit(unit).build();
    METER.counterBuilder("my-instrument").setUnit(unit).buildWithCallback(unused -> {});
    METER.counterBuilder("my-instrument").setUnit(unit).ofDoubles().build();
    METER.counterBuilder("my-instrument").setUnit(unit).ofDoubles().buildWithCallback(unused -> {});

    // UpDownCounter
    METER.upDownCounterBuilder("my-instrument").setUnit(unit).build();
    METER.upDownCounterBuilder("my-instrument").setUnit(unit).buildWithCallback(unused -> {});
    METER.upDownCounterBuilder("my-instrument").setUnit(unit).ofDoubles().build();
    METER
        .upDownCounterBuilder("my-instrument")
        .setUnit(unit)
        .ofDoubles()
        .buildWithCallback(unused -> {});

    // Histogram
    METER.histogramBuilder("my-instrument").setUnit(unit).build();
    METER.histogramBuilder("my-instrument").setUnit(unit).ofLongs().build();

    // Gauge
    METER.gaugeBuilder("my-instrument").setUnit(unit).buildWithCallback(unused -> {});
    METER.gaugeBuilder("my-instrument").setUnit(unit).ofLongs().buildWithCallback(unused -> {});

    assertThat(apiUsageLogs.getEvents())
        .hasSize(12)
        .extracting(LoggingEvent::getMessage)
        .allMatch(
            log ->
                log.equals(
                    "Unit \"日\" is invalid. Instrument unit must be 63 or less ASCII characters."));
  }

  @Test
  void noopLongCounter_doesNotThrow() {
    LongCounter counter =
        METER.counterBuilder("size").setDescription("The size I'm measuring").setUnit("1").build();
    counter.add(1);
    counter.add(1, Attributes.of(stringKey("thing"), "car"));
    counter.add(1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopDoubleCounter_doesNotThrow() {
    DoubleCounter counter =
        METER
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
        METER
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
        METER
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
        METER
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
        METER
            .histogramBuilder("size")
            .setDescription("The size I'm measuring")
            .setUnit("1")
            .build();
    histogram.record(-2e4);
    histogram.record(1.0e-1, Attributes.of(stringKey("thing"), "car"));
    histogram.record(1.0e-1, Attributes.of(stringKey("thing"), "car"), Context.current());
  }

  @Test
  void noopObservableLongGauage_doesNotThrow() {
    METER
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
  void noopObservableDoubleGauage_doesNotThrow() {
    METER
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
    METER
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
    METER
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
    METER
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
    METER
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
