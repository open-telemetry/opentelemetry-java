/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleSumObserver;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownSumObserver;
import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongSumObserver;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownSumObserver;
import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import org.junit.jupiter.api.Test;

class SdkMeterTest {
  private final SdkMeterProvider testMeterProvider = SdkMeterProvider.builder().build();
  private final Meter sdkMeter = testMeterProvider.get(getClass().getName());

  @Test
  void testLongCounter() {
    LongCounter longCounter =
        sdkMeter
            .longCounterBuilder("testLongCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longCounter).isNotNull();

    assertThat(
            sdkMeter
                .longCounterBuilder("testLongCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longCounter);

    assertThatThrownBy(() -> sdkMeter.longCounterBuilder("testLongCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(() -> sdkMeter.longCounterBuilder("testLongCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownCounter() {
    LongUpDownCounter longUpDownCounter =
        sdkMeter
            .longUpDownCounterBuilder("testLongUpDownCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longUpDownCounter).isNotNull();

    assertThat(
            sdkMeter
                .longUpDownCounterBuilder("testLongUpDownCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longUpDownCounter);

    assertThatThrownBy(() -> sdkMeter.longUpDownCounterBuilder("testLongUpDownCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.longUpDownCounterBuilder("testLongUpDownCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongValueRecorder() {
    LongValueRecorder longValueRecorder =
        sdkMeter
            .longValueRecorderBuilder("testLongValueRecorder")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longValueRecorder).isNotNull();

    assertThat(
            sdkMeter
                .longValueRecorderBuilder("testLongValueRecorder")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longValueRecorder);

    assertThatThrownBy(() -> sdkMeter.longValueRecorderBuilder("testLongValueRecorder").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.longValueRecorderBuilder("testLongValueRecorder".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongValueObserver() {
    LongValueObserver longValueObserver =
        sdkMeter
            .longValueObserverBuilder("longValueObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longValueObserver).isNotNull();

    assertThat(
            sdkMeter
                .longValueObserverBuilder("longValueObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longValueObserver);

    assertThatThrownBy(() -> sdkMeter.longValueObserverBuilder("longValueObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.longValueObserverBuilder("longValueObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongSumObserver() {
    LongSumObserver longObserver =
        sdkMeter
            .longSumObserverBuilder("testLongSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            sdkMeter
                .longSumObserverBuilder("testLongSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longObserver);

    assertThatThrownBy(() -> sdkMeter.longSumObserverBuilder("testLongSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");

    assertThatThrownBy(
            () -> sdkMeter.longSumObserverBuilder("testLongSumObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownSumObserver() {
    LongUpDownSumObserver longObserver =
        sdkMeter
            .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            sdkMeter
                .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longObserver);

    assertThatThrownBy(
            () -> sdkMeter.longUpDownSumObserverBuilder("testLongUpDownSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");

    assertThatThrownBy(
            () ->
                sdkMeter
                    .longUpDownSumObserverBuilder("testLongUpDownSumObserver".toUpperCase())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleCounter() {
    DoubleCounter doubleCounter =
        sdkMeter
            .doubleCounterBuilder("testDoubleCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleCounter).isNotNull();

    assertThat(
            sdkMeter
                .doubleCounterBuilder("testDoubleCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleCounter);

    assertThatThrownBy(() -> sdkMeter.doubleCounterBuilder("testDoubleCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.doubleCounterBuilder("testDoubleCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleUpDownCounter() {
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter
            .doubleUpDownCounterBuilder("testDoubleUpDownCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleUpDownCounter).isNotNull();

    assertThat(
            sdkMeter
                .doubleUpDownCounterBuilder("testDoubleUpDownCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleUpDownCounter);

    assertThatThrownBy(() -> sdkMeter.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .doubleUpDownCounterBuilder("testDoubleUpDownCounter".toUpperCase())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueRecorder() {
    DoubleValueRecorder doubleValueRecorder =
        sdkMeter
            .doubleValueRecorderBuilder("testDoubleValueRecorder")
            .setDescription("My very own ValueRecorder")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueRecorder).isNotNull();

    assertThat(
            sdkMeter
                .doubleValueRecorderBuilder("testDoubleValueRecorder")
                .setDescription("My very own ValueRecorder")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleValueRecorder);

    assertThatThrownBy(() -> sdkMeter.doubleValueRecorderBuilder("testDoubleValueRecorder").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .doubleValueRecorderBuilder("testDoubleValueRecorder".toUpperCase())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleSumObserver() {
    DoubleSumObserver doubleObserver =
        sdkMeter
            .doubleSumObserverBuilder("testDoubleSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            sdkMeter
                .doubleSumObserverBuilder("testDoubleSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleObserver);

    assertThatThrownBy(() -> sdkMeter.doubleSumObserverBuilder("testDoubleSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.doubleSumObserverBuilder("testDoubleSumObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleUpDownSumObserver() {
    DoubleUpDownSumObserver doubleObserver =
        sdkMeter
            .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            sdkMeter
                .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleObserver);

    assertThatThrownBy(
            () -> sdkMeter.doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                sdkMeter
                    .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver".toUpperCase())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueObserver() {
    DoubleValueObserver doubleValueObserver =
        sdkMeter
            .doubleValueObserverBuilder("doubleValueObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueObserver).isNotNull();

    assertThat(
            sdkMeter
                .doubleValueObserverBuilder("doubleValueObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleValueObserver);

    assertThatThrownBy(() -> sdkMeter.doubleValueObserverBuilder("doubleValueObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> sdkMeter.doubleValueObserverBuilder("doubleValueObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testBatchRecorder() {
    BatchRecorder batchRecorder = sdkMeter.newBatchRecorder("key", "value");
    assertThat(batchRecorder).isNotNull();
    assertThat(batchRecorder).isInstanceOf(BatchRecorderSdk.class);
  }
}
