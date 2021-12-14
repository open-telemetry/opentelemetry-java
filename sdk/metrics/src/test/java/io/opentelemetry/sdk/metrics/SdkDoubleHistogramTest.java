/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundDoubleHistogram;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for SDK {@link InstrumentValueType#DOUBLE} {@link InstrumentType#HISTOGRAM}. */
class SdkDoubleHistogramTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkDoubleHistogramTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void record_PreventNullAttributes() {
    assertThatThrownBy(() -> sdkMeter.histogramBuilder("testRecorder").build().record(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void bound_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                ((SdkDoubleHistogram) sdkMeter.histogramBuilder("testRecorder").build()).bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleHistogram doubleRecorder = sdkMeter.histogramBuilder("testRecorder").build();
    BoundDoubleHistogram bound =
        ((SdkDoubleHistogram) doubleRecorder)
            .bind(Attributes.builder().put("key", "value").build());
    try {
      sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.getFinishedMetricItems()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    DoubleHistogram doubleRecorder =
        sdkMeter
            .histogramBuilder("testRecorder")
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleRecorder.record(12d, Attributes.empty());
    doubleRecorder.record(12d);
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testRecorder")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasDoubleHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasCount(2)
                                .hasSum(24)
                                .hasBucketBoundaries(
                                    5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500, 5_000,
                                    7_500, 10_000)
                                .hasBucketCounts(0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleHistogram doubleRecorder = sdkMeter.histogramBuilder("testRecorder").build();
    BoundDoubleHistogram bound =
        ((SdkDoubleHistogram) doubleRecorder).bind(Attributes.builder().put("K", "V").build());
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleRecorder.record(12.1d, Attributes.empty());
      bound.record(123.3d);
      doubleRecorder.record(-13.1d, Attributes.empty());
      // Advancing time here should not matter.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(321.5d);
      doubleRecorder.record(-121.5d, Attributes.builder().put("K", "V").build());
      sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.getFinishedMetricItems())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testRecorder")
                      .hasDoubleHistogram()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point)
                                  .hasCount(3)
                                  .hasSum(323.3d)
                                  .hasBucketCounts(1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.builder().put("K", "V").build()),
                          point ->
                              assertThat(point)
                                  .hasCount(2)
                                  .hasSum(-1.0d)
                                  .hasBucketCounts(1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.empty())));
      exporter.reset();

      // Histograms are cumulative by default.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(222d);
      doubleRecorder.record(17d, Attributes.empty());
      sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.getFinishedMetricItems())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testRecorder")
                      .hasDoubleHistogram()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point)
                                  .hasCount(4)
                                  .hasSum(545.3)
                                  .hasBucketCounts(1, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.builder().put("K", "V").build()),
                          point ->
                              assertThat(point)
                                  .hasCount(3)
                                  .hasSum(16)
                                  .hasBucketCounts(1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.empty())));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final DoubleHistogram doubleRecorder = sdkMeter.histogramBuilder("testRecorder").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkDoubleHistogram) doubleRecorder)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new SdkDoubleHistogramTest.OperationUpdaterDirectCall(doubleRecorder, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(
                  ((SdkDoubleHistogram) doubleRecorder)
                      .bind(Attributes.builder().put("K", "V").build()))));
    }

    stressTestBuilder.build().run();
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testRecorder")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.of(stringKey("K"), "V"))
                                .hasCount(8_000)
                                .hasSum(80_000)));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleHistogram doubleRecorder = sdkMeter.histogramBuilder("testRecorder").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkDoubleHistogram) doubleRecorder)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new SdkDoubleHistogramTest.OperationUpdaterDirectCall(
                  doubleRecorder, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(
                  ((SdkDoubleHistogram) doubleRecorder)
                      .bind(Attributes.builder().put(keys[i], values[i]).build()))));
    }

    stressTestBuilder.build().run();
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testRecorder")
                    .hasDoubleHistogram()
                    .points()
                    .allSatisfy(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasCount(4_000)
                                .hasSum(40_000)
                                .hasBucketCounts(0, 2000, 2000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
                    .extracting(point -> point.getAttributes())
                    .containsExactlyInAnyOrder(
                        Attributes.of(stringKey(keys[0]), values[0]),
                        Attributes.of(stringKey(keys[1]), values[1]),
                        Attributes.of(stringKey(keys[2]), values[2]),
                        Attributes.of(stringKey(keys[3]), values[3])));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundDoubleHistogram boundDoubleValueRecorder;

    private OperationUpdaterWithBinding(BoundDoubleHistogram boundDoubleValueRecorder) {
      this.boundDoubleValueRecorder = boundDoubleValueRecorder;
    }

    @Override
    void update() {
      boundDoubleValueRecorder.record(11.0);
    }

    @Override
    void cleanup() {
      boundDoubleValueRecorder.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {
    private final DoubleHistogram doubleValueRecorder;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(
        DoubleHistogram doubleValueRecorder, String key, String value) {
      this.doubleValueRecorder = doubleValueRecorder;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleValueRecorder.record(9.0, Attributes.builder().put(key, value).build());
    }

    @Override
    void cleanup() {}
  }
}
