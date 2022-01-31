/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.View;
import org.junit.jupiter.api.Test;

class MetricDescriptorTest {

  @Test
  void metricDescriptor_preservesInstrumentDescriptor() {
    View view = View.builder().build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MetricDescriptor simple = MetricDescriptor.create(view, instrument);
    assertThat(simple.getName()).isEqualTo("name");
    assertThat(simple.getDescription()).isEqualTo("description");
    assertThat(simple.getUnit()).isEqualTo("unit");
    assertThat(simple.getSourceView()).contains(view);
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
  }

  @Test
  void metricDescriptor_overridesFromView() {
    View view = View.builder().setName("new_name").setDescription("new_description").build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.HISTOGRAM, InstrumentValueType.DOUBLE);
    MetricDescriptor simple = MetricDescriptor.create(view, instrument);
    assertThat(simple.getName()).isEqualTo("new_name");
    assertThat(simple.getDescription()).isEqualTo("new_description");
    assertThat(simple.getUnit()).isEqualTo("unit");
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
    assertThat(simple.getSourceView()).contains(view);
  }

  @Test
  void metricDescriptor_isCompatible() {
    View view = View.builder().build();
    MetricDescriptor descriptor =
        MetricDescriptor.create(
            view,
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE));
    // Same name, description, unit, instrument type, and value type is compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE))))
        .isTrue();
    // Different name is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    InstrumentDescriptor.create(
                        "foo",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE))))
        .isFalse();
    // Different description is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    InstrumentDescriptor.create(
                        "name",
                        "foo",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE))))
        .isFalse();
    // Different unit is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "foo",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE))))
        .isFalse();
    // Different instrument type is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.HISTOGRAM,
                        InstrumentValueType.DOUBLE))))
        .isFalse();
    // Different instrument value type is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.LONG))))
        .isFalse();
  }

  @Test
  void isAsync() {
    assertThat(descriptorForInstrument(InstrumentType.OBSERVABLE_UP_DOWN_SUM).isAsync()).isTrue();
    assertThat(descriptorForInstrument(InstrumentType.OBSERVABLE_GAUGE).isAsync()).isTrue();
    assertThat(descriptorForInstrument(InstrumentType.OBSERVABLE_SUM).isAsync()).isTrue();
    assertThat(descriptorForInstrument(InstrumentType.HISTOGRAM).isAsync()).isFalse();
    assertThat(descriptorForInstrument(InstrumentType.COUNTER).isAsync()).isFalse();
    assertThat(descriptorForInstrument(InstrumentType.UP_DOWN_COUNTER).isAsync()).isFalse();
  }

  private static MetricDescriptor descriptorForInstrument(InstrumentType instrumentType) {
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", instrumentType, InstrumentValueType.DOUBLE);
    return MetricDescriptor.create(View.builder().build(), instrument);
  }
}
