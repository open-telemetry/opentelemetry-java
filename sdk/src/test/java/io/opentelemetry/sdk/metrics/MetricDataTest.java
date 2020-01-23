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
import io.opentelemetry.sdk.metrics.MetricData.LongPoint;
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

  private static final Descriptor LONG_METRIC_DESCRIPTOR =
      Descriptor.createInternal(
          "metric_name",
          "metric_description",
          "ms",
          Descriptor.Type.MONOTONIC_LONG,
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
  private static final LongPoint LONG_POINT =
      MetricData.LongPoint.createInternal(START_EPOCH_NANOS, EPOCH_NANOS, LONG_VALUE);
  private static final DoublePoint DOUBLE_POINT =
      DoublePoint.createInternal(START_EPOCH_NANOS, EPOCH_NANOS, DOUBLE_VALUE);

  @Test
  public void metricData_LongPoints() {
    MetricData metricData =
        MetricData.createWithLongPoints(
            LONG_METRIC_DESCRIPTOR, Collections.singletonList(LONG_POINT));
    assertThat(metricData.getDescriptor()).isEqualTo(LONG_METRIC_DESCRIPTOR);
    assertThat(metricData.getLongPoints()).containsExactly(LONG_POINT);
    assertThat(metricData.getDoublePoints()).isNull();
  }

  @Test
  public void metricData_LongPoints_NullDescriptor() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("descriptor");
    MetricData.createWithLongPoints(null, Collections.singletonList(LONG_POINT));
  }

  @Test
  public void metricData_LongPoints_NullPoints() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("longPoints");
    MetricData.createWithLongPoints(LONG_METRIC_DESCRIPTOR, null);
  }

  @Test
  public void metricData_LongPoints_IncompatibleTypes() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Incompatible points type with metric type.");
    MetricData.createWithLongPoints(
        DOUBLE_METRIC_DESCRIPTOR, Collections.singletonList(LONG_POINT));
  }

  @Test
  public void metricData_DoublePoints() {
    MetricData metricData =
        MetricData.createWithDoublePoints(
            DOUBLE_METRIC_DESCRIPTOR, Collections.singletonList(DOUBLE_POINT));
    assertThat(metricData.getDescriptor()).isEqualTo(DOUBLE_METRIC_DESCRIPTOR);
    assertThat(metricData.getLongPoints()).isNull();
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
        LONG_METRIC_DESCRIPTOR, Collections.singletonList(DOUBLE_POINT));
  }
}
