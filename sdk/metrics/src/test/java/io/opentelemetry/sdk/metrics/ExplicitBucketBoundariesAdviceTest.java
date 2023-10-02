/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.extension.incubator.metrics.ExtendedDoubleHistogramBuilder;
import io.opentelemetry.extension.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExplicitBucketBoundariesAdviceTest {

  private SdkMeterProvider meterProvider = SdkMeterProvider.builder().build();

  @AfterEach
  void cleanup() {
    meterProvider.close();
  }

  @ParameterizedTest
  @MethodSource("histogramsWithoutAdvice")
  void histogramWithoutAdvice(Function<SdkMeterProvider, Consumer<Long>> histogramBuilder) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    Consumer<Long> histogramRecorder = histogramBuilder.apply(meterProvider);
    histogramRecorder.accept(5L);
    histogramRecorder.accept(15L);
    histogramRecorder.accept(25L);
    histogramRecorder.accept(35L);

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

  @ParameterizedTest
  @MethodSource("histogramsWithAdvice")
  void histogramWithAdvice(Function<SdkMeterProvider, Consumer<Long>> histogramBuilder) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    Consumer<Long> histogramRecorder = histogramBuilder.apply(meterProvider);
    histogramRecorder.accept(5L);
    histogramRecorder.accept(15L);
    histogramRecorder.accept(25L);
    histogramRecorder.accept(35L);

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

  @ParameterizedTest
  @MethodSource("histogramsWithAdvice")
  void histogramWithAdviceAndViews(Function<SdkMeterProvider, Consumer<Long>> histogramBuilder) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder()
                    .setAggregation(explicitBucketHistogram(Collections.singletonList(50.0)))
                    .build())
            .build();

    Consumer<Long> histogramRecorder = histogramBuilder.apply(meterProvider);
    histogramRecorder.accept(5L);
    histogramRecorder.accept(15L);
    histogramRecorder.accept(25L);
    histogramRecorder.accept(35L);

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

  @ParameterizedTest
  @MethodSource("histogramsWithAdvice")
  void histogramWithAdviceAndReaderAggregationPreference(
      Function<SdkMeterProvider, Consumer<Long>> histogramBuilder) {
    InMemoryMetricReader reader =
        InMemoryMetricReader.create(
            AggregationTemporalitySelector.alwaysCumulative(),
            DefaultAggregationSelector.getDefault()
                .with(
                    InstrumentType.HISTOGRAM,
                    explicitBucketHistogram(Collections.singletonList(50.0))));
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    Consumer<Long> histogramRecorder = histogramBuilder.apply(meterProvider);
    histogramRecorder.accept(5L);
    histogramRecorder.accept(15L);
    histogramRecorder.accept(25L);
    histogramRecorder.accept(35L);

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

  private static Stream<Arguments> histogramsWithoutAdvice() {
    return Stream.of(
        Arguments.of(
            (Function<SdkMeterProvider, Consumer<Long>>)
                meterProvider -> {
                  DoubleHistogram build =
                      meterProvider.get("meter").histogramBuilder("histogram").build();
                  return build::record;
                }),
        Arguments.of(
            (Function<SdkMeterProvider, Consumer<Long>>)
                meterProvider -> {
                  LongHistogram build =
                      meterProvider.get("meter").histogramBuilder("histogram").ofLongs().build();
                  return build::record;
                }));
  }

  private static Stream<Arguments> histogramsWithAdvice() {
    return Stream.of(
        Arguments.of(
            (Function<SdkMeterProvider, Consumer<Long>>)
                meterProvider -> {
                  DoubleHistogram build =
                      ((ExtendedDoubleHistogramBuilder)
                              meterProvider.get("meter").histogramBuilder("histogram"))
                          .setAdvice(
                              advice ->
                                  advice.setExplicitBucketBoundaries(
                                      Arrays.asList(10.0, 20.0, 30.0)))
                          .build();
                  return build::record;
                }),
        Arguments.of(
            (Function<SdkMeterProvider, Consumer<Long>>)
                meterProvider -> {
                  LongHistogram build =
                      ((ExtendedLongHistogramBuilder)
                              meterProvider.get("meter").histogramBuilder("histogram").ofLongs())
                          .setAdvice(
                              advice ->
                                  advice.setExplicitBucketBoundaries(Arrays.asList(10L, 20L, 30L)))
                          .build();
                  return build::record;
                }));
  }
}
