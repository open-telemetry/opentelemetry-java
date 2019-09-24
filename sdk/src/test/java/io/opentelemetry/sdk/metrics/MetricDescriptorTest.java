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

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricDescriptor}. */
@RunWith(JUnit4.class)
public class MetricDescriptorTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME_1 = "metric1";
  private static final String METRIC_NAME_2 = "metric2";
  private static final String DESCRIPTION = "Metric description.";
  private static final String UNIT = "kb/s";
  private static final String KEY_1 = "key1";
  private static final String KEY_2 = "key2";
  private static final String VALUE_2 = "key2";

  @Test
  public void testGet() {
    MetricDescriptor metricDescriptor =
        MetricDescriptor.createInternal(
            METRIC_NAME_1,
            DESCRIPTION,
            UNIT,
            Collections.singletonList(KEY_1),
            Collections.singletonMap(KEY_2, VALUE_2));
    assertThat(metricDescriptor.getName()).isEqualTo(METRIC_NAME_1);
    assertThat(metricDescriptor.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(metricDescriptor.getUnit()).isEqualTo(UNIT);
    assertThat(metricDescriptor.getLabelKeys()).containsExactly(KEY_1);
    assertThat(metricDescriptor.getConstantLabels()).containsExactly(KEY_2, VALUE_2);
  }

  @Test
  public void create_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    MetricDescriptor.createInternal(
        null,
        DESCRIPTION,
        UNIT,
        Collections.singletonList(KEY_1),
        Collections.singletonMap(KEY_2, VALUE_2));
  }

  @Test
  public void create_NullDescription() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    MetricDescriptor.createInternal(
        METRIC_NAME_1,
        null,
        UNIT,
        Collections.singletonList(KEY_1),
        Collections.singletonMap(KEY_2, VALUE_2));
  }

  @Test
  public void create_NullUnit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    MetricDescriptor.createInternal(
        METRIC_NAME_1,
        DESCRIPTION,
        null,
        Collections.singletonList(KEY_1),
        Collections.singletonMap(KEY_2, VALUE_2));
  }

  @Test
  public void create_NullLabelKeys() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    MetricDescriptor.createInternal(
        METRIC_NAME_1, DESCRIPTION, UNIT, null, Collections.singletonMap(KEY_2, VALUE_2));
  }

  @Test
  public void create_NullConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    MetricDescriptor.createInternal(
        METRIC_NAME_1, DESCRIPTION, UNIT, Collections.singletonList(KEY_1), null);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            MetricDescriptor.createInternal(
                METRIC_NAME_1,
                DESCRIPTION,
                UNIT,
                Arrays.asList(KEY_1, KEY_2),
                Collections.<String, String>emptyMap()),
            MetricDescriptor.createInternal(
                METRIC_NAME_1,
                DESCRIPTION,
                UNIT,
                Arrays.asList(KEY_1, KEY_2),
                Collections.<String, String>emptyMap()))
        .addEqualityGroup(
            MetricDescriptor.createInternal(
                METRIC_NAME_2,
                DESCRIPTION,
                UNIT,
                Collections.singletonList(KEY_1),
                Collections.singletonMap(KEY_2, VALUE_2)),
            MetricDescriptor.createInternal(
                METRIC_NAME_2,
                DESCRIPTION,
                UNIT,
                Collections.singletonList(KEY_1),
                Collections.singletonMap(KEY_2, VALUE_2)))
        .addEqualityGroup(
            MetricDescriptor.createInternal(
                METRIC_NAME_2,
                DESCRIPTION,
                UNIT,
                Arrays.asList(KEY_1, KEY_2),
                Collections.<String, String>emptyMap()))
        .addEqualityGroup(
            MetricDescriptor.createInternal(
                METRIC_NAME_1,
                DESCRIPTION,
                UNIT,
                Collections.singletonList(KEY_1),
                Collections.singletonMap(KEY_2, VALUE_2)))
        .testEquals();
  }
}
