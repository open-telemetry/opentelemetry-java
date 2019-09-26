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

import io.opentelemetry.trace.Timestamp;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricData}. */
@RunWith(JUnit4.class)
public class MetricDataTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.createInternal(
          "metric_name",
          "metric_description",
          "ms",
          MetricDescriptor.Type.MONOTONIC_INT64,
          Collections.singletonList("key"),
          Collections.singletonMap("key_const", "value_const"));
  private static final Timestamp START_TIMESTAMP = Timestamp.fromMillis(1000);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(2000);

  @Test
  public void testGet() {
    MetricData metricData =
        MetricData.createInternal(METRIC_DESCRIPTOR, START_TIMESTAMP, TIMESTAMP);
    assertThat(metricData.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metricData.getStartTimestamp()).isEqualTo(START_TIMESTAMP);
    assertThat(metricData.getTimestamp()).isEqualTo(TIMESTAMP);
  }

  @Test
  public void create_NullDescriptor() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("metricDescriptor");
    MetricData.createInternal(null, START_TIMESTAMP, TIMESTAMP);
  }

  @Test
  public void create_NullStartTimestamp() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("startTimestamp");
    MetricData.createInternal(METRIC_DESCRIPTOR, null, TIMESTAMP);
  }

  @Test
  public void create_NullTimestamp() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("timestamp");
    MetricData.createInternal(METRIC_DESCRIPTOR, START_TIMESTAMP, null);
  }
}
