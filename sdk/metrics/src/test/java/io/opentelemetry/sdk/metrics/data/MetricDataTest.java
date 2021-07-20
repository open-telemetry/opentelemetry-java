/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.opentelemetry.sdk.metrics.data.MetricData}. */
class MetricDataTest {
  private static final long START_EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(1000);
  private static final long EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(2000);
  private static final long LONG_VALUE = 10;
  private static final double DOUBLE_VALUE = 1.234;
  private static final AttributeKey<String> KEY = AttributeKey.stringKey("key");
  private static final ValueAtPercentile MINIMUM_VALUE =
      ValueAtPercentile.create(0.0, DOUBLE_VALUE);
  private static final ValueAtPercentile MAXIMUM_VALUE =
      ValueAtPercentile.create(100.0, DOUBLE_VALUE);
  private static final LongPointData LONG_POINT =
      LongPointData.create(START_EPOCH_NANOS, EPOCH_NANOS, Attributes.of(KEY, "value"), LONG_VALUE);
  private static final DoublePointData DOUBLE_POINT =
      DoublePointData.create(
          START_EPOCH_NANOS, EPOCH_NANOS, Attributes.of(KEY, "value"), DOUBLE_VALUE);
  private static final DoubleSummaryPointData SUMMARY_POINT =
      DoubleSummaryPointData.create(
          START_EPOCH_NANOS,
          EPOCH_NANOS,
          Attributes.of(KEY, "value"),
          LONG_VALUE,
          DOUBLE_VALUE,
          Arrays.asList(
              ValueAtPercentile.create(0.0, DOUBLE_VALUE),
              ValueAtPercentile.create(100, DOUBLE_VALUE)));
  private static final DoubleHistogramPointData HISTOGRAM_POINT =
      DoubleHistogramPointData.create(
          START_EPOCH_NANOS,
          EPOCH_NANOS,
          Attributes.of(KEY, "value"),
          DOUBLE_VALUE,
          ImmutableList.of(1.0),
          ImmutableList.of(1L, 1L));

  @Test
  void metricData_Getters() {
    MetricData metricData =
        MetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleGaugeData.create(Collections.emptyList()));
    assertThat(metricData.getName()).isEqualTo("metric_name");
    assertThat(metricData.getDescription()).isEqualTo("metric_description");
    assertThat(metricData.getUnit()).isEqualTo("ms");
    assertThat(metricData.getType()).isEqualTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(metricData.getResource()).isEqualTo(Resource.empty());
    assertThat(metricData.getInstrumentationLibraryInfo())
        .isEqualTo(InstrumentationLibraryInfo.empty());
    assertThat(metricData.isEmpty()).isTrue();
  }

  @Test
  void metricData_LongPoints() {
    assertThat(LONG_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(LONG_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(LONG_POINT.getAttributes().size()).isEqualTo(1);
    assertThat(LONG_POINT.getAttributes().get(KEY)).isEqualTo("value");
    assertThat(LONG_POINT.getValue()).isEqualTo(LONG_VALUE);
    MetricData metricData =
        MetricData.createLongGauge(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            LongGaugeData.create(Collections.singletonList(LONG_POINT)));
    assertThat(metricData.isEmpty()).isFalse();
    assertThat(metricData.getLongGaugeData().getPoints()).containsExactly(LONG_POINT);
    metricData =
        MetricData.createLongSum(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            LongSumData.create(
                /* isMonotonic= */ false,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(LONG_POINT)));
    assertThat(metricData.isEmpty()).isFalse();
    assertThat(metricData.getLongSumData().getPoints()).containsExactly(LONG_POINT);
  }

  @Test
  void metricData_DoublePoints() {
    assertThat(DOUBLE_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(DOUBLE_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(DOUBLE_POINT.getAttributes().size()).isEqualTo(1);
    assertThat(DOUBLE_POINT.getAttributes().get(KEY)).isEqualTo("value");
    assertThat(DOUBLE_POINT.getValue()).isEqualTo(DOUBLE_VALUE);
    MetricData metricData =
        MetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleGaugeData.create(Collections.singletonList(DOUBLE_POINT)));
    assertThat(metricData.isEmpty()).isFalse();
    assertThat(metricData.getDoubleGaugeData().getPoints()).containsExactly(DOUBLE_POINT);
    metricData =
        MetricData.createDoubleSum(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleSumData.create(
                /* isMonotonic= */ false,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(DOUBLE_POINT)));
    assertThat(metricData.isEmpty()).isFalse();
    assertThat(metricData.getDoubleSumData().getPoints()).containsExactly(DOUBLE_POINT);
  }

  @Test
  void metricData_SummaryPoints() {
    assertThat(SUMMARY_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(SUMMARY_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(SUMMARY_POINT.getAttributes().size()).isEqualTo(1);
    assertThat(SUMMARY_POINT.getAttributes().get(KEY)).isEqualTo("value");
    assertThat(SUMMARY_POINT.getCount()).isEqualTo(LONG_VALUE);
    assertThat(SUMMARY_POINT.getSum()).isEqualTo(DOUBLE_VALUE);
    assertThat(SUMMARY_POINT.getPercentileValues())
        .isEqualTo(Arrays.asList(MINIMUM_VALUE, MAXIMUM_VALUE));
    MetricData metricData =
        MetricData.createDoubleSummary(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleSummaryData.create(Collections.singletonList(SUMMARY_POINT)));
    assertThat(metricData.getDoubleSummaryData().getPoints()).containsExactly(SUMMARY_POINT);
  }

  @Test
  void metricData_HistogramPoints() {
    assertThat(HISTOGRAM_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(HISTOGRAM_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(HISTOGRAM_POINT.getAttributes().size()).isEqualTo(1);
    assertThat(HISTOGRAM_POINT.getAttributes().get(KEY)).isEqualTo("value");
    assertThat(HISTOGRAM_POINT.getCount()).isEqualTo(2L);
    assertThat(HISTOGRAM_POINT.getSum()).isEqualTo(DOUBLE_VALUE);
    assertThat(HISTOGRAM_POINT.getBoundaries()).isEqualTo(ImmutableList.of(1.0));
    assertThat(HISTOGRAM_POINT.getCounts()).isEqualTo(ImmutableList.of(1L, 1L));

    MetricData metricData =
        MetricData.createDoubleHistogram(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleHistogramData.create(
                AggregationTemporality.DELTA, Collections.singleton(HISTOGRAM_POINT)));
    assertThat(metricData.getDoubleHistogramData().getPoints()).containsExactly(HISTOGRAM_POINT);

    assertThatThrownBy(
            () ->
                DoubleHistogramPointData.create(
                    0, 0, Attributes.empty(), 0.0, ImmutableList.of(), ImmutableList.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                DoubleHistogramPointData.create(
                    0,
                    0,
                    Attributes.empty(),
                    0.0,
                    ImmutableList.of(1.0, 1.0),
                    ImmutableList.of(0L, 0L, 0L)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                DoubleHistogramPointData.create(
                    0,
                    0,
                    Attributes.empty(),
                    0.0,
                    ImmutableList.of(Double.NEGATIVE_INFINITY),
                    ImmutableList.of(0L, 0L)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void metricData_GetDefault() {
    MetricData metricData =
        MetricData.createDoubleSummary(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleSummaryData.create(Collections.singletonList(SUMMARY_POINT)));
    assertThat(metricData.getDoubleGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleSumData().getPoints()).isEmpty();
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleHistogramData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleSummaryData().getPoints()).containsExactly(SUMMARY_POINT);

    metricData =
        MetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationLibraryInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            DoubleGaugeData.create(Collections.singletonList(DOUBLE_POINT)));
    assertThat(metricData.getDoubleGaugeData().getPoints()).containsExactly(DOUBLE_POINT);
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleSumData().getPoints()).isEmpty();
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleHistogramData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleSummaryData().getPoints()).isEmpty();
  }
}
