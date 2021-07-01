/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class SdkMeterProviderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkMeterProviderTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProviderBuilder sdkMeterProviderBuilder =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE);

  @Test
  void defaultMeterName() {
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    assertThat(sdkMeterProvider.get(null)).isSameAs(sdkMeterProvider.get("unknown"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllSyncInstruments() {
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter longCounter = sdkMeter.longCounterBuilder("testLongCounter").build();
    longCounter.add(10, Labels.empty());
    LongUpDownCounter longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testLongUpDownCounter").build();
    longUpDownCounter.add(-10, Labels.empty());
    LongValueRecorder longValueRecorder =
        sdkMeter.longValueRecorderBuilder("testLongValueRecorder").build();
    longValueRecorder.record(10, Labels.empty());
    DoubleCounter doubleCounter = sdkMeter.doubleCounterBuilder("testDoubleCounter").build();
    doubleCounter.add(10.1, Labels.empty());
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build();
    doubleUpDownCounter.add(-10.1, Labels.empty());
    DoubleValueRecorder doubleValueRecorder =
        sdkMeter.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    doubleValueRecorder.record(10.1, Labels.empty());

    assertThat(sdkMeterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1"))
        .satisfiesExactlyInAnyOrder(
            // Note: summary metric is being deprecated.
            metric ->
                assertThat(metric)
                    .isEqualTo(
                        MetricData.createDoubleSummary(
                            RESOURCE,
                            INSTRUMENTATION_LIBRARY_INFO,
                            "testDoubleValueRecorder",
                            "",
                            "1",
                            DoubleSummaryData.create(
                                Collections.singletonList(
                                    DoubleSummaryPointData.create(
                                        testClock.now(),
                                        testClock.now(),
                                        Attributes.empty(),
                                        1,
                                        10.1d,
                                        Arrays.asList(
                                            ValueAtPercentile.create(0, 10.1d),
                                            ValueAtPercentile.create(100, 10.1d))))))),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleCounter")
                    .hasDoubleSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10.1)),

            // Note: Summary is deprecated, we don't have a nice matcher for this.
            metric ->
                assertThat(metric)
                    .isEqualTo(
                        MetricData.createDoubleSummary(
                            RESOURCE,
                            INSTRUMENTATION_LIBRARY_INFO,
                            "testLongValueRecorder",
                            "",
                            "1",
                            DoubleSummaryData.create(
                                Collections.singletonList(
                                    DoubleSummaryPointData.create(
                                        testClock.now(),
                                        testClock.now(),
                                        Attributes.empty(),
                                        1,
                                        10,
                                        Arrays.asList(
                                            ValueAtPercentile.create(0, 10),
                                            ValueAtPercentile.create(100, 10))))))),
            metric ->
                assertThat(metric)
                    .hasName("testLongUpDownCounter")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(-10)),
            metric ->
                assertThat(metric)
                    .hasName("testLongCounter")
                    .hasLongSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10)),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleUpDownCounter")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(-10.1)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllSyncInstruments_OverwriteTemporality() {
    sdkMeterProviderBuilder.registerView(
        InstrumentSelector.builder().setInstrumentType(InstrumentType.COUNTER).build(),
        View.builder()
            .setAggregatorFactory(AggregatorFactory.sum(AggregationTemporality.DELTA))
            .build());
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());

    LongCounter longCounter = sdkMeter.longCounterBuilder("testLongCounter").build();
    longCounter.add(10, Labels.empty());
    testClock.advanceNanos(50);

    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testLongCounter")
                    .hasLongSum()
                    .isMonotonic()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 50)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10)));

    longCounter.add(10, Labels.empty());
    testClock.advanceNanos(50);

    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasLongSum()
                    .isMonotonic()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 50)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllSyncInstruments_DeltaCount() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder, AggregatorFactory.count(AggregationTemporality.DELTA));
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter longCounter = sdkMeter.longCounterBuilder("testLongCounter").build();
    longCounter.add(10, Labels.empty());
    LongUpDownCounter longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testLongUpDownCounter").build();
    longUpDownCounter.add(-10, Labels.empty());
    LongValueRecorder longValueRecorder =
        sdkMeter.longValueRecorderBuilder("testLongValueRecorder").build();
    longValueRecorder.record(10, Labels.empty());
    DoubleCounter doubleCounter = sdkMeter.doubleCounterBuilder("testDoubleCounter").build();
    doubleCounter.add(10.1, Labels.empty());
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build();
    doubleUpDownCounter.add(-10.1, Labels.empty());
    DoubleValueRecorder doubleValueRecorder =
        sdkMeter.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    doubleValueRecorder.record(10.1, Labels.empty());

    testClock.advanceNanos(50);

    assertThat(sdkMeterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasLongSum()
                    .isMonotonic()
                    .isDelta()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 50)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(1)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongCounter",
            "testDoubleCounter",
            "testLongUpDownCounter",
            "testDoubleUpDownCounter",
            "testLongValueRecorder",
            "testDoubleValueRecorder");

    testClock.advanceNanos(50);

    longCounter.add(10, Labels.empty());
    longUpDownCounter.add(-10, Labels.empty());
    longValueRecorder.record(10, Labels.empty());
    doubleCounter.add(10.1, Labels.empty());
    doubleUpDownCounter.add(-10.1, Labels.empty());
    doubleValueRecorder.record(10.1, Labels.empty());

    assertThat(sdkMeterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasLongSum()
                    .isMonotonic()
                    .isDelta()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 50)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(1)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongCounter",
            "testDoubleCounter",
            "testLongUpDownCounter",
            "testDoubleUpDownCounter",
            "testLongValueRecorder",
            "testDoubleValueRecorder");
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllAsyncInstruments() {
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    sdkMeter
        .longSumObserverBuilder("testLongSumObserver")
        .setUpdater(longResult -> longResult.observe(10, Labels.empty()))
        .build();
    sdkMeter
        .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
        .setUpdater(longResult -> longResult.observe(-10, Labels.empty()))
        .build();
    sdkMeter
        .longValueObserverBuilder("testLongValueObserver")
        .setUpdater(longResult -> longResult.observe(10, Labels.empty()))
        .build();

    sdkMeter
        .doubleSumObserverBuilder("testDoubleSumObserver")
        .setUpdater(doubleResult -> doubleResult.observe(10.1, Labels.empty()))
        .build();
    sdkMeter
        .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
        .setUpdater(doubleResult -> doubleResult.observe(-10.1, Labels.empty()))
        .build();
    sdkMeter
        .doubleValueObserverBuilder("testDoubleValueObserver")
        .setUpdater(doubleResult -> doubleResult.observe(10.1, Labels.empty()))
        .build();

    assertThat(sdkMeterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1"))
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongSumObserver")
                    .hasLongSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10)),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleSumObserver")
                    .hasDoubleSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10.1)),
            metric ->
                assertThat(metric)
                    .hasName("testLongUpDownSumObserver")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(-10)),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleUpDownSumObserver")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(-10.1)),
            metric ->
                assertThat(metric)
                    .hasName("testLongValueObserver")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(0) // Gauges have no start time?
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10)),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleValueObserver")
                    .hasDoubleGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(0) // Gauges have no start time?
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10.1)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllAsyncInstruments_CumulativeCount() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder, AggregatorFactory.count(AggregationTemporality.CUMULATIVE));
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    sdkMeter
        .longSumObserverBuilder("testLongSumObserver")
        .setUpdater(longResult -> longResult.observe(10, Labels.empty()))
        .build();
    sdkMeter
        .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
        .setUpdater(longResult -> longResult.observe(-10, Labels.empty()))
        .build();
    sdkMeter
        .longValueObserverBuilder("testLongValueObserver")
        .setUpdater(longResult -> longResult.observe(10, Labels.empty()))
        .build();

    sdkMeter
        .doubleSumObserverBuilder("testDoubleSumObserver")
        .setUpdater(doubleResult -> doubleResult.observe(10.1, Labels.empty()))
        .build();
    sdkMeter
        .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
        .setUpdater(doubleResult -> doubleResult.observe(-10.1, Labels.empty()))
        .build();
    sdkMeter
        .doubleValueObserverBuilder("testDoubleValueObserver")
        .setUpdater(doubleResult -> doubleResult.observe(10.1, Labels.empty()))
        .build();

    testClock.advanceNanos(50);

    assertThat(sdkMeterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasLongSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 50)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(1)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongSumObserver",
            "testDoubleSumObserver",
            "testLongUpDownSumObserver",
            "testDoubleUpDownSumObserver",
            "testLongValueObserver",
            "testDoubleValueObserver");

    testClock.advanceNanos(50);

    assertThat(sdkMeterProvider.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasLongSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 100)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(2)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongSumObserver",
            "testDoubleSumObserver",
            "testLongUpDownSumObserver",
            "testDoubleUpDownSumObserver",
            "testLongValueObserver",
            "testDoubleValueObserver");
  }

  private static void registerViewForAllTypes(
      SdkMeterProviderBuilder meterProviderBuilder, AggregatorFactory factory) {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      meterProviderBuilder.registerView(
          InstrumentSelector.builder().setInstrumentType(instrumentType).build(),
          View.builder().setAggregatorFactory(factory).build());
    }
  }
}
