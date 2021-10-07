/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
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
    InMemoryMetricReader sdkMeterReader = new InMemoryMetricReader();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();

    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter longCounter = sdkMeter.counterBuilder("testLongCounter").build();
    longCounter.add(10, Attributes.empty());
    LongUpDownCounter longUpDownCounter =
        sdkMeter.upDownCounterBuilder("testLongUpDownCounter").build();
    longUpDownCounter.add(-10, Attributes.empty());
    LongHistogram longValueRecorder =
        sdkMeter.histogramBuilder("testLongHistogram").ofLongs().build();
    longValueRecorder.record(10, Attributes.empty());
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testDoubleCounter").ofDoubles().build();
    doubleCounter.add(10.1, Attributes.empty());
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testDoubleUpDownCounter").ofDoubles().build();
    doubleUpDownCounter.add(-10.1, Attributes.empty());
    DoubleHistogram doubleValueRecorder = sdkMeter.histogramBuilder("testDoubleHistogram").build();
    doubleValueRecorder.record(10.1, Attributes.empty());

    assertThat(sdkMeterReader.collectAllMetrics())
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
                    .hasName("testDoubleHistogram")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasCount(1)
                                .hasSum(10.1)
                                .hasBucketCounts(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
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
            metric ->
                assertThat(metric)
                    .hasName("testLongHistogram")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasCount(1)
                                .hasSum(10)
                                .hasBucketCounts(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
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
            .setAggregation(
                Aggregation.explicitBucketHistogram(
                    AggregationTemporality.DELTA, Collections.emptyList()))
            .build());
    InMemoryMetricReader sdkMeterReader = new InMemoryMetricReader();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());

    LongCounter longCounter = sdkMeter.counterBuilder("testLongCounter").build();
    longCounter.add(10, Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testLongCounter")
                    .hasDoubleHistogram()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 1000000000)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasBucketCounts(1)));

    longCounter.add(10, Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDoubleHistogram()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 1000000000)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasBucketCounts(1)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllSyncInstruments_DeltaHistogram() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder,
        Aggregation.explicitBucketHistogram(AggregationTemporality.DELTA, Collections.emptyList()));
    InMemoryMetricReader sdkMeterReader = new InMemoryMetricReader();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter longCounter = sdkMeter.counterBuilder("testLongCounter").build();
    longCounter.add(10, Attributes.empty());
    LongUpDownCounter longUpDownCounter =
        sdkMeter.upDownCounterBuilder("testLongUpDownCounter").build();
    longUpDownCounter.add(10, Attributes.empty());
    LongHistogram longValueRecorder =
        sdkMeter.histogramBuilder("testLongValueRecorder").ofLongs().build();
    longValueRecorder.record(10, Attributes.empty());
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testDoubleCounter").ofDoubles().build();
    doubleCounter.add(10, Attributes.empty());
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testDoubleUpDownCounter").ofDoubles().build();
    doubleUpDownCounter.add(10, Attributes.empty());
    DoubleHistogram doubleValueRecorder =
        sdkMeter.histogramBuilder("testDoubleValueRecorder").build();
    doubleValueRecorder.record(10, Attributes.empty());

    testClock.advance(Duration.ofSeconds(1));

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasDoubleHistogram()
                    .isDelta()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 1000000000)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasBucketCounts(1)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongCounter",
            "testDoubleCounter",
            "testLongUpDownCounter",
            "testDoubleUpDownCounter",
            "testLongValueRecorder",
            "testDoubleValueRecorder");

    testClock.advance(Duration.ofSeconds(1));

    longCounter.add(10, Attributes.empty());
    longUpDownCounter.add(10, Attributes.empty());
    longValueRecorder.record(10, Attributes.empty());
    doubleCounter.add(10, Attributes.empty());
    doubleUpDownCounter.add(10, Attributes.empty());
    doubleValueRecorder.record(10, Attributes.empty());

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasDoubleHistogram()
                    .isDelta()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 1000000000)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasBucketCounts(1)))
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
    InMemoryMetricReader sdkMeterReader = new InMemoryMetricReader();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .buildWithCallback(longResult -> longResult.observe(10, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .buildWithCallback(longResult -> longResult.observe(-10, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testLongValueObserver")
        .ofLongs()
        .buildWithCallback(longResult -> longResult.observe(10, Attributes.empty()));

    sdkMeter
        .counterBuilder("testDoubleSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.observe(10.1, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.observe(-10.1, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testDoubleValueObserver")
        .buildWithCallback(doubleResult -> doubleResult.observe(10.1, Attributes.empty()));

    assertThat(sdkMeterReader.collectAllMetrics())
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
  void viewSdk_AllowRenames() {
    InMemoryMetricReader reader = new InMemoryMetricReader();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder()
                    // TODO: Make instrument type optional.
                    .setInstrumentType(InstrumentType.OBSERVABLE_GAUGE)
                    .setInstrumentName("test")
                    .build(),
                View.builder()
                    .setName("not_test")
                    .setDescription("not_desc")
                    .setAggregation(Aggregation.lastValue())
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    meter
        .gaugeBuilder("test")
        .setDescription("desc")
        .setUnit("unit")
        .buildWithCallback(o -> o.observe(1));
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("not_test")
                    .hasDescription("not_desc")
                    .hasUnit("unit")
                    .hasDoubleGauge());
  }

  @Test
  @SuppressWarnings("unchecked")
  void viewSdk_AllowMulitpleViewsPerSynchronousInstrument() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            // TODO: Make instrument type optional.
            .setInstrumentType(InstrumentType.HISTOGRAM)
            .setInstrumentName("test")
            .build();
    InMemoryMetricReader reader = new InMemoryMetricReader();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                selector,
                View.builder()
                    .setName("not_test")
                    .setDescription("not_desc")
                    .setAggregation(Aggregation.lastValue())
                    .build())
            .registerView(
                selector,
                View.builder()
                    .setName("not_test_2")
                    .setDescription("not_desc_2")
                    .setAggregation(Aggregation.sum(AggregationTemporality.CUMULATIVE))
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    DoubleHistogram histogram =
        meter.histogramBuilder("test").setDescription("desc").setUnit("unit").build();
    histogram.record(1.0);
    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("not_test")
                    .hasDescription("not_desc")
                    .hasUnit("unit")
                    .hasDoubleGauge(),
            metric ->
                assertThat(metric)
                    .hasName("not_test_2")
                    .hasDescription("not_desc_2")
                    .hasUnit("unit")
                    .hasDoubleSum());
  }

  @Test
  @SuppressWarnings("unchecked")
  void viewSdk_AllowMulitpleViewsPerAsynchronousInstrument() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            // TODO: Make instrument type optional.
            .setInstrumentType(InstrumentType.OBSERVABLE_GAUGE)
            .setInstrumentName("test")
            .build();
    InMemoryMetricReader reader = new InMemoryMetricReader();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                selector,
                View.builder()
                    .setName("not_test")
                    .setDescription("not_desc")
                    .setAggregation(Aggregation.lastValue())
                    .build())
            .registerView(
                selector,
                View.builder()
                    .setName("not_test_2")
                    .setDescription("not_desc_2")
                    .setAggregation(Aggregation.sum(AggregationTemporality.CUMULATIVE))
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    meter
        .gaugeBuilder("test")
        .setDescription("desc")
        .setUnit("unit")
        .buildWithCallback(obs -> obs.observe(1.0));
    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("not_test")
                    .hasDescription("not_desc")
                    .hasUnit("unit")
                    .hasDoubleGauge(),
            metric ->
                assertThat(metric)
                    .hasName("not_test_2")
                    .hasDescription("not_desc_2")
                    .hasUnit("unit")
                    .hasDoubleSum());
  }

  @Test
  void viewSdk_capturesBaggageFromContext() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentName("test")
            .build();
    InMemoryMetricReader reader = new InMemoryMetricReader();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                selector,
                View.builder()
                    .setAggregation(Aggregation.sum(AggregationTemporality.CUMULATIVE))
                    .appendAllBaggageAttributes()
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    Baggage baggage = Baggage.builder().put("baggage", "value").build();
    Context context = Context.root().with(baggage);
    LongCounter counter = meter.counterBuilder("test").build();

    // Make sure whether or not we explicitly pass baggage, all values have it appended.
    counter.add(1, Attributes.empty(), context);
    counter.bind(Attributes.empty()).add(1, context);
    // Also check implicit context
    try (Scope scope = context.makeCurrent()) {
      counter.add(1, Attributes.empty());
      counter.bind(Attributes.empty()).add(1);
    }
    // Now make sure all metrics have baggage appended.
    // Implicitly we should have ONLY ONE metric data point that has baggage appended.
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("test")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasAttributes(
                                    Attributes.builder().put("baggage", "value").build())));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectAllAsyncInstruments_CumulativeHistogram() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder,
        Aggregation.explicitBucketHistogram(
            AggregationTemporality.CUMULATIVE, Collections.emptyList()));
    InMemoryMetricReader sdkMeterReader = new InMemoryMetricReader();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .buildWithCallback(longResult -> longResult.observe(10, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .buildWithCallback(longResult -> longResult.observe(-10, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testLongValueObserver")
        .ofLongs()
        .buildWithCallback(longResult -> longResult.observe(10, Attributes.empty()));

    sdkMeter
        .counterBuilder("testDoubleSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.observe(10.1, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.observe(-10.1, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testDoubleValueObserver")
        .buildWithCallback(doubleResult -> doubleResult.observe(10.1, Attributes.empty()));

    testClock.advance(Duration.ofNanos(50));

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasDoubleHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 50)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasBucketCounts(1)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongSumObserver",
            "testDoubleSumObserver",
            "testLongUpDownSumObserver",
            "testDoubleUpDownSumObserver",
            "testLongValueObserver",
            "testDoubleValueObserver");

    testClock.advance(Duration.ofNanos(50));

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDescription("")
                    .hasUnit("1")
                    .hasDoubleHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 100)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasBucketCounts(2)))
        .extracting(metric -> metric.getName())
        .containsExactlyInAnyOrder(
            "testLongSumObserver",
            "testDoubleSumObserver",
            "testLongUpDownSumObserver",
            "testDoubleUpDownSumObserver",
            "testLongValueObserver",
            "testDoubleValueObserver");
  }

  @Test
  @SuppressWarnings("unchecked")
  void sdkMeterProvider_supportsMultipleCollectorsCumulative() {
    InMemoryMetricReader collector1 = new InMemoryMetricReader();
    InMemoryMetricReader collector2 = new InMemoryMetricReader();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(collector1)
            .registerMetricReader(collector2)
            .build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    final LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    final long startTime = testClock.now();

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    assertThat(collector1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(startTime)
                                .hasEpochNanos(testClock.now())
                                .hasValue(1)));

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    // Make sure collector 2 sees the value collector 1 saw
    assertThat(collector2.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(startTime)
                                .hasEpochNanos(testClock.now())
                                .hasValue(2)));

    // Make sure Collector 1 sees the same point as 2
    assertThat(collector1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSum()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(startTime)
                                .hasEpochNanos(testClock.now())
                                .hasValue(2)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void sdkMeterProvider_supportsMultipleCollectorsDelta() {
    // Note: we use a view to do delta aggregation, but any view ALWAYS uses double-precision right
    // now.
    InMemoryMetricReader collector1 = new InMemoryMetricReader();
    InMemoryMetricReader collector2 = new InMemoryMetricReader();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(collector1)
            .registerMetricReader(collector2)
            .registerView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.COUNTER)
                    .setInstrumentName("testSum")
                    .build(),
                View.builder()
                    .setAggregation(Aggregation.sum(AggregationTemporality.DELTA))
                    .build())
            .build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    final LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    final long startTime = testClock.now();

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    assertThat(collector1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(startTime)
                                .hasEpochNanos(testClock.now())
                                .hasValue(1)));
    long collectorOneTimeOne = testClock.now();

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    // Make sure collector 2 sees the value collector 1 saw
    assertThat(collector2.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(startTime)
                                .hasEpochNanos(testClock.now())
                                .hasValue(2)));

    // Make sure Collector 1 sees the same point as 2, when it collects.
    assertThat(collector1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSum()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(collectorOneTimeOne)
                                .hasEpochNanos(testClock.now())
                                .hasValue(1)));
  }

  private static void registerViewForAllTypes(
      SdkMeterProviderBuilder meterProviderBuilder, Aggregation aggregation) {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      meterProviderBuilder.registerView(
          InstrumentSelector.builder().setInstrumentType(instrumentType).build(),
          View.builder().setAggregation(aggregation).build());
    }
  }
}
