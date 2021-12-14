/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.mockito.Mockito.when;

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
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkMeterProviderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkMeterProviderTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProviderBuilder sdkMeterProviderBuilder =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE);

  @Mock MetricReader metricReader;

  @Test
  void defaultMeterName() {
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    assertThat(sdkMeterProvider.get(null)).isSameAs(sdkMeterProvider.get("unknown"));
  }

  @Test
  void collectAllSyncInstruments() {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .build();

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

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
  void collectAllSyncInstruments_OverwriteTemporality() {
    sdkMeterProviderBuilder.registerView(
        InstrumentSelector.builder().setInstrumentType(InstrumentType.COUNTER).build(),
        View.builder()
            .setAggregation(Aggregation.explicitBucketHistogram(Collections.emptyList()))
            .build());
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create(AggregationTemporality.DELTA);
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());

    LongCounter longCounter = sdkMeter.counterBuilder("testLongCounter").build();
    longCounter.add(10, Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
    exporter.reset();

    longCounter.add(10, Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
  void collectAllSyncInstruments_DeltaHistogram() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder, Aggregation.explicitBucketHistogram(Collections.emptyList()));
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create(AggregationTemporality.DELTA);
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .build();
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

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
        .extracting(MetricData::getName)
        .containsExactlyInAnyOrder(
            "testLongCounter",
            "testDoubleCounter",
            "testLongUpDownCounter",
            "testDoubleUpDownCounter",
            "testLongValueRecorder",
            "testDoubleValueRecorder");
    exporter.reset();

    testClock.advance(Duration.ofSeconds(1));

    longCounter.add(10, Attributes.empty());
    longUpDownCounter.add(10, Attributes.empty());
    longValueRecorder.record(10, Attributes.empty());
    doubleCounter.add(10, Attributes.empty());
    doubleUpDownCounter.add(10, Attributes.empty());
    doubleValueRecorder.record(10, Attributes.empty());

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
        .extracting(MetricData::getName)
        .containsExactlyInAnyOrder(
            "testLongCounter",
            "testDoubleCounter",
            "testLongUpDownCounter",
            "testDoubleUpDownCounter",
            "testLongValueRecorder",
            "testDoubleValueRecorder");
  }

  @Test
  void collectAllAsyncInstruments() {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .buildWithCallback(longResult -> longResult.record(10, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .buildWithCallback(longResult -> longResult.record(-10, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testLongValueObserver")
        .ofLongs()
        .buildWithCallback(longResult -> longResult.record(10, Attributes.empty()));

    sdkMeter
        .counterBuilder("testDoubleSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.record(10.1, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.record(-10.1, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testDoubleValueObserver")
        .buildWithCallback(doubleResult -> doubleResult.record(10.1, Attributes.empty()));

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
                                .hasStartEpochNanos(testClock.now())
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
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(10.1)));
  }

  @Test
  void viewSdk_AllowRenames() {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
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
        .buildWithCallback(o -> o.record(1));
    provider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("not_test")
                    .hasDescription("not_desc")
                    .hasUnit("unit")
                    .hasDoubleGauge());
  }

  @Test
  void viewSdk_AllowMultipleViewsPerSynchronousInstrument() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            // TODO: Make instrument type optional.
            .setInstrumentType(InstrumentType.HISTOGRAM)
            .setInstrumentName("test")
            .build();
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
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
                    .setAggregation(Aggregation.sum())
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    DoubleHistogram histogram =
        meter.histogramBuilder("test").setDescription("desc").setUnit("unit").build();
    histogram.record(1.0);
    provider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
  void viewSdk_AllowMultipleViewsPerAsynchronousInstrument() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            // TODO: Make instrument type optional.
            .setInstrumentType(InstrumentType.OBSERVABLE_GAUGE)
            .setInstrumentName("test")
            .build();
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
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
                    .setAggregation(Aggregation.sum())
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    meter
        .gaugeBuilder("test")
        .setDescription("desc")
        .setUnit("unit")
        .buildWithCallback(obs -> obs.record(1.0));
    provider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .registerView(
                selector,
                View.builder()
                    .setAggregation(Aggregation.sum())
                    .appendAllBaggageAttributes()
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    Baggage baggage = Baggage.builder().put("baggage", "value").build();
    Context context = Context.root().with(baggage);
    LongCounter counter = meter.counterBuilder("test").build();

    // Make sure whether or not we explicitly pass baggage, all values have it appended.
    counter.add(1, Attributes.empty(), context);
    // Also check implicit context
    try (Scope scope = context.makeCurrent()) {
      counter.add(1, Attributes.empty());
    }
    // Now make sure all metrics have baggage appended.
    // Implicitly we should have ONLY ONE metric data point that has baggage appended.
    provider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
  void collectAllAsyncInstruments_CumulativeHistogram() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder, Aggregation.explicitBucketHistogram(Collections.emptyList()));
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .build();
    Meter sdkMeter = sdkMeterProvider.get(SdkMeterProviderTest.class.getName());
    sdkMeter
        .counterBuilder("testLongSumObserver")
        .buildWithCallback(longResult -> longResult.record(10, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testLongUpDownSumObserver")
        .buildWithCallback(longResult -> longResult.record(-10, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testLongValueObserver")
        .ofLongs()
        .buildWithCallback(longResult -> longResult.record(10, Attributes.empty()));

    sdkMeter
        .counterBuilder("testDoubleSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.record(10.1, Attributes.empty()));
    sdkMeter
        .upDownCounterBuilder("testDoubleUpDownSumObserver")
        .ofDoubles()
        .buildWithCallback(doubleResult -> doubleResult.record(-10.1, Attributes.empty()));
    sdkMeter
        .gaugeBuilder("testDoubleValueObserver")
        .buildWithCallback(doubleResult -> doubleResult.record(10.1, Attributes.empty()));

    testClock.advance(Duration.ofNanos(50));

    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
        .extracting(MetricData::getName)
        .containsExactlyInAnyOrder(
            "testLongSumObserver",
            "testDoubleSumObserver",
            "testLongUpDownSumObserver",
            "testDoubleUpDownSumObserver",
            "testLongValueObserver",
            "testDoubleValueObserver");
    exporter.reset();

    testClock.advance(Duration.ofNanos(50));
    // When collecting the next set of async measurements, we still only have 1 count per bucket
    // because we assume ALL measurements are cumulative and come in the async callback.
    // Note: We do not support "gauge histogram".
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
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
                                .hasBucketCounts(1)))
        .extracting(MetricData::getName)
        .containsExactlyInAnyOrder(
            "testLongSumObserver",
            "testDoubleSumObserver",
            "testLongUpDownSumObserver",
            "testDoubleUpDownSumObserver",
            "testLongValueObserver",
            "testDoubleValueObserver");
  }

  @Test
  void sdkMeterProvider_supportsMultipleReadersCumulative() {
    InMemoryMetricReader reader1 = new InMemoryMetricReader(AggregationTemporality.CUMULATIVE);
    InMemoryMetricReader reader2 = new InMemoryMetricReader(AggregationTemporality.CUMULATIVE);
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder.registerMetricReader(reader1).registerMetricReader(reader2).build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    final LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    final long startTime = testClock.now();

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    assertThat(reader1.collectAllMetrics())
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

    // Make sure reader2 sees the value reader1 saw
    assertThat(reader2.collectAllMetrics())
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

    // Make sure reader1 sees the same point as reader22
    assertThat(reader1.collectAllMetrics())
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
  void sdkMeterProvider_supportsMultipleReadersDelta() {
    // Note: we use a view to do delta aggregation, but any view ALWAYS uses double-precision right
    // now.
    InMemoryMetricReader reader1 = new InMemoryMetricReader(AggregationTemporality.DELTA);
    InMemoryMetricReader reader2 = new InMemoryMetricReader(AggregationTemporality.DELTA);
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader1)
            .registerMetricReader(reader2)
            .registerView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.COUNTER)
                    .setInstrumentName("testSum")
                    .build(),
                View.builder().setAggregation(Aggregation.sum()).build())
            .build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    final LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    final long startTime = testClock.now();

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    assertThat(reader1.collectAllMetrics())
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

    // Make sure reader2 sees the value reader1 saw
    assertThat(reader2.collectAllMetrics())
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

    // Make sure reader1 sees the same point as reader2, when it collects.
    assertThat(reader1.collectAllMetrics())
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

  @Test
  void collectAll_DropAggregator() {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerView(
                InstrumentSelector.builder().setInstrumentType(InstrumentType.COUNTER).build(),
                View.builder().setAggregation(Aggregation.drop()).build())
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .build();
    Meter meter = sdkMeterProvider.get("my-meter");
    meter.counterBuilder("sync-counter").build().add(1);
    meter.counterBuilder("async-counter").buildWithCallback(measurement -> measurement.record(1));
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .hasSize(1)
        .satisfiesExactly(
            metric -> assertThat(metric).hasResource(RESOURCE).hasName("async-counter"));
  }

  @Test
  void shutdown() {
    when(metricReader.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode result =
        SdkMeterProvider.builder()
            .registerMetricReader(unused -> metricReader)
            .build()
            .shutdown()
            .join(10, TimeUnit.SECONDS);

    assertThat(result.isSuccess()).isTrue();
  }

  private static void registerViewForAllTypes(
      SdkMeterProviderBuilder meterProviderBuilder, Aggregation aggregation) {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      meterProviderBuilder.registerView(
          InstrumentSelector.builder().setInstrumentType(instrumentType).build(),
          View.builder().setAggregation(aggregation).build());
    }
  }

  private static class InMemoryMetricReader implements MetricReader, MetricReaderFactory {
    private final AggregationTemporality preferred;
    @Nullable private volatile MetricProducer metricProducer;

    private InMemoryMetricReader(AggregationTemporality preferred) {
      this.preferred = preferred;
    }

    /** Returns all metrics accumulated since the last call. */
    public Collection<MetricData> collectAllMetrics() {
      MetricProducer metricProducer = this.metricProducer;
      if (metricProducer != null) {
        return metricProducer.collectAllMetrics();
      }
      return Collections.emptyList();
    }

    @Override
    public AggregationTemporality getPreferredTemporality() {
      return preferred;
    }

    @Override
    public CompletableResultCode flush() {
      MetricProducer metricProducer = this.metricProducer;
      if (metricProducer != null) {
        metricProducer.collectAllMetrics();
      }
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public MetricReader apply(MetricProducer producer) {
      this.metricProducer = producer;
      return this;
    }
  }
}
