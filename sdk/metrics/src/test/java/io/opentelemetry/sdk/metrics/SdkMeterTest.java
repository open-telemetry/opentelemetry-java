/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.internal.ValidationUtil.API_USAGE_LOGGER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.LoggingEvent;

@SuppressLogger(loggerName = API_USAGE_LOGGER_NAME)
@SuppressLogger(MetricStorageRegistry.class)
class SdkMeterTest {

  private static final Meter NOOP_METER = MeterProvider.noop().get("noop");
  private static final String NOOP_INSTRUMENT_NAME = "noop";

  // Meter must have an exporter configured to actual run.
  private final SdkMeterProvider testMeterProvider =
      SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(MetricStorageRegistry.class);

  @RegisterExtension
  LogCapturer apiUsageLogs = LogCapturer.create().captureForLogger(API_USAGE_LOGGER_NAME);

  @Test
  void builder_InvalidName() {
    // Counter
    assertThat(sdkMeter.counterBuilder("1").build())
        .isSameAs(NOOP_METER.counterBuilder(NOOP_INSTRUMENT_NAME).build());
    assertThat(sdkMeter.counterBuilder("1").ofDoubles().build())
        .isSameAs(NOOP_METER.counterBuilder(NOOP_INSTRUMENT_NAME).ofDoubles().build());
    assertThat(sdkMeter.counterBuilder("1").buildWithCallback(unused -> {}))
        .isSameAs(NOOP_METER.counterBuilder(NOOP_INSTRUMENT_NAME).buildWithCallback(unused -> {}));
    assertThat(sdkMeter.counterBuilder("1").ofDoubles().buildWithCallback(unused -> {}))
        .isSameAs(
            NOOP_METER
                .counterBuilder(NOOP_INSTRUMENT_NAME)
                .ofDoubles()
                .buildWithCallback(unused -> {}));

    // UpDownCounter
    assertThat(sdkMeter.upDownCounterBuilder("1").build())
        .isSameAs(NOOP_METER.upDownCounterBuilder(NOOP_INSTRUMENT_NAME).build());
    assertThat(sdkMeter.upDownCounterBuilder("1").ofDoubles().build())
        .isSameAs(NOOP_METER.upDownCounterBuilder(NOOP_INSTRUMENT_NAME).ofDoubles().build());
    assertThat(sdkMeter.upDownCounterBuilder("1").buildWithCallback(unused -> {}))
        .isSameAs(
            NOOP_METER.upDownCounterBuilder(NOOP_INSTRUMENT_NAME).buildWithCallback(unused -> {}));
    assertThat(sdkMeter.upDownCounterBuilder("1").ofDoubles().buildWithCallback(unused -> {}))
        .isSameAs(
            NOOP_METER
                .upDownCounterBuilder(NOOP_INSTRUMENT_NAME)
                .ofDoubles()
                .buildWithCallback(unused -> {}));

    // Histogram
    assertThat(sdkMeter.histogramBuilder("1").build())
        .isSameAs(NOOP_METER.histogramBuilder(NOOP_INSTRUMENT_NAME).build());
    assertThat(sdkMeter.histogramBuilder("1").ofLongs().build())
        .isSameAs(NOOP_METER.histogramBuilder(NOOP_INSTRUMENT_NAME).ofLongs().build());

    // Gauage
    assertThat(sdkMeter.gaugeBuilder("1").buildWithCallback(unused -> {}))
        .isSameAs(NOOP_METER.gaugeBuilder(NOOP_INSTRUMENT_NAME).buildWithCallback(unused -> {}));
    assertThat(sdkMeter.gaugeBuilder("1").ofLongs().buildWithCallback(unused -> {}))
        .isSameAs(
            NOOP_METER
                .gaugeBuilder(NOOP_INSTRUMENT_NAME)
                .ofLongs()
                .buildWithCallback(unused -> {}));

    assertThat(apiUsageLogs.getEvents())
        .extracting(LoggingEvent::getMessage)
        .hasSize(12)
        .allMatch(
            log ->
                log.equals(
                    "Instrument name \"1\" is invalid, returning noop instrument. Instrument names must consist of 63 or less characters including alphanumeric, _, ., -, and start with a letter. Returning noop instrument."));
  }

  @Test
  void builder_InvalidUnit() {
    String unit = "日";
    // Counter
    sdkMeter.counterBuilder("my-instrument").setUnit(unit).build();
    sdkMeter.counterBuilder("my-instrument").setUnit(unit).buildWithCallback(unused -> {});
    sdkMeter.counterBuilder("my-instrument").setUnit(unit).ofDoubles().build();
    sdkMeter
        .counterBuilder("my-instrument")
        .setUnit(unit)
        .ofDoubles()
        .buildWithCallback(unused -> {});

    // UpDownCounter
    sdkMeter.upDownCounterBuilder("my-instrument").setUnit(unit).build();
    sdkMeter.upDownCounterBuilder("my-instrument").setUnit(unit).buildWithCallback(unused -> {});
    sdkMeter.upDownCounterBuilder("my-instrument").setUnit(unit).ofDoubles().build();
    sdkMeter
        .upDownCounterBuilder("my-instrument")
        .setUnit(unit)
        .ofDoubles()
        .buildWithCallback(unused -> {});

    // Histogram
    sdkMeter.histogramBuilder("my-instrument").setUnit(unit).build();
    sdkMeter.histogramBuilder("my-instrument").setUnit(unit).ofLongs().build();

    // Gauge
    sdkMeter.gaugeBuilder("my-instrument").setUnit(unit).buildWithCallback(unused -> {});
    sdkMeter.gaugeBuilder("my-instrument").setUnit(unit).ofLongs().buildWithCallback(unused -> {});

    assertThat(apiUsageLogs.getEvents())
        .hasSize(12)
        .extracting(LoggingEvent::getMessage)
        .allMatch(
            log ->
                log.equals(
                    "Unit \"日\" is invalid. Instrument unit must be 63 or less ASCII characters. Using \"\" for instrument my-instrument instead."));
  }

  @Test
  void testLongCounter() {
    LongCounter longCounter =
        sdkMeter
            .counterBuilder("testLongCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longCounter).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't log a warning.
    sdkMeter
        .counterBuilder("testLongCounter")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testLongCounter").build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongCounter_upperCaseConflict() {
    LongCounter longCounter =
        sdkMeter
            .counterBuilder("testLongCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longCounter).isNotNull();
    sdkMeter.counterBuilder("testLongCounter".toUpperCase()).build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongUpDownCounter() {
    LongUpDownCounter longUpDownCounter =
        sdkMeter
            .upDownCounterBuilder("testLongUpDownCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longUpDownCounter).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't throw to grab
    // a second instance.
    sdkMeter
        .upDownCounterBuilder("testLongUpDownCounter")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.upDownCounterBuilder("testLongUpDownCounter").build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongUpDownCounter_upperCaseConflict() {
    LongUpDownCounter longUpDownCounter =
        sdkMeter
            .upDownCounterBuilder("testLongUpDownCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longUpDownCounter).isNotNull();
    assertThat(logs.getEvents()).isEmpty();
    sdkMeter.upDownCounterBuilder("testLongUpDownCounter".toUpperCase()).build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongHistogram() {
    LongHistogram longHistogram =
        sdkMeter
            .histogramBuilder("testLongValueRecorder")
            .ofLongs()
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longHistogram).isNotNull();
    assertThat(logs.getEvents()).isEmpty();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't log an error.
    sdkMeter
        .histogramBuilder("testLongValueRecorder")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.histogramBuilder("testLongValueRecorder").ofLongs().build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongHistogram_upperCaseConflict() {
    LongHistogram longHistogram =
        sdkMeter
            .histogramBuilder("testLongValueRecorder")
            .ofLongs()
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longHistogram).isNotNull();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.histogramBuilder("testLongValueRecorder".toUpperCase()).ofLongs().build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongGauge_conflicts() {
    sdkMeter
        .gaugeBuilder("longValueObserver")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(obs -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.gaugeBuilder("longValueObserver").ofLongs().buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongGauge_upperCaseConflicts() {
    sdkMeter
        .gaugeBuilder("longValueObserver")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(obs -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.gaugeBuilder("longValueObserver".toUpperCase()).ofLongs().buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongSumObserver_conflicts() {
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testLongSumObserver").buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongSumObserver_upperCaseConflicts() {
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testLongSumObserver".toUpperCase()).buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongUpDownSumObserver_conflicts() {
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.upDownCounterBuilder("testLongUpDownSumObserver").buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongUpDownSumObserver_upperCaseConflicts() {
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver".toUpperCase())
        .buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleCounter() {
    DoubleCounter doubleCounter =
        sdkMeter
            .counterBuilder("testDoubleCounter")
            .ofDoubles()
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleCounter).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't log an error.
    sdkMeter
        .counterBuilder("testDoubleCounter")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testDoubleCounter").ofDoubles().build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleUpDownCounter() {
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter
            .upDownCounterBuilder("testDoubleUpDownCounter")
            .ofDoubles()
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleUpDownCounter).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't log an error.
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownCounter")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.upDownCounterBuilder("testDoubleUpDownCounter").ofDoubles().build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleHistogram() {
    DoubleHistogram doubleValueRecorder =
        sdkMeter
            .histogramBuilder("testDoubleValueRecorder")
            .setDescription("My very own ValueRecorder")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueRecorder).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't log an error
    sdkMeter
        .histogramBuilder("testDoubleValueRecorder")
        .setDescription("My very own ValueRecorder")
        .setUnit("metric tonnes")
        .build();
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleSumObserver() {
    sdkMeter
        .counterBuilder("testDoubleSumObserver")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();
    sdkMeter.counterBuilder("testDoubleSumObserver").ofDoubles().buildWithCallback(x -> {});
    sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleUpDownSumObserver() {
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .buildWithCallback(x -> {});
    sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    logs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleGauge() {
    sdkMeter
        .gaugeBuilder("doubleValueObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(logs.getEvents()).isEmpty();

    sdkMeter.gaugeBuilder("doubleValueObserver").buildWithCallback(x -> {});
    logs.assertContains("Found duplicate metric definition");
  }
}
