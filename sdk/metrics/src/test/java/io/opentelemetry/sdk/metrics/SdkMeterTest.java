/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(AbstractInstrumentBuilder.class)
@SuppressLogger(MetricStorageRegistry.class)
class SdkMeterTest {
  // Meter must have an exporter configured to actual run.
  private final SdkMeterProvider testMeterProvider =
      SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());

  @RegisterExtension
  LogCapturer metricStorageLogs = LogCapturer.create().captureForType(MetricStorageRegistry.class);

  @RegisterExtension
  LogCapturer instrumentBuilderLogs =
      LogCapturer.create().captureForType(AbstractInstrumentBuilder.class);

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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testLongCounter").build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.upDownCounterBuilder("testLongUpDownCounter").build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();
    sdkMeter.upDownCounterBuilder("testLongUpDownCounter".toUpperCase()).build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't log an error.
    sdkMeter
        .histogramBuilder("testLongValueRecorder")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.histogramBuilder("testLongValueRecorder").ofLongs().build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.histogramBuilder("testLongValueRecorder".toUpperCase()).ofLongs().build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongGauge_conflicts() {
    sdkMeter
        .gaugeBuilder("longValueObserver")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(obs -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.gaugeBuilder("longValueObserver").ofLongs().buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongGauge_upperCaseConflicts() {
    sdkMeter
        .gaugeBuilder("longValueObserver")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(obs -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.gaugeBuilder("longValueObserver".toUpperCase()).ofLongs().buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongSumObserver_conflicts() {
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testLongSumObserver").buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongSumObserver_upperCaseConflicts() {
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testLongSumObserver".toUpperCase()).buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongUpDownSumObserver_conflicts() {
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.upDownCounterBuilder("testLongUpDownSumObserver").buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testLongUpDownSumObserver_upperCaseConflicts() {
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver".toUpperCase())
        .buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.counterBuilder("testDoubleCounter").ofDoubles().build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.upDownCounterBuilder("testDoubleUpDownCounter").ofDoubles().build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
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
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleSumObserver() {
    sdkMeter
        .counterBuilder("testDoubleSumObserver")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();
    sdkMeter.counterBuilder("testDoubleSumObserver").ofDoubles().buildWithCallback(x -> {});
    sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleUpDownSumObserver() {
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .buildWithCallback(x -> {});
    sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void testDoubleGauge() {
    sdkMeter
        .gaugeBuilder("doubleValueObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});
    assertThat(metricStorageLogs.getEvents()).isEmpty();

    sdkMeter.gaugeBuilder("doubleValueObserver").buildWithCallback(x -> {});
    metricStorageLogs.assertContains("Found duplicate metric definition");
  }

  @Test
  void build_InvalidUnitLogs() {
    String unit = "日";
    sdkMeter.counterBuilder("name").setUnit(unit).build();
    sdkMeter.counterBuilder("name").setUnit(unit).buildWithCallback(unused -> {});
    sdkMeter.counterBuilder("name").setUnit(unit).ofDoubles().build();
    sdkMeter.counterBuilder("name").setUnit(unit).ofDoubles().buildWithCallback(unused -> {});
    sdkMeter.upDownCounterBuilder("name").setUnit(unit).build();
    sdkMeter.upDownCounterBuilder("name").setUnit(unit).buildWithCallback(unused -> {});
    sdkMeter.upDownCounterBuilder("name").setUnit(unit).ofDoubles().build();
    sdkMeter.upDownCounterBuilder("name").setUnit(unit).ofDoubles().buildWithCallback(unused -> {});
    sdkMeter.histogramBuilder("name").setUnit(unit).build();
    sdkMeter.histogramBuilder("name").setUnit(unit).ofLongs().build();
    sdkMeter.gaugeBuilder("name").setUnit(unit).buildWithCallback(unused -> {});
    sdkMeter.gaugeBuilder("name").setUnit(unit).ofLongs().buildWithCallback(unused -> {});

    assertThat(instrumentBuilderLogs.getEvents())
        .hasSize(12)
        .allSatisfy(
            log ->
                assertThat(log.getMessage())
                    .isEqualTo(
                        "Instrument name unit \"日\" is invalid, using 1 instead. Instrument unit must be 63 or less ASCII characters."));
  }

  @Test
  void isValidUnit() {
    assertThat(AbstractInstrumentBuilder.isValidUnit("a")).isTrue();
    assertThat(AbstractInstrumentBuilder.isValidUnit("A")).isTrue();
    assertThat(AbstractInstrumentBuilder.isValidUnit("foo129")).isTrue();
    assertThat(AbstractInstrumentBuilder.isValidUnit("!@#$%^&*()")).isTrue();
    assertThat(AbstractInstrumentBuilder.isValidUnit(new String(new char[63]).replace('\0', 'a')))
        .isTrue();

    // Empty and null not allowed
    assertThat(AbstractInstrumentBuilder.isValidUnit(null)).isFalse();
    assertThat(AbstractInstrumentBuilder.isValidUnit("")).isFalse();
    // Non-ascii characters
    assertThat(AbstractInstrumentBuilder.isValidUnit("日")).isFalse();
    // Must be 63 characters or less
    assertThat(AbstractInstrumentBuilder.isValidUnit(new String(new char[64]).replace('\0', 'a')))
        .isFalse();
  }
}
