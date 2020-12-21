/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.DoubleSummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkMeter}. */
class SdkMeterTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.MeterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final SdkMeter testSdk =
      new SdkMeter(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  void testLongCounter() {
    LongCounterSdk longCounter =
        testSdk
            .longCounterBuilder("testLongCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longCounter).isNotNull();

    assertThat(
            testSdk
                .longCounterBuilder("testLongCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longCounter);

    assertThatThrownBy(() -> testSdk.longCounterBuilder("testLongCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(() -> testSdk.longCounterBuilder("testLongCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownCounter() {
    LongUpDownCounterSdk longUpDownCounter =
        testSdk
            .longUpDownCounterBuilder("testLongUpDownCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longUpDownCounter).isNotNull();

    assertThat(
            testSdk
                .longUpDownCounterBuilder("testLongUpDownCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longUpDownCounter);

    assertThatThrownBy(() -> testSdk.longUpDownCounterBuilder("testLongUpDownCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> testSdk.longUpDownCounterBuilder("testLongUpDownCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongValueRecorder() {
    LongValueRecorderSdk longValueRecorder =
        testSdk
            .longValueRecorderBuilder("testLongValueRecorder")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longValueRecorder).isNotNull();

    assertThat(
            testSdk
                .longValueRecorderBuilder("testLongValueRecorder")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longValueRecorder);

    assertThatThrownBy(() -> testSdk.longValueRecorderBuilder("testLongValueRecorder").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> testSdk.longValueRecorderBuilder("testLongValueRecorder".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongValueObserver() {
    LongValueObserver longValueObserver =
        testSdk
            .longValueObserverBuilder("longValueObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longValueObserver).isNotNull();

    assertThat(
            testSdk
                .longValueObserverBuilder("longValueObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longValueObserver);

    assertThatThrownBy(() -> testSdk.longValueObserverBuilder("longValueObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> testSdk.longValueObserverBuilder("longValueObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongSumObserver() {
    LongSumObserverSdk longObserver =
        testSdk
            .longSumObserverBuilder("testLongSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            testSdk
                .longSumObserverBuilder("testLongSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longObserver);

    assertThatThrownBy(() -> testSdk.longSumObserverBuilder("testLongSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");

    assertThatThrownBy(
            () -> testSdk.longSumObserverBuilder("testLongSumObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownSumObserver() {
    LongUpDownSumObserverSdk longObserver =
        testSdk
            .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            testSdk
                .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longObserver);

    assertThatThrownBy(
            () -> testSdk.longUpDownSumObserverBuilder("testLongUpDownSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");

    assertThatThrownBy(
            () ->
                testSdk
                    .longUpDownSumObserverBuilder("testLongUpDownSumObserver".toUpperCase())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleCounter() {
    DoubleCounterSdk doubleCounter =
        testSdk
            .doubleCounterBuilder("testDoubleCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleCounter).isNotNull();

    assertThat(
            testSdk
                .doubleCounterBuilder("testDoubleCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleCounter);

    assertThatThrownBy(() -> testSdk.doubleCounterBuilder("testDoubleCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> testSdk.doubleCounterBuilder("testDoubleCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleUpDownCounter() {
    DoubleUpDownCounterSdk doubleUpDownCounter =
        testSdk
            .doubleUpDownCounterBuilder("testDoubleUpDownCounter")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleUpDownCounter).isNotNull();

    assertThat(
            testSdk
                .doubleUpDownCounterBuilder("testDoubleUpDownCounter")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleUpDownCounter);

    assertThatThrownBy(() -> testSdk.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                testSdk.doubleUpDownCounterBuilder("testDoubleUpDownCounter".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueRecorder() {
    DoubleValueRecorderSdk doubleValueRecorder =
        testSdk
            .doubleValueRecorderBuilder("testDoubleValueRecorder")
            .setDescription("My very own ValueRecorder")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueRecorder).isNotNull();

    assertThat(
            testSdk
                .doubleValueRecorderBuilder("testDoubleValueRecorder")
                .setDescription("My very own ValueRecorder")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleValueRecorder);

    assertThatThrownBy(() -> testSdk.doubleValueRecorderBuilder("testDoubleValueRecorder").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                testSdk.doubleValueRecorderBuilder("testDoubleValueRecorder".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleSumObserver() {
    DoubleSumObserverSdk doubleObserver =
        testSdk
            .doubleSumObserverBuilder("testDoubleSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            testSdk
                .doubleSumObserverBuilder("testDoubleSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleObserver);

    assertThatThrownBy(() -> testSdk.doubleSumObserverBuilder("testDoubleSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> testSdk.doubleSumObserverBuilder("testDoubleSumObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleUpDownSumObserver() {
    DoubleUpDownSumObserverSdk doubleObserver =
        testSdk
            .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            testSdk
                .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleObserver);

    assertThatThrownBy(
            () -> testSdk.doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () ->
                testSdk
                    .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver".toUpperCase())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueObserver() {
    DoubleValueObserver doubleValueObserver =
        testSdk
            .doubleValueObserverBuilder("doubleValueObserver")
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueObserver).isNotNull();

    assertThat(
            testSdk
                .doubleValueObserverBuilder("doubleValueObserver")
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleValueObserver);

    assertThatThrownBy(() -> testSdk.doubleValueObserverBuilder("doubleValueObserver").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
    assertThatThrownBy(
            () -> testSdk.doubleValueObserverBuilder("doubleValueObserver".toUpperCase()).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument with same name and different descriptor already created.");
  }

  @Test
  void testBatchRecorder() {
    BatchRecorder batchRecorder = testSdk.newBatchRecorder("key", "value");
    assertThat(batchRecorder).isNotNull();
    assertThat(batchRecorder).isInstanceOf(BatchRecorderSdk.class);
  }

  @Test
  void collectAll() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testLongCounter").build();
    longCounter.add(10, Labels.empty());
    LongValueRecorderSdk longValueRecorder =
        testSdk.longValueRecorderBuilder("testLongValueRecorder").build();
    longValueRecorder.record(10, Labels.empty());
    // LongSumObserver longObserver = testSdk.longSumObserverBuilder("testLongSumObserver").build();
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testDoubleCounter").build();
    doubleCounter.add(10.1, Labels.empty());
    DoubleValueRecorderSdk doubleValueRecorder =
        testSdk.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    doubleValueRecorder.record(10.1, Labels.empty());
    // DoubleSumObserver doubleObserver =
    // testSdk.doubleSumObserverBuilder("testDoubleSumObserver").build();

    assertThat(testSdk.collectAll())
        .containsExactlyInAnyOrder(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10)))),
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleCounter",
                "",
                "1",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10.1)))),
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongValueRecorder",
                "",
                "1",
                MetricData.DoubleSummaryData.create(
                    Collections.singletonList(
                        DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.empty(),
                            1,
                            10,
                            Arrays.asList(
                                ValueAtPercentile.create(0, 10),
                                ValueAtPercentile.create(100, 10)))))),
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleValueRecorder",
                "",
                "1",
                MetricData.DoubleSummaryData.create(
                    Collections.singletonList(
                        DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.empty(),
                            1,
                            10.1d,
                            Arrays.asList(
                                ValueAtPercentile.create(0, 10.1d),
                                ValueAtPercentile.create(100, 10.1d)))))));
  }
}
