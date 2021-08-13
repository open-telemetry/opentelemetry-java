/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import org.junit.jupiter.api.Test;

class SdkMeterTest {
  private final SdkMeterProvider testMeterProvider = SdkMeterProvider.builder().build();
  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());

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
    // objects backed by storage now.  Here we just make sure it doesn't throw to grab
    // a second instance.
    sdkMeter
        .counterBuilder("testLongCounter")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();

    assertThatThrownBy(() -> sdkMeter.counterBuilder("testLongCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(() -> sdkMeter.counterBuilder("testLongCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
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

    assertThatThrownBy(() -> sdkMeter.upDownCounterBuilder("testLongUpDownCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.upDownCounterBuilder("testLongUpDownCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void testLongValueRecorder() {
    LongHistogram longValueRecorder =
        sdkMeter
            .histogramBuilder("testLongValueRecorder")
            .ofLongs()
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longValueRecorder).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't throw to grab
    // a second instance.
    sdkMeter
        .histogramBuilder("testLongValueRecorder")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();

    assertThatThrownBy(() -> sdkMeter.histogramBuilder("testLongValueRecorder").ofLongs().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter.histogramBuilder("testLongValueRecorder".toUpperCase()).ofLongs().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void testLongValueObserver() {
    sdkMeter
        .gaugeBuilder("longValueObserver")
        .ofLongs()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(obs -> {});

    assertThatThrownBy(
            () -> sdkMeter.gaugeBuilder("longValueObserver").ofLongs().buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .gaugeBuilder("longValueObserver".toUpperCase())
                    .ofLongs()
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void testLongSumObserver() {
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});

    assertThatThrownBy(
            () -> sdkMeter.counterBuilder("testLongSumObserver").buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");

    assertThatThrownBy(
            () ->
                sdkMeter
                    .counterBuilder("testLongSumObserver".toUpperCase())
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownSumObserver() {
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});

    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testLongUpDownSumObserver")
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");

    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testLongUpDownSumObserver".toUpperCase())
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
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
    // objects backed by storage now.  Here we just make sure it doesn't throw to grab
    // a second instance.
    sdkMeter
        .counterBuilder("testDoubleCounter")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();

    assertThatThrownBy(() -> sdkMeter.counterBuilder("testDoubleCounter").ofDoubles().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.counterBuilder("testDoubleCounter".toUpperCase()).ofDoubles().build())
        .isInstanceOf(IllegalArgumentException.class)
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
    // objects backed by storage now.  Here we just make sure it doesn't throw to grab
    // a second instance.
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownCounter")
        .ofDoubles()
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .build();

    assertThatThrownBy(
            () -> sdkMeter.upDownCounterBuilder("testDoubleUpDownCounter").ofDoubles().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testDoubleUpDownCounter".toUpperCase())
                    .ofDoubles()
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueRecorder() {
    DoubleHistogram doubleValueRecorder =
        sdkMeter
            .histogramBuilder("testDoubleValueRecorder")
            .setDescription("My very own ValueRecorder")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueRecorder).isNotNull();

    // Note: We no longer get the same instrument instance as these instances are lightweight
    // objects backed by storage now.  Here we just make sure it doesn't throw to grab
    // a second instance.
    sdkMeter
        .histogramBuilder("testDoubleValueRecorder")
        .setDescription("My very own ValueRecorder")
        .setUnit("metric tonnes")
        .build();

    assertThatThrownBy(() -> sdkMeter.histogramBuilder("testDoubleValueRecorder").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.histogramBuilder("testDoubleValueRecorder".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
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

    assertThatThrownBy(
            () ->
                sdkMeter
                    .counterBuilder("testDoubleSumObserver")
                    .ofDoubles()
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .counterBuilder("testDoubleSumObserver".toUpperCase())
                    .ofDoubles()
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
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

    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testDoubleUpDownSumObserver")
                    .ofDoubles()
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testDoubleUpDownSumObserver".toUpperCase())
                    .ofDoubles()
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueObserver() {
    sdkMeter
        .gaugeBuilder("doubleValueObserver")
        .setDescription("My very own counter")
        .setUnit("metric tonnes")
        .buildWithCallback(x -> {});

    assertThatThrownBy(
            () -> sdkMeter.gaugeBuilder("doubleValueObserver").buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .gaugeBuilder("doubleValueObserver".toUpperCase())
                    .buildWithCallback(x -> {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }
}
