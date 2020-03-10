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

import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opentelemetry.sdk.metrics.data.MetricData.Descriptor}. */
@RunWith(JUnit4.class)
public class MetricDataDescriptorTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "metric";
  private static final String DESCRIPTION = "Instrument description.";
  private static final String UNIT = "kb/s";
  private static final Descriptor.Type TYPE = Descriptor.Type.MONOTONIC_LONG;
  private static final String KEY = "key1";
  private static final String VALUE = "value_1";

  @Test
  public void testGet() {
    Descriptor descriptor =
        Descriptor.create(
            METRIC_NAME, DESCRIPTION, UNIT, TYPE, Collections.singletonMap(KEY, VALUE));
    assertThat(descriptor.getName()).isEqualTo(METRIC_NAME);
    assertThat(descriptor.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(descriptor.getUnit()).isEqualTo(UNIT);
    assertThat(descriptor.getType()).isEqualTo(TYPE);
    assertThat(descriptor.getConstantLabels()).containsExactly(KEY, VALUE);
  }

  @Test
  public void create_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    Descriptor.create(null, DESCRIPTION, UNIT, TYPE, Collections.singletonMap(KEY, VALUE));
  }

  @Test
  public void create_NullDescription() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    Descriptor.create(METRIC_NAME, null, UNIT, TYPE, Collections.singletonMap(KEY, VALUE));
  }

  @Test
  public void create_NullUnit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    Descriptor.create(METRIC_NAME, DESCRIPTION, null, TYPE, Collections.singletonMap(KEY, VALUE));
  }

  @Test
  public void create_NullType() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("type");
    Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, null, Collections.singletonMap(KEY, VALUE));
  }

  @Test
  public void create_NullConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, TYPE, null);
  }
}
