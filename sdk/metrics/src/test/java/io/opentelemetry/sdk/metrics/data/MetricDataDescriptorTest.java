/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.opentelemetry.sdk.metrics.data.MetricData.Descriptor}. */
class MetricDataDescriptorTest {

  private static final String METRIC_NAME = "metric";
  private static final String DESCRIPTION = "Instrument description.";
  private static final String UNIT = "kb/s";
  private static final Descriptor.Type TYPE = Descriptor.Type.MONOTONIC_LONG;
  private static final String KEY = "key1";
  private static final String VALUE = "value_1";

  @Test
  void testGet() {
    Descriptor descriptor =
        Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, TYPE, Labels.of(KEY, VALUE));
    assertThat(descriptor.getName()).isEqualTo(METRIC_NAME);
    assertThat(descriptor.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(descriptor.getUnit()).isEqualTo(UNIT);
    assertThat(descriptor.getType()).isEqualTo(TYPE);
    assertThat(descriptor.getConstantLabels().size()).isEqualTo(1);
    assertThat(descriptor.getConstantLabels().get(KEY)).isEqualTo(VALUE);
  }

  @Test
  void create_NullName() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(null, DESCRIPTION, UNIT, TYPE, Labels.of(KEY, VALUE)),
        "name");
  }

  @Test
  void create_NullDescription() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, null, UNIT, TYPE, Labels.of(KEY, VALUE)),
        "description");
  }

  @Test
  void create_NullUnit() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, DESCRIPTION, null, TYPE, Labels.of(KEY, VALUE)),
        "unit");
  }

  @Test
  void create_NullType() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, null, Labels.of(KEY, VALUE)),
        "type");
  }

  @Test
  void create_NullConstantLabels() {
    assertThrows(
        NullPointerException.class,
        () -> Descriptor.create(METRIC_NAME, DESCRIPTION, UNIT, TYPE, null),
        "constantLabels");
  }
}
