/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
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
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkMeterProviderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkMeterProviderTest.class.getName());

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(ViewRegistry.class);

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
                    .hasUnit(""))
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testDoubleHistogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasCount(1)
                                        .hasSum(10.1)
                                        .hasBucketCounts(
                                            0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(10.1))),
            metric ->
                assertThat(metric)
                    .hasName("testLongHistogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasCount(1)
                                        .hasSum(10)
                                        .hasBucketCounts(
                                            0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))),
            metric ->
                assertThat(metric)
                    .hasName("testLongUpDownCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(-10))),
            metric ->
                assertThat(metric)
                    .hasName("testLongCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(10))),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleUpDownCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(-10.1))));
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
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram
                                .isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - 1000000000)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasBucketCounts(1))));

    longCounter.add(10, Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram
                                .isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - 1000000000)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasBucketCounts(1))));
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
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testLongHistogram").ofLongs().build();
    longHistogram.record(10, Attributes.empty());
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testDoubleCounter").ofDoubles().build();
    doubleCounter.add(10, Attributes.empty());
    DoubleHistogram doubleHistogram = sdkMeter.histogramBuilder("testDoubleHistogram").build();
    doubleHistogram.record(10, Attributes.empty());

    testClock.advance(Duration.ofSeconds(1));

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasDescription("")
                    .hasUnit("")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram
                                .isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - 1000000000)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasBucketCounts(1))))
        .extracting(MetricData::getName)
        .containsExactlyInAnyOrder(
            "testLongCounter", "testDoubleCounter", "testLongHistogram", "testDoubleHistogram");

    testClock.advance(Duration.ofSeconds(1));

    longCounter.add(10, Attributes.empty());
    longHistogram.record(10, Attributes.empty());
    doubleCounter.add(10, Attributes.empty());
    doubleHistogram.record(10, Attributes.empty());

    assertThat(sdkMeterReader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasDescription("")
                    .hasUnit("")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram
                                .isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - 1000000000)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasBucketCounts(1))))
        .extracting(MetricData::getName)
        .containsExactlyInAnyOrder(
            "testLongCounter", "testDoubleCounter", "testLongHistogram", "testDoubleHistogram");
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
                    .hasUnit(""))
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongSumObserver")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(10))),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleSumObserver")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(10.1))),
            metric ->
                assertThat(metric)
                    .hasName("testLongUpDownSumObserver")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(-10))),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleUpDownSumObserver")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(-10.1))),
            metric ->
                assertThat(metric)
                    .hasName("testLongValueObserver")
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(10))),
            metric ->
                assertThat(metric)
                    .hasName("testDoubleValueObserver")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(10.1))));
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
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point -> point.hasAttributes(attributeEntry("callback", "one")),
                                point -> point.hasAttributes(attributeEntry("callback", "two")))));

    observableCounter1.close();

    assertThat(reader.collectAllMetrics())
        .hasSize(1)
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point -> point.hasAttributes(attributeEntry("callback", "two")))));

    observableCounter2.close();
    assertThat(reader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void collectAllMetrics_NoConcurrentCalls()
      throws ExecutionException, InterruptedException, TimeoutException {
    InMemoryMetricReader reader1 = InMemoryMetricReader.create();
    InMemoryMetricReader reader2 = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder.registerMetricReader(reader1).registerMetricReader(reader2).build();
    AtomicBoolean inProgress = new AtomicBoolean(false);
    // Callback records if not called concurrently
    Consumer<ObservableLongMeasurement> callback =
        measurement -> {
          if (inProgress.compareAndSet(false, true)) {
            measurement.record(1);
          }
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          inProgress.set(false);
        };
    meterProvider.get("meter").counterBuilder("counter").buildWithCallback(callback);

    List<Future<?>> futures = new ArrayList<>();
    List<MetricData> allMetricData = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    try {
      futures.add(executorService.submit(() -> allMetricData.addAll(reader1.collectAllMetrics())));
      futures.add(executorService.submit(() -> allMetricData.addAll(reader1.collectAllMetrics())));
      futures.add(executorService.submit(() -> allMetricData.addAll(reader2.collectAllMetrics())));
      futures.add(executorService.submit(() -> allMetricData.addAll(reader2.collectAllMetrics())));
      for (Future<?> future : futures) {
        future.get(10, TimeUnit.SECONDS);
      }

      // If callback is in invoked concurrently, only one metric data will be reported
      assertThat(allMetricData)
          .hasSize(4)
          .allSatisfy(
              metricData ->
                  assertThat(metricData)
                      .hasInstrumentationScope(InstrumentationScopeInfo.create("meter"))
                      .hasLongSumSatisfying(
                          sum -> sum.hasPointsSatisfying(point -> point.hasValue(1))));
    } finally {
      executorService.shutdown();
    }
  }

  @Test
  void viewSdk_filterAttributes() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
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
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasAttributes(attributeEntry("allowed", "bear")))));
  }

  @Test
  void viewSdk_AllowRenames() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
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
                    .hasDoubleGaugeSatisfying(gauge -> {}));
  }

  @Test
  void viewSdk_AllowMultipleViewsPerSynchronousInstrument() {
    InstrumentSelector selector = InstrumentSelector.builder().setName("test").build();
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(
                selector,
                View.builder()
                    .setName("not_test")
                    .setDescription("not_desc")
                    .setAggregation(Aggregation.explicitBucketHistogram(Collections.emptyList()))
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
                    .hasHistogramSatisfying(histogramAssert -> {}),
            metric ->
                assertThat(metric)
                    .hasName("not_test_2")
                    .hasDescription("not_desc_2")
                    .hasUnit("unit")
                    .hasDoubleSumSatisfying(sum -> {}));
  }

  @Test
  void viewSdk_AllowMultipleViewsPerAsynchronousInstrument() {
    InstrumentSelector selector = InstrumentSelector.builder().setName("test").build();
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
                    .setAggregation(Aggregation.lastValue())
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
                    .hasDoubleGaugeSatisfying(gauge -> {}),
            metric ->
                assertThat(metric)
                    .hasName("not_test_2")
                    .hasDescription("not_desc_2")
                    .hasUnit("unit")
                    .hasDoubleGaugeSatisfying(gauge -> {}));
  }

  @Test
  void viewSdk_capturesBaggageFromContext() {
    InstrumentSelector selector =
        InstrumentSelector.builder().setType(InstrumentType.COUNTER).setName("test").build();
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder viewBuilder = View.builder().setAggregation(Aggregation.sum());
    SdkMeterProviderUtil.appendAllBaggageAttributes(viewBuilder);
    SdkMeterProvider provider =
        sdkMeterProviderBuilder
            .registerMetricReader(reader)
            .registerView(selector, viewBuilder.build())
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
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point.hasAttributes(attributeEntry("baggage", "value")))));
  }

  @Test
  void sdkMeterProvider_supportsMultipleReadersCumulative() {
    InMemoryMetricReader reader1 = InMemoryMetricReader.create();
    InMemoryMetricReader reader2 = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder.registerMetricReader(reader1).registerMetricReader(reader2).build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    long startTime = testClock.now();
    Attributes attributes = Attributes.builder().put("key", "value").build();

    counter.add(1L);
    counter.add(1L, attributes);
    testClock.advance(Duration.ofSeconds(1));

    assertThat(reader1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(Attributes.empty()),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(attributes))));

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    // Reader 2 should see the measurements of Reader 1 plus the additional measurement
    assertThat(reader2.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(2)
                                            .hasAttributes(Attributes.empty()),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(attributes))));

    // Reader 1 should see updated cumulative values
    assertThat(reader1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(2)
                                            .hasAttributes(Attributes.empty()),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(attributes))));
  }

  @Test
  void sdkMeterProvider_supportsMultipleReadersDelta() {
    InMemoryMetricReader reader1 = InMemoryMetricReader.createDelta();
    InMemoryMetricReader reader2 = InMemoryMetricReader.createDelta();
    SdkMeterProvider meterProvider =
        sdkMeterProviderBuilder.registerMetricReader(reader1).registerMetricReader(reader2).build();
    Meter sdkMeter = meterProvider.get(SdkMeterProviderTest.class.getName());
    LongCounter counter = sdkMeter.counterBuilder("testSum").build();
    long startTime = testClock.now();
    Attributes attributes = Attributes.builder().put("key", "value").build();

    counter.add(1L);
    counter.add(1L, attributes);
    testClock.advance(Duration.ofSeconds(1));

    assertThat(reader1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(Attributes.empty()),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(attributes))));
    long collectorOneTimeOne = testClock.now();

    counter.add(1L);
    testClock.advance(Duration.ofSeconds(1));

    // Reader 2 should see the measurements of Reader 1 plus the additional measurement
    assertThat(reader2.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(2)
                                            .hasAttributes(Attributes.empty()),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1)
                                            .hasAttributes(attributes))));

    // Reader 1 should only see diff since its last collect
    assertThat(reader1.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasName("testSum")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(collectorOneTimeOne)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(1))));
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
    when(metricReader.getDefaultAggregation(any())).thenCallRealMethod();
    when(metricReader.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode result =
        SdkMeterProvider.builder()
            .registerMetricReader(metricReader)
            .build()
            .shutdown()
            .join(10, TimeUnit.SECONDS);

    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void resetForTest() {
    InMemoryMetricReader reader1 = InMemoryMetricReader.createDelta();
    InMemoryMetricReader reader2 = InMemoryMetricReader.create();

    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader1)
            .registerMetricReader(reader2)
            .registerView(
                InstrumentSelector.builder().setName("counter").build(),
                View.builder().setName("new-counter").build())
            .build();

    Meter meter = meterProvider.get("meter");

    // Create both synchronous and asynchronous instruments
    LongCounter counter = meter.counterBuilder("counter").build();
    counter.add(1);
    meter.counterBuilder("async-counter").buildWithCallback(observable -> observable.record(1));

    assertThat(reader1.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("new-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isDelta().hasPointsSatisfying(point -> point.hasValue(1))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isDelta().hasPointsSatisfying(point -> point.hasValue(1))));
    assertThat(reader2.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("new-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isCumulative().hasPointsSatisfying(point -> point.hasValue(1))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isCumulative().hasPointsSatisfying(point -> point.hasValue(1))));

    // Reset the meter provider and confirm empty collections
    SdkMeterProviderUtil.resetForTest(meterProvider);

    counter.add(1);

    assertThat(reader1.collectAllMetrics()).isEmpty();
    assertThat(reader2.collectAllMetrics()).isEmpty();

    // Create new instruments and confirm valid collections, including view configuration
    counter = meter.counterBuilder("counter").build();
    counter.add(1);
    meter.counterBuilder("async-counter").buildWithCallback(observable -> observable.record(1));

    assertThat(reader1.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("new-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isDelta().hasPointsSatisfying(point -> point.hasValue(1))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isDelta().hasPointsSatisfying(point -> point.hasValue(1))));
    assertThat(reader2.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("new-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isCumulative().hasPointsSatisfying(point -> point.hasValue(1))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter")
                    .hasLongSumSatisfying(
                        sum -> sum.isCumulative().hasPointsSatisfying(point -> point.hasValue(1))));
  }

  private static ScopeConfigurator<MeterConfig> flipConfigurator(boolean enabled) {
    return scopeInfo -> enabled ? MeterConfig.disabled() : MeterConfig.enabled();
  }

  @Test
  void propagatesEnablementToMeterDirectly() {
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
    SdkMeter meter = (SdkMeter) meterProvider.get("test");
    boolean isEnabled = meter.isMeterEnabled();

    meterProvider.setMeterConfigurator(flipConfigurator(isEnabled));

    Assertions.assertThat(meter.isMeterEnabled()).isEqualTo(!isEnabled);
  }

  @Test
  void propagatesEnablementToMeterByUtil() {
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();
    SdkMeter sdkMeter = (SdkMeter) sdkMeterProvider.get("test");
    boolean isEnabled = sdkMeter.isMeterEnabled();

    SdkMeterProviderUtil.setMeterConfigurator(sdkMeterProvider, flipConfigurator(isEnabled));

    Assertions.assertThat(sdkMeter.isMeterEnabled()).isEqualTo(!isEnabled);
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
