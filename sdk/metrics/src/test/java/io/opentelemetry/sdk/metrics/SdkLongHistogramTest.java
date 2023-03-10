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
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link SdkLongHistogram}. */
class SdkLongHistogramTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkLongHistogramTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(sdkMeterReader)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(SdkLongHistogram.class);

  @Test
  void record_PreventNullAttributes() {
    assertThatThrownBy(
            () -> sdkMeter.histogramBuilder("testHistogram").ofLongs().build().record(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    LongHistogram longHistogram =
        sdkMeter
            .histogramBuilder("testHistogram")
            .ofLongs()
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longHistogram.record(12, Attributes.empty());
    longHistogram.record(12);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDescription("description")
                    .hasUnit("By")
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
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    longHistogram.record(9, Attributes.empty());
    longHistogram.record(123, Attributes.builder().put("K", "V").build());
    longHistogram.record(14, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longHistogram.record(321, Attributes.builder().put("K", "V").build());
    longHistogram.record(1, Attributes.builder().put("K", "V").build());
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
                                        .hasSum(445)
                                        .hasBucketCounts(
                                            0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry("K", "V")),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(2)
                                        .hasSum(23)
                                        .hasBucketCounts(
                                            0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(Attributes.empty()))));

    // Histograms are cumulative by default.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longHistogram.record(222, Attributes.builder().put("K", "V").build());
    longHistogram.record(17, Attributes.empty());
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
                                        .hasSum(667)
                                        .hasBucketCounts(
                                            0, 1, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry("K", "V")),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(3)
                                        .hasSum(40)
                                        .hasBucketCounts(
                                            0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(Attributes.empty()))));
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
                View.builder()
                    .setAggregation(Aggregation.base2ExponentialBucketHistogram(5, 20))
                    .build())
            .build();
    LongHistogram longHistogram =
        sdkMeterProvider
            .get(getClass().getName())
            .histogramBuilder("testHistogram")
            .setDescription("description")
            .setUnit("ms")
            .ofLongs()
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longHistogram.record(12L, Attributes.builder().put("key", "value").build());
    longHistogram.record(12L);
    longHistogram.record(13L);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasExponentialHistogramSatisfying(
                        expHistogram ->
                            expHistogram
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasCount(2)
                                            .hasSum(25)
                                            .hasMin(12)
                                            .hasMax(13)
                                            .hasScale(5)
                                            .hasZeroCount(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(114)
                                                        .hasCounts(
                                                            Arrays.asList(1L, 0L, 0L, 0L, 1L)))
                                            .hasNegativeBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(0)
                                                        .hasCounts(Collections.emptyList())),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(
                                                Attributes.builder().put("key", "value").build())
                                            .hasCount(1)
                                            .hasSum(12)
                                            .hasMin(12)
                                            .hasMax(12)
                                            .hasScale(20)
                                            .hasZeroCount(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(3759105)
                                                        .hasCounts(Collections.singletonList(1L)))
                                            .hasNegativeBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(0)
                                                        .hasCounts(Collections.emptyList())))));
  }

  @Test
  @SuppressLogger(SdkLongHistogram.class)
  void longHistogramRecord_NonNegativeCheck() {
    LongHistogram histogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    histogram.record(-45);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
  }

  @Test
  void stressTest() {
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              () -> longHistogram.record(10, Attributes.builder().put("K", "V").build())));
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
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    IntStream.range(0, 4)
        .forEach(
            i ->
                stressTestBuilder.addOperation(
                    StressTestRunner.Operation.create(
                        1_000,
                        2,
                        () ->
                            longHistogram.record(
                                10, Attributes.builder().put(keys[i], values[i]).build()))));

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
                                        .hasCount(1_000)
                                        .hasSum(10_000)
                                        .hasBucketCounts(
                                            0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[0], values[0])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(1_000)
                                        .hasSum(10_000)
                                        .hasBucketCounts(
                                            0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[1], values[1])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(1_000)
                                        .hasSum(10_000)
                                        .hasBucketCounts(
                                            0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[2], values[2])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasCount(1_000)
                                        .hasSum(10_000)
                                        .hasBucketCounts(
                                            0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasAttributes(attributeEntry(keys[3], values[3])))));
  }
}
