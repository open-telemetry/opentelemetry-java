/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class MetricAssertionsTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("instrumentation_library", null);
  private static final MetricData HISTOGRAM_METRIC =
      MetricData.createDoubleHistogram(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "histogram",
          /* description= */ "description",
          /* unit= */ "unit",
          DoubleHistogramData.create(
              AggregationTemporality.CUMULATIVE,
              // Points
              Collections.emptyList()));

  private static final MetricData HISTOGRAM_DELTA_METRIC =
      MetricData.createDoubleHistogram(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "histogram_delta",
          /* description= */ "description",
          /* unit= */ "unit",
          DoubleHistogramData.create(
              AggregationTemporality.DELTA,
              // Points
              Collections.emptyList()));

  private static final MetricData DOUBLE_SUMMARY_METRIC =
      MetricData.createDoubleSummary(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "summary",
          /* description= */ "description",
          /* unit= */ "unit",
          DoubleSummaryData.create(
              // Points
              Collections.emptyList()));

  private static final MetricData DOUBLE_GAUGE_METRIC =
      MetricData.createDoubleGauge(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "gauge",
          /* description= */ "description",
          /* unit= */ "unit",
          DoubleGaugeData.create(
              // Points
              Collections.emptyList()));

  private static final MetricData DOUBLE_SUM_METRIC =
      MetricData.createDoubleSum(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "sum",
          /* description= */ "description",
          /* unit= */ "unit",
          DoubleSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              // Points
              Collections.emptyList()));

  private static final MetricData DOUBLE_DELTA_SUM_METRIC =
      MetricData.createDoubleSum(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "sum_delta",
          /* description= */ "description",
          /* unit= */ "unit",
          DoubleSumData.create(
              false,
              AggregationTemporality.DELTA,
              // Points
              Collections.emptyList()));

  private static final DoubleExemplar DOUBLE_EXEMPLAR =
      DoubleExemplar.create(Attributes.empty(), 0, "span", "trace", 1.0);

  private static final DoublePointData DOUBLE_POINT_DATA =
      DoublePointData.create(1, 2, Attributes.empty(), 3.0, Collections.emptyList());

  private static final DoublePointData DOUBLE_POINT_DATA_WITH_EXEMPLAR =
      DoublePointData.create(
          1, 2, Attributes.empty(), 3.0, Collections.singletonList(DOUBLE_EXEMPLAR));

  private static final MetricData LONG_GAUGE_METRIC =
      MetricData.createLongGauge(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "gauge",
          /* description= */ "description",
          /* unit= */ "unit",
          LongGaugeData.create(
              // Points
              Collections.emptyList()));

  private static final MetricData LONG_SUM_METRIC =
      MetricData.createLongSum(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "sum",
          /* description= */ "description",
          /* unit= */ "unit",
          LongSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              // Points
              Collections.emptyList()));

  private static final MetricData LONG_DELTA_SUM_METRIC =
      MetricData.createLongSum(
          RESOURCE,
          INSTRUMENTATION_LIBRARY_INFO,
          /* name= */ "sum_delta",
          /* description= */ "description",
          /* unit= */ "unit",
          LongSumData.create(
              false,
              AggregationTemporality.DELTA,
              // Points
              Collections.emptyList()));

  private static final LongExemplar LONG_EXEMPLAR =
      LongExemplar.create(Attributes.empty(), 0, "span", "trace", 1);

  private static final LongPointData LONG_POINT_DATA =
      LongPointData.create(1, 2, Attributes.empty(), 3, Collections.emptyList());

  private static final LongPointData LONG_POINT_DATA_WITH_EXEMPLAR =
      LongPointData.create(1, 2, Attributes.empty(), 3, Collections.singletonList(LONG_EXEMPLAR));

  private static final ValueAtPercentile PERCENTILE_VALUE = ValueAtPercentile.create(0, 1);

  private static final DoubleSummaryPointData DOUBLE_SUMMARY_POINT_DATA =
      DoubleSummaryPointData.create(
          1, 2, Attributes.empty(), 1, 2, Collections.singletonList(PERCENTILE_VALUE));

  @Test
  void metric_passing() {
    assertThat(HISTOGRAM_METRIC)
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
        .hasName("histogram")
        .hasDescription("description")
        .hasUnit("unit");
  }

  @Test
  void metric_fails() {
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasResource(
                        Resource.create(Attributes.of(stringKey("monkey_key"), "resource_value"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(
                            "instrumentation_library_for_monkeys", null)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(HISTOGRAM_METRIC).hasName("Monkeys"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(HISTOGRAM_METRIC).hasDescription("Monkeys"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(HISTOGRAM_METRIC).hasUnit("Monkeys"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void histogram_passing() {
    assertThat(HISTOGRAM_METRIC).hasDoubleHistogram().isCumulative();
    assertThat(HISTOGRAM_DELTA_METRIC).hasDoubleHistogram().isDelta();
  }

  @Test
  void histogram_fails() {
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasDoubleHistogram())
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(HISTOGRAM_METRIC).hasDoubleHistogram().isDelta())
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(HISTOGRAM_DELTA_METRIC).hasDoubleHistogram().isCumulative())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void summary_passing() {
    assertThat(DOUBLE_SUMMARY_METRIC).hasDoubleSummary();
  }

  @Test
  void sumamry_failing() {
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasDoubleSummary())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void doubleGauge_passing() {
    assertThat(DOUBLE_GAUGE_METRIC).hasDoubleGauge();
  }

  @Test
  void doubleGauge_fails() {
    assertThatThrownBy(() -> assertThat(HISTOGRAM_DELTA_METRIC).hasDoubleGauge())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void doubleSum_passing() {
    assertThat(DOUBLE_SUM_METRIC).hasDoubleSum().isCumulative().isMonotonic();
    assertThat(DOUBLE_DELTA_SUM_METRIC).hasDoubleSum().isDelta().isNotMonotonic();
  }

  @Test
  void doubleSum_fails() {
    assertThatThrownBy(() -> assertThat(HISTOGRAM_DELTA_METRIC).hasDoubleSum())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(DOUBLE_SUM_METRIC).hasDoubleSum().isDelta())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(DOUBLE_SUM_METRIC).hasDoubleSum().isNotMonotonic())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(DOUBLE_DELTA_SUM_METRIC).hasDoubleSum().isCumulative())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(DOUBLE_DELTA_SUM_METRIC).hasDoubleSum().isMonotonic())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void doublePoint_passing() {
    assertThat(DOUBLE_POINT_DATA)
        .hasStartEpochNanos(1)
        .hasEpochNanos(2)
        .hasValue(3)
        .hasAttributes(Attributes.empty())
        .exemplars()
        .isEmpty();

    assertThat(DOUBLE_POINT_DATA_WITH_EXEMPLAR).hasExemplars(DOUBLE_EXEMPLAR);
  }

  @Test
  void doublePoint_failing() {
    assertThatThrownBy(() -> assertThat(DOUBLE_POINT_DATA).hasStartEpochNanos(2))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(DOUBLE_POINT_DATA).hasEpochNanos(3))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(DOUBLE_POINT_DATA).hasValue(4))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_POINT_DATA)
                    .hasAttributes(Attributes.builder().put("x", "y").build()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_POINT_DATA)
                    .hasExemplars(
                        DoubleExemplar.create(Attributes.empty(), 0, "span", "trace", 1.0)))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void longPoint_passing() {
    assertThat(LONG_POINT_DATA)
        .hasStartEpochNanos(1)
        .hasEpochNanos(2)
        .hasValue(3)
        .hasAttributes(Attributes.empty())
        .exemplars()
        .isEmpty();

    assertThat(LONG_POINT_DATA_WITH_EXEMPLAR).hasExemplars(LONG_EXEMPLAR);
  }

  @Test
  void longPoint_failing() {
    assertThatThrownBy(() -> assertThat(LONG_POINT_DATA).hasStartEpochNanos(2))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(LONG_POINT_DATA).hasEpochNanos(3))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(LONG_POINT_DATA).hasValue(4))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_POINT_DATA)
                    .hasAttributes(Attributes.builder().put("x", "y").build()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_POINT_DATA)
                    .hasExemplars(LongExemplar.create(Attributes.empty(), 0, "span", "trace", 1)))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void longSum_passing() {
    assertThat(LONG_SUM_METRIC).hasLongSum().isCumulative().isMonotonic();
    assertThat(LONG_DELTA_SUM_METRIC).hasLongSum().isDelta().isNotMonotonic();
  }

  @Test
  void longSum_fails() {
    assertThatThrownBy(() -> assertThat(HISTOGRAM_DELTA_METRIC).hasLongSum())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(LONG_SUM_METRIC).hasLongSum().isDelta())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(LONG_SUM_METRIC).hasLongSum().isNotMonotonic())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(LONG_DELTA_SUM_METRIC).hasLongSum().isCumulative())
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(LONG_DELTA_SUM_METRIC).hasLongSum().isMonotonic())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void longGauge_passing() {
    assertThat(LONG_GAUGE_METRIC).hasLongGauge();
  }

  @Test
  void longGauge_fails() {
    assertThatThrownBy(() -> assertThat(HISTOGRAM_DELTA_METRIC).hasLongGauge())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void doubleSummaryPointData_passing() {
    assertThat(DOUBLE_SUMMARY_POINT_DATA)
        .hasCount(1)
        .hasSum(2)
        .hasEpochNanos(2)
        .hasStartEpochNanos(1)
        .hasAttributes(Attributes.empty())
        .hasPercentileValues(PERCENTILE_VALUE);
  }

  @Test
  void doubleSummaryPointData_failing() {
    assertThatThrownBy(() -> assertThat(DOUBLE_SUMMARY_POINT_DATA).hasCount(2))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(DOUBLE_SUMMARY_POINT_DATA).hasSum(1))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_SUMMARY_POINT_DATA)
                    .hasPercentileValues(ValueAtPercentile.create(1, 1)))
        .isInstanceOf(AssertionError.class);
  }
}
