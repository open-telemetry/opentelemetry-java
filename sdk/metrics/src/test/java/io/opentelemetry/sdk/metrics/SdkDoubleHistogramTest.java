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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.state.DefaultSynchronousMetricStorage;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
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
  void collectMetrics_NoRecords() {
    sdkMeter.histogramBuilder("testHistogram").build();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
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
    doubleHistogram.record(9.1d, Attributes.empty());
    doubleHistogram.record(123.3d, Attributes.builder().put("K", "V").build());
    doubleHistogram.record(13.1d, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleHistogram.record(321.5d, Attributes.builder().put("K", "V").build());
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
    doubleHistogram.record(222d, Attributes.builder().put("K", "V").build());
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
  @SuppressLogger(SdkDoubleHistogram.class)
  void doubleHistogramRecord_NonNegativeCheck() {
    DoubleHistogram histogram = sdkMeter.histogramBuilder("testHistogram").build();
    histogram.record(-45);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
  }

  @Test
  @SuppressLogger(DefaultSynchronousMetricStorage.class)
  void doubleHistogramRecord_NaN() {
    DoubleHistogram histogram = sdkMeter.histogramBuilder("testHistogram").build();
    histogram.record(Double.NaN);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void collectMetrics_ExemplarsWithExponentialHistogram() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .setResource(RESOURCE)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder()
                    .setAggregation(Aggregation.base2ExponentialBucketHistogram())
                    .setAttributeFilter(Collections.emptySet())
                    .build())
            .registerMetricReader(reader)
            .build();
    Meter sdkMeter = sdkMeterProvider.get(getClass().getName());
    DoubleHistogram histogram = sdkMeter.histogramBuilder("testHistogram").build();

    SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();
    Tracer tracer = tracerProvider.get("foo");

    Span span = tracer.spanBuilder("span").startSpan();
    try (Scope unused = span.makeCurrent()) {
      histogram.record(10, Attributes.builder().put("key", "value").build());
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasExponentialHistogramSatisfying(
                        exponentialHistogram ->
                            exponentialHistogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(10.0)
                                        .hasAttributes(Attributes.empty())
                                        .hasExemplarsSatisfying(
                                            exemplar ->
                                                exemplar
                                                    .hasValue(10.0)
                                                    .hasFilteredAttributes(
                                                        Attributes.builder()
                                                            .put("key", "value")
                                                            .build())))));
  }

  @Test
  void collectMetrics_ExemplarsWithExplicitBucketHistogram() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .setResource(RESOURCE)
            .registerView(
                InstrumentSelector.builder().setName("*").build(),
                View.builder().setAttributeFilter(Collections.emptySet()).build())
            .registerMetricReader(reader)
            .build();
    Meter sdkMeter = sdkMeterProvider.get(getClass().getName());
    DoubleHistogram histogram = sdkMeter.histogramBuilder("testHistogram").build();

    SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();
    Tracer tracer = tracerProvider.get("foo");

    Span span = tracer.spanBuilder("span").startSpan();
    try (Scope unused = span.makeCurrent()) {
      histogram.record(10, Attributes.builder().put("key", "value").build());
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasHistogramSatisfying(
                        explicitHistogram ->
                            explicitHistogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(10)
                                        .hasAttributes(Attributes.empty())
                                        .hasExemplarsSatisfying(
                                            exemplar ->
                                                exemplar
                                                    .hasValue(10.0)
                                                    .hasFilteredAttributes(
                                                        Attributes.builder()
                                                            .put("key", "value")
                                                            .build())))));
  }
}
