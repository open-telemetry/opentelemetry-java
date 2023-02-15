/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AdviceTest {

  private SdkMeterProvider meterProvider = SdkMeterProvider.builder().build();

  @AfterEach
  void cleanup() {
    meterProvider.close();
  }

  @Test
  void histogramWithoutAdvice() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();

    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    DoubleHistogram doubleHistogram =
        meterProvider.get("meter").histogramBuilder("histogram").build();

    doubleHistogram.record(5.0);
    doubleHistogram.record(15.0);
    doubleHistogram.record(25.0);
    doubleHistogram.record(35.0);

    // Should use default bucket bounds
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasBucketCounts(
                                            0, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                        .hasBucketBoundaries(
                                            0d, 5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d,
                                            1_000d, 2_500d, 5_000d, 7_500d, 10_000d))));
  }

  @Test
  void histogramWithAdvice() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    DoubleHistogram doubleHistogram =
        meterProvider
            .get("meter")
            .histogramBuilder("histogram")
            .setAggregationAdvice(
                advice -> advice.setExplicitBucketBoundaries(Arrays.asList(10.0, 20.0, 30.0)))
            .build();

    doubleHistogram.record(5.0);
    doubleHistogram.record(15.0);
    doubleHistogram.record(25.0);
    doubleHistogram.record(35.0);

    // Bucket bounds from advice should be used
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasBucketCounts(1, 1, 1, 1)
                                        .hasBucketBoundaries(10.0, 20.0, 30.0))));
  }

  @Test
  void histogramWithAdviceAndViews() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder()
                    .setAggregation(
                        Aggregation.explicitBucketHistogram(Collections.singletonList(50.0)))
                    .build())
            .build();
    DoubleHistogram doubleHistogram =
        meterProvider
            .get("meter")
            .histogramBuilder("histogram")
            .setAggregationAdvice(
                advice -> advice.setExplicitBucketBoundaries(Arrays.asList(10.0, 20.0, 30.0)))
            .build();

    doubleHistogram.record(5.0);
    doubleHistogram.record(15.0);
    doubleHistogram.record(25.0);
    doubleHistogram.record(35.0);

    // View should take priority over bucket bounds from advice
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point -> point.hasBucketCounts(4, 0).hasBucketBoundaries(50.0))));
  }

  @Test
  void histogramWithAdviceAndReaderAggregationPreference() {
    InMemoryMetricReader reader =
        InMemoryMetricReader.create(
            AggregationTemporalitySelector.alwaysCumulative(),
            DefaultAggregationSelector.getDefault()
                .with(
                    InstrumentType.HISTOGRAM,
                    io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram(
                        Collections.singletonList(50.0))));
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    DoubleHistogram doubleHistogram =
        meterProvider
            .get("meter")
            .histogramBuilder("histogram")
            .setAggregationAdvice(
                advice -> advice.setExplicitBucketBoundaries(Arrays.asList(10.0, 20.0, 30.0)))
            .build();

    doubleHistogram.record(5.0);
    doubleHistogram.record(15.0);
    doubleHistogram.record(25.0);
    doubleHistogram.record(35.0);

    // Reader aggregation preference should take priority over bucket bounds from advice
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point -> point.hasBucketCounts(4, 0).hasBucketBoundaries(50.0))));
  }
}
