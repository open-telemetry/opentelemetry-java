/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.metrics.SdkMeter.checkValidInstrumentName;
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
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(MetricStorageRegistry.class)
@SuppressLogger(SdkMeter.class)
class SdkMeterTest {

  private static final Meter NOOP_METER = MeterProvider.noop().get("noop");
  private static final String NOOP_INSTRUMENT_NAME = "noop";

  // Meter must have an exporter configured to actual run.
  private final SdkMeterProvider testMeterProvider =
      SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();

  @RegisterExtension
  LogCapturer metricStorageLogs = LogCapturer.create().captureForType(MetricStorageRegistry.class);

  @RegisterExtension LogCapturer sdkMeterLogs = LogCapturer.create().captureForType(SdkMeter.class);

  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());

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
  }

  @Test
  void checkValidInstrumentName_InvalidNameLogs() {
    assertThat(checkValidInstrumentName("1")).isFalse();
    sdkMeterLogs.assertContains(
        "Instrument name \"1\" is invalid, returning noop instrument. Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, /, and start with a letter.");
  }

  @Test
  void checkValidInstrumentNameTest() {
    // Valid names
    assertThat(checkValidInstrumentName("f")).isTrue();
    assertThat(checkValidInstrumentName("F")).isTrue();
    assertThat(checkValidInstrumentName("foo")).isTrue();
    assertThat(checkValidInstrumentName("a1")).isTrue();
    assertThat(checkValidInstrumentName("a.")).isTrue();
    assertThat(checkValidInstrumentName("abcdefghijklmnopqrstuvwxyz")).isTrue();
    assertThat(checkValidInstrumentName("ABCDEFGHIJKLMNOPQRSTUVWXYZ")).isTrue();
    assertThat(checkValidInstrumentName("a1234567890")).isTrue();
    assertThat(checkValidInstrumentName("a_-.")).isTrue();
    assertThat(checkValidInstrumentName(new String(new char[255]).replace('\0', 'a'))).isTrue();
    assertThat(checkValidInstrumentName("a/b")).isTrue();

    // Empty and null not allowed
    assertThat(checkValidInstrumentName(null)).isFalse();
    assertThat(checkValidInstrumentName("")).isFalse();
    // Must start with a letter
    assertThat(checkValidInstrumentName("1")).isFalse();
    assertThat(checkValidInstrumentName(".")).isFalse();
    // Illegal characters
    assertThat(checkValidInstrumentName("a~")).isFalse();
    assertThat(checkValidInstrumentName("a!")).isFalse();
    assertThat(checkValidInstrumentName("a@")).isFalse();
    assertThat(checkValidInstrumentName("a#")).isFalse();
    assertThat(checkValidInstrumentName("a$")).isFalse();
    assertThat(checkValidInstrumentName("a%")).isFalse();
    assertThat(checkValidInstrumentName("a^")).isFalse();
    assertThat(checkValidInstrumentName("a&")).isFalse();
    assertThat(checkValidInstrumentName("a*")).isFalse();
    assertThat(checkValidInstrumentName("a(")).isFalse();
    assertThat(checkValidInstrumentName("a)")).isFalse();
    assertThat(checkValidInstrumentName("a=")).isFalse();
    assertThat(checkValidInstrumentName("a+")).isFalse();
    assertThat(checkValidInstrumentName("a{")).isFalse();
    assertThat(checkValidInstrumentName("a}")).isFalse();
    assertThat(checkValidInstrumentName("a[")).isFalse();
    assertThat(checkValidInstrumentName("a]")).isFalse();
    assertThat(checkValidInstrumentName("a\\")).isFalse();
    assertThat(checkValidInstrumentName("a|")).isFalse();
    assertThat(checkValidInstrumentName("a<")).isFalse();
    assertThat(checkValidInstrumentName("a>")).isFalse();
    assertThat(checkValidInstrumentName("a?")).isFalse();
    // Must be 255 characters or fewer
    assertThat(checkValidInstrumentName(new String(new char[256]).replace('\0', 'a'))).isFalse();
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
    sdkMeter.counterBuilder("testLongCounter".toUpperCase(Locale.getDefault())).build();
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
    sdkMeter.upDownCounterBuilder("testLongUpDownCounter".toUpperCase(Locale.getDefault())).build();
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

    sdkMeter
        .histogramBuilder("testLongValueRecorder".toUpperCase(Locale.getDefault()))
        .ofLongs()
        .build();
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

    sdkMeter
        .gaugeBuilder("longValueObserver".toUpperCase(Locale.getDefault()))
        .ofLongs()
        .buildWithCallback(x -> {});
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

    sdkMeter
        .counterBuilder("testLongSumObserver".toUpperCase(Locale.getDefault()))
        .buildWithCallback(x -> {});
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
        .upDownCounterBuilder("testLongUpDownSumObserver".toUpperCase(Locale.getDefault()))
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
  void stringRepresentation() {
    assertThat(sdkMeter.toString())
        .isEqualTo(
            "SdkMeter{"
                + "instrumentationScopeInfo=InstrumentationScopeInfo{"
                + "name=io.opentelemetry.sdk.metrics.SdkMeterTest, "
                + "version=null, "
                + "schemaUrl=null, "
                + "attributes={}"
                + "}}");
  }

  @Test
  void updateEnabled() {
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
    SdkMeter meter = (SdkMeter) sdkMeterProvider.get("test");

    meter.updateMeterConfig(MeterConfig.disabled());
    assertThat(meter.isMeterEnabled()).isFalse();
    meter.updateMeterConfig(MeterConfig.enabled());
    assertThat(meter.isMeterEnabled()).isTrue();
  }
}
