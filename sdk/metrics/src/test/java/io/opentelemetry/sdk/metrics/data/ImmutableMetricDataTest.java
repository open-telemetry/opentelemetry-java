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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.opentelemetry.sdk.metrics.data.MetricData}. */
class ImmutableMetricDataTest {
  private static final long START_EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(1000);
  private static final long EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(2000);
  private static final long LONG_VALUE = 10;
  private static final double DOUBLE_VALUE = 1.234;
  private static final double DOUBLE_VALUE_MIN = 1.02;
  private static final double DOUBLE_VALUE_MAX = 0.214;
  private static final AttributeKey<String> KEY = AttributeKey.stringKey("key");
  private static final ValueAtQuantile MINIMUM_VALUE =
      ImmutableValueAtQuantile.create(0.0, DOUBLE_VALUE);
  private static final ValueAtQuantile MAXIMUM_VALUE =
      ImmutableValueAtQuantile.create(1.0, DOUBLE_VALUE);
  private static final LongPointData LONG_POINT =
      ImmutableLongPointData.create(
          START_EPOCH_NANOS, EPOCH_NANOS, Attributes.of(KEY, "value"), LONG_VALUE);
  private static final DoublePointData DOUBLE_POINT =
      ImmutableDoublePointData.create(
          START_EPOCH_NANOS, EPOCH_NANOS, Attributes.of(KEY, "value"), DOUBLE_VALUE);
  private static final SummaryPointData SUMMARY_POINT =
      ImmutableSummaryPointData.create(
          START_EPOCH_NANOS,
          EPOCH_NANOS,
          Attributes.of(KEY, "value"),
          LONG_VALUE,
          DOUBLE_VALUE,
          Arrays.asList(
              ImmutableValueAtQuantile.create(0.0, DOUBLE_VALUE),
              ImmutableValueAtQuantile.create(1.0, DOUBLE_VALUE)));
  private static final ImmutableHistogramPointData HISTOGRAM_POINT =
      ImmutableHistogramPointData.create(
          START_EPOCH_NANOS,
          EPOCH_NANOS,
          Attributes.of(KEY, "value"),
          DOUBLE_VALUE,
          /* hasMin= */ true,
          DOUBLE_VALUE_MIN,
          /* hasMax= */ true,
          DOUBLE_VALUE_MAX,
          ImmutableList.of(1.0),
          ImmutableList.of(1L, 1L));

  @Test
  void metricData_Getters() {
    MetricData metricData =
        ImmutableMetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableGaugeData.create(Collections.emptyList()));
    assertThat(metricData.getName()).isEqualTo("metric_name");
    assertThat(metricData.getDescription()).isEqualTo("metric_description");
    assertThat(metricData.getUnit()).isEqualTo("ms");
    assertThat(metricData.getType()).isEqualTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(metricData.getResource()).isEqualTo(Resource.empty());
    assertThat(metricData.getInstrumentationScopeInfo())
        .isEqualTo(InstrumentationScopeInfo.empty());
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
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableGaugeData.create(Collections.singletonList(LONG_POINT)));
    assertThat(metricData.isEmpty()).isFalse();
    assertThat(metricData.getLongGaugeData().getPoints()).containsExactly(LONG_POINT);
    metricData =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableSumData.create(
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
        ImmutableMetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableGaugeData.create(Collections.singletonList(DOUBLE_POINT)));
    assertThat(metricData.isEmpty()).isFalse();
    assertThat(metricData.getDoubleGaugeData().getPoints()).containsExactly(DOUBLE_POINT);
    metricData =
        ImmutableMetricData.createDoubleSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableSumData.create(
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
    assertThat(SUMMARY_POINT.getValues()).isEqualTo(Arrays.asList(MINIMUM_VALUE, MAXIMUM_VALUE));
    MetricData metricData =
        ImmutableMetricData.createDoubleSummary(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableSummaryData.create(Collections.singletonList(SUMMARY_POINT)));
    assertThat(metricData.getSummaryData().getPoints()).containsExactly(SUMMARY_POINT);
  }

  @Test
  void metricData_HistogramPoints() {
    assertThat(HISTOGRAM_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(HISTOGRAM_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(HISTOGRAM_POINT.getAttributes().size()).isEqualTo(1);
    assertThat(HISTOGRAM_POINT.getAttributes().get(KEY)).isEqualTo("value");
    assertThat(HISTOGRAM_POINT.getCount()).isEqualTo(2L);
    assertThat(HISTOGRAM_POINT.getSum()).isEqualTo(DOUBLE_VALUE);
    assertThat(HISTOGRAM_POINT.getMin()).isEqualTo(DOUBLE_VALUE_MIN);
    assertThat(HISTOGRAM_POINT.getMax()).isEqualTo(DOUBLE_VALUE_MAX);
    assertThat(HISTOGRAM_POINT.getBoundaries()).isEqualTo(ImmutableList.of(1.0));
    assertThat(HISTOGRAM_POINT.getCounts()).isEqualTo(ImmutableList.of(1L, 1L));

    MetricData metricData =
        ImmutableMetricData.createDoubleHistogram(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableHistogramData.create(
                AggregationTemporality.DELTA, Collections.singleton(HISTOGRAM_POINT)));
    assertThat(metricData.getHistogramData().getPoints()).containsExactly(HISTOGRAM_POINT);

    assertThatThrownBy(
            () ->
                ImmutableHistogramPointData.create(
                    0,
                    0,
                    Attributes.empty(),
                    0.0,
                    /* hasMin= */ false,
                    0.0,
                    /* hasMax= */ false,
                    0.0,
                    ImmutableList.of(),
                    ImmutableList.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ImmutableHistogramPointData.create(
                    0,
                    0,
                    Attributes.empty(),
                    0.0,
                    /* hasMin= */ false,
                    0.0,
                    /* hasMax= */ false,
                    0.0,
                    ImmutableList.of(1.0, 1.0),
                    ImmutableList.of(0L, 0L, 0L)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ImmutableHistogramPointData.create(
                    0,
                    0,
                    Attributes.empty(),
                    0.0,
                    /* hasMin= */ false,
                    0.0,
                    /* hasMax= */ false,
                    0.0,
                    ImmutableList.of(Double.NEGATIVE_INFINITY),
                    ImmutableList.of(0L, 0L)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void metricData_GetDefault() {
    MetricData metricData =
        ImmutableMetricData.createDoubleSummary(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableSummaryData.create(Collections.singletonList(SUMMARY_POINT)));
    assertThat(metricData.getDoubleGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleSumData().getPoints()).isEmpty();
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getSummaryData().getPoints()).containsExactly(SUMMARY_POINT);
    assertThat(metricData.getHistogramData().getPoints()).isEmpty();

    metricData =
        ImmutableMetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "metric_name",
            "metric_description",
            "ms",
            ImmutableGaugeData.create(Collections.singletonList(DOUBLE_POINT)));
    assertThat(metricData.getDoubleGaugeData().getPoints()).containsExactly(DOUBLE_POINT);
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getDoubleSumData().getPoints()).isEmpty();
    assertThat(metricData.getLongGaugeData().getPoints()).isEmpty();
    assertThat(metricData.getSummaryData().getPoints()).isEmpty();
    assertThat(metricData.getHistogramData().getPoints()).isEmpty();
  }
}
