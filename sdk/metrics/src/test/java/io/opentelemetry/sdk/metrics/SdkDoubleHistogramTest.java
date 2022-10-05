/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundDoubleHistogram;
import io.opentelemetry.sdk.metrics.internal.view.ExponentialHistogramAggregation;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.assertj.MetricAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link SdkDoubleHistogram}. */
class SdkDoubleHistogramTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkDoubleHistogramTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(sdkMeterReader)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(SdkDoubleHistogram.class);

  @Test
  void record_PreventNullAttributes() {
    assertThatThrownBy(() -> sdkMeter.histogramBuilder("testHistogram").build().record(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void bound_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                ((SdkDoubleHistogram) sdkMeter.histogramBuilder("testHistogram").build())
                    .bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleHistogram doubleHistogram = sdkMeter.histogramBuilder("testHistogram").build();
    BoundDoubleHistogram bound =
        ((SdkDoubleHistogram) doubleHistogram)
            .bind(Attributes.builder().put("key", "value").build());
    try {
      assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    DoubleHistogram doubleHistogram =
        sdkMeter
            .histogramBuilder("testHistogram")
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleHistogram.record(12d, Attributes.empty());
    doubleHistogram.record(12d);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasCount(2)
                                            .hasSum(24)
                                            .hasBucketBoundaries(
                                                0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000,
                                                2_500, 5_000, 7_500, 10_000)
                                            .hasBucketCounts(
                                                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleHistogram doubleHistogram = sdkMeter.histogramBuilder("testHistogram").build();
    BoundDoubleHistogram bound =
        ((SdkDoubleHistogram) doubleHistogram).bind(Attributes.builder().put("K", "V").build());
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleHistogram.record(9.1d, Attributes.empty());
      bound.record(123.3d);
      doubleHistogram.record(13.1d, Attributes.empty());
      // Advancing time here should not matter.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(321.5d);
      doubleHistogram.record(121.5d, Attributes.builder().put("K", "V").build());
      assertThat(sdkMeterReader.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                      .hasName("testHistogram")
                      .hasHistogramSatisfying(
                          histogram ->
                              histogram.hasPointsSatisfying(
                                  point ->
                                      point
                                          .hasStartEpochNanos(startTime)
                                          .hasEpochNanos(testClock.now())
                                          .hasCount(3)
                                          .hasSum(566.3d)
                                          .hasBucketCounts(
                                              0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0)
                                          .hasAttributes(attributeEntry("K", "V")),
                                  point ->
                                      point
                                          .hasStartEpochNanos(startTime)
                                          .hasEpochNanos(testClock.now())
                                          .hasCount(2)
                                          .hasSum(22.2d)
                                          .hasBucketCounts(
                                              0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                          .hasAttributes(Attributes.empty()))));

      // Histograms are cumulative by default.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(222d);
      doubleHistogram.record(17d, Attributes.empty());
      assertThat(sdkMeterReader.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                      .hasName("testHistogram")
                      .hasHistogramSatisfying(
                          histogram ->
                              histogram.hasPointsSatisfying(
                                  point ->
                                      point
                                          .hasStartEpochNanos(startTime)
                                          .hasEpochNanos(testClock.now())
                                          .hasCount(4)
                                          .hasSum(788.3)
                                          .hasBucketCounts(
                                              0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0)
                                          .hasAttributes(attributeEntry("K", "V")),
                                  point ->
                                      point
                                          .hasStartEpochNanos(startTime)
                                          .hasEpochNanos(testClock.now())
                                          .hasCount(3)
                                          .hasSum(39.2)
                                          .hasBucketCounts(
                                              0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                          .hasAttributes(Attributes.empty()))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_ExponentialHistogramAggregation() {
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .setResource(RESOURCE)
            .setClock(testClock)
            .registerMetricReader(sdkMeterReader)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder().setAggregation(ExponentialHistogramAggregation.create(5)).build())
            .build();
    DoubleHistogram doubleHistogram =
        sdkMeterProvider
            .get(SdkDoubleHistogramTest.class.getName())
            .histogramBuilder("testHistogram")
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleHistogram.record(12d, Attributes.builder().put("key", "value").build());
    doubleHistogram.record(12d);
    doubleHistogram.record(13d);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                MetricAssertions.assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasExponentialHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point -> {
                          MetricAssertions.assertThat(point)
                              .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                              .hasEpochNanos(testClock.now())
                              .hasAttributes(Attributes.empty())
                              .hasCount(2)
                              .hasSum(25)
                              .hasMin(12)
                              .hasMax(13)
                              .hasScale(5)
                              .hasZeroCount(0);
                          MetricAssertions.assertThat(point.getPositiveBuckets())
                              .hasOffset(114)
                              .hasCounts(Arrays.asList(1L, 0L, 0L, 0L, 1L));
                          MetricAssertions.assertThat(point.getNegativeBuckets())
                              .hasOffset(0)
                              .hasCounts(Collections.emptyList());
                        },
                        point -> {
                          MetricAssertions.assertThat(point)
                              .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                              .hasEpochNanos(testClock.now())
                              .hasAttributes(Attributes.builder().put("key", "value").build())
                              .hasCount(1)
                              .hasSum(12)
                              .hasMin(12)
                              .hasMax(12)
                              .hasScale(20)
                              .hasZeroCount(0);
                          MetricAssertions.assertThat(point.getPositiveBuckets())
                              .hasOffset(3759105)
                              .hasCounts(Collections.singletonList(1L));
                          MetricAssertions.assertThat(point.getNegativeBuckets())
                              .hasOffset(0)
                              .hasCounts(Collections.emptyList());
                        }));
  }

  @Test
  @SuppressLogger(SdkDoubleHistogram.class)
  void doubleHistogramRecord_NonNegativeCheck() {
    DoubleHistogram histogram = sdkMeter.histogramBuilder("testHistogram").build();
    histogram.record(-45);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
  }

  @Test
  @SuppressLogger(SdkDoubleHistogram.class)
  void boundDoubleHistogramRecord_MonotonicityCheck() {
    DoubleHistogram histogram = sdkMeter.histogramBuilder("testHistogram").build();
    BoundDoubleHistogram bound = ((SdkDoubleHistogram) histogram).bind(Attributes.empty());
    try {
      bound.record(-9);
      assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
      logs.assertContains(
          "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    DoubleHistogram doubleHistogram = sdkMeter.histogramBuilder("testHistogram").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkDoubleHistogram) doubleHistogram)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new SdkDoubleHistogramTest.OperationUpdaterDirectCall(doubleHistogram, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(
                  ((SdkDoubleHistogram) doubleHistogram)
                      .bind(Attributes.builder().put("K", "V").build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(attributeEntry("K", "V"))
                                        .hasCount(8_000)
                                        .hasSum(80_000))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    DoubleHistogram doubleHistogram = sdkMeter.histogramBuilder("testHistogram").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkDoubleHistogram) doubleHistogram)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new SdkDoubleHistogramTest.OperationUpdaterDirectCall(
                  doubleHistogram, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(
                  ((SdkDoubleHistogram) doubleHistogram)
                      .bind(Attributes.builder().put(keys[i], values[i]).build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(4_000)
                                        .hasSum(40_000)
                                        .hasBucketCounts(
                                            0, 0, 2000, 2000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[0], values[0])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(4_000)
                                        .hasSum(40_000)
                                        .hasBucketCounts(
                                            0, 0, 2000, 2000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[1], values[1])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(4_000)
                                        .hasSum(40_000)
                                        .hasBucketCounts(
                                            0, 0, 2000, 2000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[2], values[2])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(4_000)
                                        .hasSum(40_000)
                                        .hasBucketCounts(
                                            0, 0, 2000, 2000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[3], values[3])))));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundDoubleHistogram boundDoubleHistogram;

    private OperationUpdaterWithBinding(BoundDoubleHistogram boundDoubleHistogram) {
      this.boundDoubleHistogram = boundDoubleHistogram;
    }

    @Override
    void update() {
      boundDoubleHistogram.record(11.0);
    }

    @Override
    void cleanup() {
      boundDoubleHistogram.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {
    private final DoubleHistogram doubleHistogram;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(DoubleHistogram doubleHistogram, String key, String value) {
      this.doubleHistogram = doubleHistogram;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleHistogram.record(9.0, Attributes.builder().put(key, value).build());
    }

    @Override
    void cleanup() {}
  }
}
