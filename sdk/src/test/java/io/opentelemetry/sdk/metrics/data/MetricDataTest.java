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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opentelemetry.sdk.metrics.data.MetricData}. */
@RunWith(JUnit4.class)
public class MetricDataTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Descriptor LONG_METRIC_DESCRIPTOR =
      Descriptor.create(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.MONOTONIC_LONG,
          Collections.singletonMap("key_const", "value_const"));
  private static final Descriptor DOUBLE_METRIC_DESCRIPTOR =
      Descriptor.create(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.NON_MONOTONIC_DOUBLE,
          Collections.singletonMap("key_const", "value_const"));
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
          START_EPOCH_NANOS, EPOCH_NANOS, Collections.singletonMap("key", "value"), LONG_VALUE);
  private static final DoublePoint DOUBLE_POINT =
      DoublePoint.create(
          START_EPOCH_NANOS, EPOCH_NANOS, Collections.singletonMap("key", "value"), DOUBLE_VALUE);
  private static final SummaryPoint SUMMARY_POINT =
      SummaryPoint.create(
          START_EPOCH_NANOS,
          EPOCH_NANOS,
          Collections.singletonMap("key", "value"),
          LONG_VALUE,
          DOUBLE_VALUE,
          Arrays.asList(
              ValueAtPercentile.create(0.0, DOUBLE_VALUE),
              ValueAtPercentile.create(100, DOUBLE_VALUE)));

  @Test
  public void metricData_NullDescriptor() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("descriptor");
    MetricData.create(
        null,
        Resource.getEmpty(),
        InstrumentationLibraryInfo.getEmpty(),
        Collections.<Point>singletonList(DOUBLE_POINT));
  }

  @Test
  public void metricData_NullResource() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("resource");
    MetricData.create(
        LONG_METRIC_DESCRIPTOR,
        null,
        InstrumentationLibraryInfo.getEmpty(),
        Collections.<Point>singletonList(DOUBLE_POINT));
  }

  @Test
  public void metricData_NullInstrumentationLibraryInfo() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("instrumentationLibraryInfo");
    MetricData.create(
        LONG_METRIC_DESCRIPTOR,
        Resource.getEmpty(),
        null,
        Collections.<Point>singletonList(DOUBLE_POINT));
  }

  @Test
  public void metricData_NullPoints() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("points");
    MetricData.create(
        LONG_METRIC_DESCRIPTOR, Resource.getEmpty(), InstrumentationLibraryInfo.getEmpty(), null);
  }

  @Test
  public void metricData_Getters() {
    MetricData metricData =
        MetricData.create(
            LONG_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            Collections.<Point>emptyList());
    assertThat(metricData.getDescriptor()).isEqualTo(LONG_METRIC_DESCRIPTOR);
    assertThat(metricData.getResource()).isEqualTo(Resource.getEmpty());
    assertThat(metricData.getInstrumentationLibraryInfo())
        .isEqualTo(InstrumentationLibraryInfo.getEmpty());
    assertThat(metricData.getPoints()).isEmpty();
  }

  @Test
  public void metricData_LongPoints() {
    assertThat(LONG_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(LONG_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(LONG_POINT.getLabels()).containsExactly("key", "value");
    assertThat(LONG_POINT.getValue()).isEqualTo(LONG_VALUE);
    MetricData metricData =
        MetricData.create(
            LONG_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            Collections.<Point>singletonList(LONG_POINT));
    assertThat(metricData.getPoints()).containsExactly(LONG_POINT);
  }

  @Test
  public void metricData_SummaryPoints() {
    assertThat(SUMMARY_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(SUMMARY_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(SUMMARY_POINT.getLabels()).containsExactly("key", "value");
    assertThat(SUMMARY_POINT.getCount()).isEqualTo(LONG_VALUE);
    assertThat(SUMMARY_POINT.getSum()).isEqualTo(DOUBLE_VALUE);
    assertThat(SUMMARY_POINT.getPercentileValues())
        .isEqualTo(Arrays.asList(MINIMUM_VALUE, MAXIMUM_VALUE));
    MetricData metricData =
        MetricData.create(
            DOUBLE_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            Collections.<Point>singletonList(SUMMARY_POINT));
    assertThat(metricData.getPoints()).containsExactly(SUMMARY_POINT);
  }

  @Test
  public void metricData_DoublePoints() {
    assertThat(DOUBLE_POINT.getStartEpochNanos()).isEqualTo(START_EPOCH_NANOS);
    assertThat(DOUBLE_POINT.getEpochNanos()).isEqualTo(EPOCH_NANOS);
    assertThat(DOUBLE_POINT.getLabels()).containsExactly("key", "value");
    assertThat(DOUBLE_POINT.getValue()).isEqualTo(DOUBLE_VALUE);
    MetricData metricData =
        MetricData.create(
            DOUBLE_METRIC_DESCRIPTOR,
            Resource.getEmpty(),
            InstrumentationLibraryInfo.getEmpty(),
            Collections.<Point>singletonList(DOUBLE_POINT));
    assertThat(metricData.getPoints()).containsExactly(DOUBLE_POINT);
  }
}
