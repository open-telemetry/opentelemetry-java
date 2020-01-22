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

package io.opentelemetry.sdk.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.metrics.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.MetricData.Int64Point;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricData}. */
@RunWith(JUnit4.class)
public class MetricDataTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Descriptor INT64_METRIC_DESCRIPTOR =
      Descriptor.createInternal(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.MONOTONIC_INT64,
          Collections.singletonList("key"),
          Collections.singletonMap("key_const", "value_const"));
  private static final Descriptor DOUBLE_METRIC_DESCRIPTOR =
      Descriptor.createInternal(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.NON_MONOTONIC_DOUBLE,
          Collections.singletonList("key"),
          Collections.singletonMap("key_const", "value_const"));
  private static final long START_EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(1000);
  private static final long EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(2000);
  private static final long LONG_VALUE = 10;
  private static final double DOUBLE_VALUE = 1.234;
  private static final Int64Point LONG_POINT =
      Int64Point.createInternal(START_EPOCH_NANOS, EPOCH_NANOS, LONG_VALUE);
  private static final DoublePoint DOUBLE_POINT =
      DoublePoint.createInternal(START_EPOCH_NANOS, EPOCH_NANOS, DOUBLE_VALUE);

  @Test
  public void metricData_Int64Points() {
    MetricData metricData =
        MetricData.createWithInt64Points(
            INT64_METRIC_DESCRIPTOR, Collections.singletonList(LONG_POINT));
    assertThat(metricData.getDescriptor()).isEqualTo(INT64_METRIC_DESCRIPTOR);
    assertThat(metricData.getInt64Points()).containsExactly(LONG_POINT);
    assertThat(metricData.getDoublePoints()).isNull();
  }

  @Test
  public void metricData_Int64Points_NullDescriptor() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("descriptor");
    MetricData.createWithInt64Points(null, Collections.singletonList(LONG_POINT));
  }

  @Test
  public void metricData_Int64Points_NullPoints() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("longPoints");
    MetricData.createWithInt64Points(INT64_METRIC_DESCRIPTOR, null);
  }

  @Test
  public void metricData_Int64Points_IncompatibleTypes() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Incompatible points type with metric type.");
    MetricData.createWithInt64Points(
        DOUBLE_METRIC_DESCRIPTOR, Collections.singletonList(LONG_POINT));
  }

  @Test
  public void metricData_DoublePoints() {
    MetricData metricData =
        MetricData.createWithDoublePoints(
            DOUBLE_METRIC_DESCRIPTOR, Collections.singletonList(DOUBLE_POINT));
    assertThat(metricData.getDescriptor()).isEqualTo(DOUBLE_METRIC_DESCRIPTOR);
    assertThat(metricData.getInt64Points()).isNull();
    assertThat(metricData.getDoublePoints()).containsExactly(DOUBLE_POINT);
  }

  @Test
  public void metricData_DoublePoints_NullDescriptor() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("descriptor");
    MetricData.createWithDoublePoints(null, Collections.singletonList(DOUBLE_POINT));
  }

  @Test
  public void metricData_DoublePoints_NullPoints() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("doublePoints");
    MetricData.createWithDoublePoints(DOUBLE_METRIC_DESCRIPTOR, null);
  }

  @Test
  public void metricData_DoublePoints_IncompatibleTypes() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Incompatible points type with metric type.");
    MetricData.createWithDoublePoints(
        INT64_METRIC_DESCRIPTOR, Collections.singletonList(DOUBLE_POINT));
  }
}
