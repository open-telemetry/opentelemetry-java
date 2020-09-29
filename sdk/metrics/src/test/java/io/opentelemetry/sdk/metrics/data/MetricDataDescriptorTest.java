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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.opentelemetry.sdk.metrics.data.MetricData.Descriptor}. */
class MetricDataDescriptorTest {

  private static final String METRIC_NAME = "metric";
  private static final String DESCRIPTION = "Instrument description.";
  private static final String UNIT = "kb/s";
  private static final Descriptor.Type TYPE = Descriptor.Type.MONOTONIC_LONG;

  @Test
  void testGet() {
    Descriptor descriptor = Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, TYPE);
    assertThat(descriptor.getName()).isEqualTo(METRIC_NAME);
    assertThat(descriptor.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(descriptor.getUnit()).isEqualTo(UNIT);
    assertThat(descriptor.getType()).isEqualTo(TYPE);
  }

  @Test
  void create_NullName() {
    assertThrows(
        NullPointerException.class, () -> Descriptor.create(null, DESCRIPTION, UNIT, TYPE), "name");
  }

  @Test
  void create_NullDescription() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, null, UNIT, TYPE),
        "description");
  }

  @Test
  void create_NullUnit() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, DESCRIPTION, null, TYPE),
        "unit");
  }

  @Test
  void create_NullType() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, null),
        "type");
  }
}
