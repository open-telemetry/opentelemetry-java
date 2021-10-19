/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.event.Level.WARN;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SdkMeterTest {
  // Meter must have an exporter configured to actual run.
  private final SdkMeterProvider testMeterProvider =
      SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(MeterSharedState.class);

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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
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
    assertThat(
            logs.assertContains(
                    loggingEvent -> loggingEvent.getLevel().equals(WARN),
                    "Failed to register metric.")
                .getThrowable())
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }
}
