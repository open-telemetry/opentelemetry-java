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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
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
  private final InMemoryMetricReader reader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(reader)
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
    assertThat(reader.collectAllMetrics()).isEmpty();
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
    assertThat(reader.collectAllMetrics())
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
    assertThat(reader.collectAllMetrics())
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
    assertThat(reader.collectAllMetrics())
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

  /**
   * Verify that the exponential histogram scale behaves properly after collection with delta and
   * cumulative readers.
   */
  @Test
  void collectMetrics_ExponentialHistogramScaleResets() {
    InMemoryMetricReader deltaReader = InMemoryMetricReader.createDelta();
    InMemoryMetricReader cumulativeReader = InMemoryMetricReader.create();

    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(cumulativeReader)
            .registerMetricReader(deltaReader)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder()
                    .setAggregation(Aggregation.base2ExponentialBucketHistogram(5, 20))
                    .build())
            .build();

    Meter meter = sdkMeterProvider.get(getClass().getName());

    LongHistogram histogram = meter.histogramBuilder("histogram").ofLongs().build();
    Attributes attributes1 = Attributes.builder().put("key", "value1").build();
    Attributes attributes2 = Attributes.builder().put("key", "value2").build();

    // Record 2 measurement to attributes1 such that scale changes, 1 measurement to attributes2
    // such that scale doesn't change.
    histogram.record(1, attributes1);
    histogram.record(10, attributes1);
    histogram.record(1, attributes2);

    // Both deltaReader and cumulativeReader should read out the same values, with the attributes1
    // point having a scale that accommodates the range of measurements.
    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasExponentialHistogramSatisfying(
                        expHistogram ->
                            expHistogram
                                .isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(attributes1)
                                            .hasScale(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Arrays.asList(1L, 0L, 0L, 0L, 1L))),
                                    point ->
                                        point
                                            .hasAttributes(attributes2)
                                            .hasScale(20)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Collections.singletonList(1L))))));
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasExponentialHistogramSatisfying(
                        expHistogram ->
                            expHistogram
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(attributes1)
                                            .hasScale(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Arrays.asList(1L, 0L, 0L, 0L, 1L))),
                                    point ->
                                        point
                                            .hasAttributes(attributes2)
                                            .hasScale(20)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Collections.singletonList(1L))))));

    // Record 1 measurement to attributes1 such that scale doesn't change, 2 measurement to
    // attributes2 such that scale changes.
    histogram.record(1, attributes1);
    histogram.record(1, attributes2);
    histogram.record(10, attributes2);

    // The deltaReader should have points reflecting a reset to the maxScale. The attributes1 point
    // now has scale 20, the attributes2 point now has scale 0 to accommodate the range of
    // measurements.
    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasExponentialHistogramSatisfying(
                        expHistogram ->
                            expHistogram
                                .isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(attributes1)
                                            .hasScale(20)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(Collections.singletonList(1L))),
                                    point ->
                                        point
                                            .hasAttributes(attributes2)
                                            .hasScale(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Arrays.asList(1L, 0L, 0L, 0L, 1L))))));
    // The cumulativeReader does not reset after collection, so both attributes1 and attributes2
    // points should now have scale 0.
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasExponentialHistogramSatisfying(
                        expHistogram ->
                            expHistogram
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(attributes1)
                                            .hasScale(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Arrays.asList(2L, 0L, 0L, 0L, 1L))),
                                    point ->
                                        point
                                            .hasAttributes(attributes2)
                                            .hasScale(0)
                                            .hasPositiveBucketsSatisfying(
                                                buckets ->
                                                    buckets
                                                        .hasOffset(-1)
                                                        .hasCounts(
                                                            Arrays.asList(2L, 0L, 0L, 0L, 1L))))));
  }

  @Test
  void collectMetrics_ExponentialHistogramAggregation() {
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .setResource(RESOURCE)
            .setClock(testClock)
            .registerMetricReader(reader)
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
    assertThat(reader.collectAllMetrics())
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
    assertThat(reader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
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
    LongHistogram histogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();

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
    LongHistogram histogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();

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
