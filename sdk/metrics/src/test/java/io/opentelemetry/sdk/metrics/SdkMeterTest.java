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
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(SdkMeter.class)
@SuppressLogger(MetricStorageRegistry.class)
class SdkMeterTest {
  // Meter must have an exporter configured to actual run.
  private final SdkMeterProvider testMeterProvider =
      SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());
  private final Meter noopMeter = MeterProvider.noop().get("");

  @RegisterExtension LogCapturer sdkMeterLogs = LogCapturer.create().captureForType(SdkMeter.class);

  @RegisterExtension
  LogCapturer metricStorageLogs = LogCapturer.create().captureForType(MetricStorageRegistry.class);

  @Test
  void counterBuilder_InvalidName() {
    assertThat(sdkMeter.counterBuilder("1").build())
        .isSameAs(noopMeter.counterBuilder("1").build());
    assertThat(sdkMeter.counterBuilder("1").ofDoubles().build())
        .isSameAs(noopMeter.counterBuilder("1").ofDoubles().build());
    assertThat(sdkMeter.counterBuilder("1").buildWithCallback(unused -> {}))
        .isSameAs(noopMeter.counterBuilder("1").buildWithCallback(unused -> {}));
    assertThat(sdkMeter.counterBuilder("1").ofDoubles().buildWithCallback(unused -> {}))
        .isSameAs(noopMeter.counterBuilder("1").ofDoubles().buildWithCallback(unused -> {}));
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
  void upDownCounterBuilder_InvalidName() {
    assertThat(sdkMeter.upDownCounterBuilder("1").build())
        .isSameAs(noopMeter.upDownCounterBuilder("1").build());
    assertThat(sdkMeter.upDownCounterBuilder("1").ofDoubles().build())
        .isSameAs(noopMeter.upDownCounterBuilder("1").ofDoubles().build());
    assertThat(sdkMeter.upDownCounterBuilder("1").buildWithCallback(unused -> {}))
        .isSameAs(noopMeter.upDownCounterBuilder("1").buildWithCallback(unused -> {}));
    assertThat(sdkMeter.upDownCounterBuilder("1").ofDoubles().buildWithCallback(unused -> {}))
        .isSameAs(noopMeter.upDownCounterBuilder("1").ofDoubles().buildWithCallback(unused -> {}));
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
  void histogramBuilder_InvalidName() {
    assertThat(sdkMeter.histogramBuilder("1").build())
        .isSameAs(noopMeter.histogramBuilder("1").build());
    assertThat(sdkMeter.histogramBuilder("1").ofLongs().build())
        .isSameAs(noopMeter.histogramBuilder("1").ofLongs().build());
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
  void gaugeBuilder_InvalidName() {
    assertThat(sdkMeter.gaugeBuilder("1").buildWithCallback(unused -> {}))
        .isSameAs(noopMeter.gaugeBuilder("1").buildWithCallback(unused -> {}));
    assertThat(sdkMeter.gaugeBuilder("1").ofLongs().buildWithCallback(unused -> {}))
        .isSameAs(noopMeter.gaugeBuilder("1").ofLongs().buildWithCallback(unused -> {}));
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
  void isValidName_InvalidNameLogs() {
    assertThat(SdkMeter.isValidName("1")).isFalse();
    sdkMeterLogs.assertContains(
        "Instrument names \"1\" is invalid, returning noop instrument. Valid instrument names must match ([A-Za-z]){1}([A-Za-z0-9\\_\\-\\.]){0,62}.");
  }

  @Test
  void isValidName() {
    // Valid names
    assertThat(SdkMeter.isValidName("f")).isTrue();
    assertThat(SdkMeter.isValidName("F")).isTrue();
    assertThat(SdkMeter.isValidName("foo")).isTrue();
    assertThat(SdkMeter.isValidName("a1")).isTrue();
    assertThat(SdkMeter.isValidName("a.")).isTrue();
    assertThat(SdkMeter.isValidName("abcdefghijklmnopqrstuvwxyz")).isTrue();
    assertThat(SdkMeter.isValidName("ABCDEFGHIJKLMNOPQRSTUVWXYZ")).isTrue();
    assertThat(SdkMeter.isValidName("a1234567890")).isTrue();
    assertThat(SdkMeter.isValidName("a_-.")).isTrue();
    assertThat(SdkMeter.isValidName(new String(new char[63]).replace('\0', 'a'))).isTrue();

    // Empty and null not allowed
    assertThat(SdkMeter.isValidName(null)).isFalse();
    assertThat(SdkMeter.isValidName("")).isFalse();
    // Must start with a letter
    assertThat(SdkMeter.isValidName("1")).isFalse();
    assertThat(SdkMeter.isValidName(".")).isFalse();
    // Illegal characters
    assertThat(SdkMeter.isValidName("a~")).isFalse();
    assertThat(SdkMeter.isValidName("a!")).isFalse();
    assertThat(SdkMeter.isValidName("a@")).isFalse();
    assertThat(SdkMeter.isValidName("a#")).isFalse();
    assertThat(SdkMeter.isValidName("a$")).isFalse();
    assertThat(SdkMeter.isValidName("a%")).isFalse();
    assertThat(SdkMeter.isValidName("a^")).isFalse();
    assertThat(SdkMeter.isValidName("a&")).isFalse();
    assertThat(SdkMeter.isValidName("a*")).isFalse();
    assertThat(SdkMeter.isValidName("a(")).isFalse();
    assertThat(SdkMeter.isValidName("a)")).isFalse();
    assertThat(SdkMeter.isValidName("a=")).isFalse();
    assertThat(SdkMeter.isValidName("a+")).isFalse();
    assertThat(SdkMeter.isValidName("a{")).isFalse();
    assertThat(SdkMeter.isValidName("a}")).isFalse();
    assertThat(SdkMeter.isValidName("a[")).isFalse();
    assertThat(SdkMeter.isValidName("a]")).isFalse();
    assertThat(SdkMeter.isValidName("a\\")).isFalse();
    assertThat(SdkMeter.isValidName("a|")).isFalse();
    assertThat(SdkMeter.isValidName("a<")).isFalse();
    assertThat(SdkMeter.isValidName("a>")).isFalse();
    assertThat(SdkMeter.isValidName("a?")).isFalse();
    // Must be 63 characters or less
    assertThat(SdkMeter.isValidName(new String(new char[64]).replace('\0', 'a'))).isFalse();
  }
}
