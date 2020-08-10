/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.data;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
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

  private static final Descriptor LONG_METRIC_DESCRIPTOR =
      Descriptor.create(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.MONOTONIC_LONG,
          Labels.of("key_const", "value_const"));
  private static final Descriptor DOUBLE_METRIC_DESCRIPTOR =
      Descriptor.create(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.NON_MONOTONIC_DOUBLE,
          Labels.of("key_const", "value_const"));
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
  void metricData_NullDescriptor() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                null,
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                singletonList(DOUBLE_POINT)),
        "descriptor");
  }

  @Test
  void metricData_NullResource() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                LONG_METRIC_DESCRIPTOR,
                null,
                InstrumentationLibraryInfo.getEmpty(),
                singletonList(DOUBLE_POINT)),
        "resource");
  }

  @Test
  void metricData_NullInstrumentationLibraryInfo() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                LONG_METRIC_DESCRIPTOR, Resource.getEmpty(), null, singletonList(DOUBLE_POINT)),
        "instrumentationLibraryInfo");
  }

  @Test
  void metricData_NullPoints() {
    assertThrows(
        NullPointerException.class,
        () ->
            MetricData.create(
                LONG_METRIC_DESCRIPTOR,
                Resource.getEmpty(),
                InstrumentationLibraryInfo.getEmpty(),
                null),
        "points");
  }

  @Test
  void metricData_Getters() {
    MetricData metricData =
        MetricData.create(
            LONG_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            Collections.emptyList());
    assertThat(metricData.getDescriptor()).isEqualTo(LONG_METRIC_DESCRIPTOR);
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
            LONG_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
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
            DOUBLE_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
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
            DOUBLE_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            Collections.singletonList(DOUBLE_POINT));
    assertThat(metricData.getPoints()).containsExactly(DOUBLE_POINT);
  }
}
