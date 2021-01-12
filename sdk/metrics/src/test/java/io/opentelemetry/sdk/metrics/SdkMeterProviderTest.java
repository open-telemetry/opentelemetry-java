/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
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
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final SdkMeter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());

  @Test
  void defaultMeterName() {
    assertThat(sdkMeterProvider.get(null)).isSameAs(sdkMeterProvider.get("unknown"));
  }

  @Test
  void collectAllSyncInstruments() {
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
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10)))),
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
                        MetricData.DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10.1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongUpDownCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), -10)))),
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleUpDownCounter",
                "",
                "1",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), -10.1)))),
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongValueRecorder",
                "",
                "1",
                MetricData.DoubleSummaryData.create(
                    Collections.singletonList(
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.empty(),
                            1,
                            10,
                            Arrays.asList(
                                MetricData.ValueAtPercentile.create(0, 10),
                                MetricData.ValueAtPercentile.create(100, 10)))))),
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleValueRecorder",
                "",
                "1",
                MetricData.DoubleSummaryData.create(
                    Collections.singletonList(
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.empty(),
                            1,
                            10.1d,
                            Arrays.asList(
                                MetricData.ValueAtPercentile.create(0, 10.1d),
                                MetricData.ValueAtPercentile.create(100, 10.1d)))))));
  }

  @Test
  void collectAllSyncInstruments_CustomAggregation() {
    registerViewForAllTypes(
        sdkMeterProvider,
        AggregationConfiguration.create(
            AggregatorFactory.count(), MetricData.AggregationTemporality.CUMULATIVE));
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
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongUpDownCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleUpDownCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongValueRecorder",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleValueRecorder",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))));
  }

  @Test
  void collectAllAsyncInstruments() {
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
        .containsExactlyInAnyOrder(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongSumObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10)))),
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleSumObserver",
                "",
                "1",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10.1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongUpDownSumObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), -10)))),
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleUpDownSumObserver",
                "",
                "1",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), -10.1)))),
            MetricData.createLongGauge(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongValueObserver",
                "",
                "1",
                MetricData.LongGaugeData.create(
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10)))),
            MetricData.createDoubleGauge(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleValueObserver",
                "",
                "1",
                MetricData.DoubleGaugeData.create(
                    Collections.singletonList(
                        MetricData.DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 10.1)))));
  }

  @Test
  void collectAllAsyncInstruments_CustomAggregation() {
    registerViewForAllTypes(
        sdkMeterProvider,
        AggregationConfiguration.create(
            AggregatorFactory.count(), MetricData.AggregationTemporality.CUMULATIVE));
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
        .containsExactlyInAnyOrder(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongSumObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleSumObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongUpDownSumObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleUpDownSumObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongValueObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))),
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleValueObserver",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.empty(), 1)))));
  }

  private static void registerViewForAllTypes(
      SdkMeterProvider meterProvider, AggregationConfiguration configuration) {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      meterProvider.registerView(
          InstrumentSelector.builder().setInstrumentType(instrumentType).build(), configuration);
    }
  }
}
