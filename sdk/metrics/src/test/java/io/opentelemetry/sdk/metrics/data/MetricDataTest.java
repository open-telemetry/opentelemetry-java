/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
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
  private static final ValueAtPercentile MINIMUM_VALUE =
      ValueAtPercentile.create(0.0, DOUBLE_VALUE);
  private static final ValueAtPercentile MAXIMUM_VALUE =
      ValueAtPercentile.create(100.0, DOUBLE_VALUE);
  private static final LongPoint LONG_POINT =
      MetricData.LongPoint.create(
          START_EPOCH_NANOS, EPOCH_NANOS, Labels.of("key", "value"), LONG_VALUE);
  private static final DoublePoint DOUBLE_POINT =
      DoublePoint.create(START_EPOCH_NANOS, EPOCH_NANOS, Labels.of("key", "value"), DOUBLE_VALUE);
  private static final SummaryPoint SUMMARY_POINT =
      SummaryPoint.create(
          START_EPOCH_NANOS,
          EPOCH_NANOS,
          Labels.of("key", "value"),
          LONG_VALUE,
          DOUBLE_VALUE,
          Arrays.asList(
              ValueAtPercentile.create(0.0, DOUBLE_VALUE),
              ValueAtPercentile.create(100, DOUBLE_VALUE)));

  @Test
  void metricData_NullInstrumentationLibraryInfo() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                Resource.getEmpty(),
                null,
                "metric_name",
                "metric_description",
                "ms",
                MetricData.Type.MONOTONIC_LONG,
                singletonList(DOUBLE_POINT)),
        "instrumentationLibraryInfo");
  }

  @Test
  void metricData_NullName() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                null,
                "metric_description",
                "ms",
                MetricData.Type.MONOTONIC_LONG,
                singletonList(DOUBLE_POINT)),
        "name");
  }

  @Test
  void metricData_NullDescription() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                "metric_name",
                null,
                "ms",
                MetricData.Type.MONOTONIC_LONG,
                singletonList(DOUBLE_POINT)),
        "description");
  }

  @Test
  void metricData_NullUnit() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                "metric_name",
                "metric_description",
                null,
                MetricData.Type.MONOTONIC_LONG,
                singletonList(DOUBLE_POINT)),
        "unit");
  }

  @Test
  void metricData_NullType() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                "metric_name",
                "metric_description",
                "ms",
                null,
                singletonList(DOUBLE_POINT)),
        "type");
  }

  @Test
  void metricData_NullPoints() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                "metric_name",
                "metric_description",
                "ms",
                MetricData.Type.MONOTONIC_LONG,
                null),
        "points");
  }

  @Test
  void metricData_Getters() {
    MetricData metricData =
        MetricData.create(
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            "metric_name",
            "metric_description",
            "ms",
            MetricData.Type.MONOTONIC_LONG,
            Collections.emptyList());
    assertThat(metricData.getName()).isEqualTo("metric_name");
    assertThat(metricData.getDescription()).isEqualTo("metric_description");
    assertThat(metricData.getUnit()).isEqualTo("ms");
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.MONOTONIC_LONG);
    assertThat(metricData.getResource()).isEqualTo(Resource.getEmpty());
    assertThat(metricData.getInstrumentationLibraryInfo())
        .isEqualTo(InstrumentationLibraryInfo.getEmpty());
    assertThat(metricData.getPoints()).isEmpty();
  }

  @Test
  void metricData_LongPoints() {
    assertThat(LONG_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(LONG_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(LONG_POINT.getLabels().size()).isEqualTo(1);
    assertThat(LONG_POINT.getLabels().get("key")).isEqualTo("value");
    assertThat(LONG_POINT.getValue()).isEqualTo(LONG_VALUE);
    MetricData metricData =
        MetricData.create(
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            "metric_name",
            "metric_description",
            "ms",
            MetricData.Type.MONOTONIC_LONG,
            Collections.singletonList(LONG_POINT));
    assertThat(metricData.getPoints()).containsExactly(LONG_POINT);
  }

  @Test
  void metricData_SummaryPoints() {
    assertThat(SUMMARY_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(SUMMARY_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(SUMMARY_POINT.getLabels().size()).isEqualTo(1);
    assertThat(SUMMARY_POINT.getLabels().get("key")).isEqualTo("value");
    assertThat(SUMMARY_POINT.getCount()).isEqualTo(LONG_VALUE);
    assertThat(SUMMARY_POINT.getSum()).isEqualTo(DOUBLE_VALUE);
    assertThat(SUMMARY_POINT.getPercentileValues())
        .isEqualTo(Arrays.asList(MINIMUM_VALUE, MAXIMUM_VALUE));
    MetricData metricData =
        MetricData.create(
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            "metric_name",
            "metric_description",
            "ms",
            MetricData.Type.NON_MONOTONIC_DOUBLE,
            Collections.singletonList(SUMMARY_POINT));
    assertThat(metricData.getPoints()).containsExactly(SUMMARY_POINT);
  }

  @Test
  void metricData_DoublePoints() {
    assertThat(DOUBLE_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(DOUBLE_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(DOUBLE_POINT.getLabels().size()).isEqualTo(1);
    assertThat(DOUBLE_POINT.getLabels().get("key")).isEqualTo("value");
    assertThat(DOUBLE_POINT.getValue()).isEqualTo(DOUBLE_VALUE);
    MetricData metricData =
        MetricData.create(
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            "metric_name",
            "metric_description",
            "ms",
            MetricData.Type.NON_MONOTONIC_DOUBLE,
            Collections.singletonList(DOUBLE_POINT));
    assertThat(metricData.getPoints()).containsExactly(DOUBLE_POINT);
  }
}
