/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongCounter;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewBuilderImpl;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkMeterProviderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkMeterProviderTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProviderBuilder sdkMeterProviderBuilder =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE);

  @Mock MetricReader metricReader;

  @Test
  void noopImplementationWithNoReaders() {
    SdkMeterProvider meterProvider = sdkMeterProviderBuilder.build();
    assertThat(meterProvider.meterBuilder("test"))
        .isSameAs(MeterProvider.noop().meterBuilder("test"));
    assertThat(meterProvider.get("test")).isSameAs(MeterProvider.noop().get("test"));
    assertThat(meterProvider.forceFlush().isSuccess()).isTrue();
    assertThat(meterProvider.shutdown().isSuccess()).isTrue();
  }

  @Test
  void defaultMeterName() {
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(InMemoryMetricReader.create()).build();
    assertThat(sdkMeterProvider.get(null)).isSameAs(sdkMeterProvider.get("unknown"));
  }

  @Test
  void collectAllSyncInstruments() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
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
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
        InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
        View.builder()
            .setAggregation(Aggregation.explicitBucketHistogram(Collections.emptyList()))
            .build());
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.createDelta();
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
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.createDelta();
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
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
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

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
  void removeAsyncInstrument() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    Meter meter =
        sdkMeterProviderBuilder.registerMetricReader(reader).build().get(getClass().getName());

    ObservableLongCounter observableCounter1 =
        meter
            .counterBuilder("foo")
            .buildWithCallback(
                measurement ->
                    measurement.record(10, Attributes.builder().put("callback", "one").build()));
    ObservableLongCounter observableCounter2 =
        meter
            .counterBuilder("foo")
            .buildWithCallback(
                measurement ->
                    measurement.record(10, Attributes.builder().put("callback", "two").build()));

    assertThat(reader.collectAllMetrics())
        .hasSize(1)
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasLongSum()
                    .points()
                    .hasSize(2)
                    .satisfiesExactlyInAnyOrder(
                        pointData ->
                            assertThat(pointData)
                                .hasAttributes(Attributes.builder().put("callback", "one").build()),
                        (Consumer<LongPointData>)
                            longPointData ->
                                assertThat(longPointData)
                                    .hasAttributes(
                                        Attributes.builder().put("callback", "two").build())));

    observableCounter1.close();

    assertThat(reader.collectAllMetrics())
        .hasSize(1)
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasLongSum()
                    .points()
                    .hasSize(1)
                    .satisfiesExactlyInAnyOrder(
                        (Consumer<LongPointData>)
                            longPointData ->
                                assertThat(longPointData)
                                    .hasAttributes(
                                        Attributes.builder().put("callback", "two").build())));

    observableCounter2.close();
    assertThat(reader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void viewSdk_filterAttributes() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder()
                    // TODO: Make instrument type optional.
                    .setType(InstrumentType.OBSERVABLE_GAUGE)
                    .setName("test")
                    .build(),
                View.builder().setAttributeFilter(name -> name.equals("allowed")).build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    meter
        .gaugeBuilder("test")
        .setDescription("desc")
        .setUnit("unit")
        .buildWithCallback(
            o ->
                o.record(
                    1,
                    Attributes.builder().put("allowed", "bear").put("not allowed", "dog").build()));
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleGauge()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point.getAttributes().asMap())
                                .containsOnly(entry(AttributeKey.stringKey("allowed"), "bear"))));
  }

  @Test
  void viewSdk_AllowRenames() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder()
                    // TODO: Make instrument type optional.
                    .setType(InstrumentType.OBSERVABLE_GAUGE)
                    .setName("test")
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
  void viewSdk_AllowMulitpleViewsPerSynchronousInstrument() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            // TODO: Make instrument type optional.
            .setType(InstrumentType.HISTOGRAM)
            .setName("test")
            .build();
    InMemoryMetricReader reader = InMemoryMetricReader.create();
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
                    .setAggregation(Aggregation.sum())
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
  void viewSdk_AllowMulitpleViewsPerAsynchronousInstrument() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            // TODO: Make instrument type optional.
            .setType(InstrumentType.OBSERVABLE_GAUGE)
            .setName("test")
            .build();
    InMemoryMetricReader reader = InMemoryMetricReader.create();
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
                    .setAggregation(Aggregation.sum())
                    .build())
            .build();
    Meter meter = provider.get(SdkMeterProviderTest.class.getName());
    meter
        .gaugeBuilder("test")
        .setDescription("desc")
        .setUnit("unit")
        .buildWithCallback(obs -> obs.record(1.0));
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
        InstrumentSelector.builder().setType(InstrumentType.COUNTER).setName("test").build();
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                selector,
                ((ViewBuilderImpl) View.builder().setAggregation(Aggregation.sum()))
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
    try (Scope ignored = context.makeCurrent()) {
      counter.add(1, Attributes.empty());
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
  void collectAllAsyncInstruments_CumulativeHistogram() {
    registerViewForAllTypes(
        sdkMeterProviderBuilder, Aggregation.explicitBucketHistogram(Collections.emptyList()));
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
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

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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

    testClock.advance(Duration.ofNanos(50));
    // When collecting the next set of async measurements, we still only have 1 count per bucket
    // because we assume ALL measurements are cumulative and come in the async callback.
    // Note: We do not support "gauge histogram".
    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
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
  void sdkMeterProvider_supportsMultipleCollectorsCumulative() {
    InMemoryMetricReader collector1 = InMemoryMetricReader.create();
    InMemoryMetricReader collector2 = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(collector1)
            .registerMetricReader(collector2)
            .build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    long startTime = testClock.now();

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
  void sdkMeterProvider_supportsMultipleCollectorsDelta() {
    // Note: we use a view to do delta aggregation, but any view ALWAYS uses double-precision right
    // now.
    InMemoryMetricReader collector1 = InMemoryMetricReader.createDelta();
    InMemoryMetricReader collector2 = InMemoryMetricReader.createDelta();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(collector1)
            .registerMetricReader(collector2)
            .registerView(
                InstrumentSelector.builder()
                    .setType(InstrumentType.COUNTER)
                    .setName("testSum")
                    .build(),
                View.builder().setAggregation(Aggregation.sum()).build())
            .build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    long startTime = testClock.now();

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

  @Test
  void collectAll_DropAggregator() {
    InMemoryMetricReader collector = InMemoryMetricReader.create();
    Meter meter =
        sdkMeterProviderBuilder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setAggregation(Aggregation.drop()).build())
            .registerMetricReader(collector)
            .build()
            .get("my-meter");
    meter.counterBuilder("sync-counter").build().add(1);
    meter.counterBuilder("async-counter").buildWithCallback(measurement -> measurement.record(1));
    assertThat(collector.collectAllMetrics())
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
          InstrumentSelector.builder().setType(instrumentType).build(),
          View.builder().setAggregation(aggregation).build());
    }
  }
}
