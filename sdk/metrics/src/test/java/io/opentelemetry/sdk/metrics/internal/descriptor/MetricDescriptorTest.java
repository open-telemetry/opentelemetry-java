/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregation;
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
    assertThat(simple.getSourceView()).isEqualTo(view);
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
    assertThat(simple.getAggregationName())
        .isEqualTo("io.opentelemetry.sdk.metrics.view.DefaultAggregation");
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
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
    assertThat(simple.getSourceView()).isEqualTo(view);
    assertThat(simple.getAggregationName())
        .isEqualTo("io.opentelemetry.sdk.metrics.view.DefaultAggregation");
  }

  @Test
  void metricDescriptor_isCompatible() {
    View view = View.builder().build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MetricDescriptor descriptor = MetricDescriptor.create(view, instrument);
    // Same name, description, source name, source description, source unit, source type, and source
    // value type is compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    View.builder().build(),
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE))))
        .isTrue();
    // Different name overridden by view is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(View.builder().setName("bar").build(), instrument)))
        .isFalse();
    // Different description overridden by view is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(View.builder().setDescription("foo").build(), instrument)))
        .isFalse();
    // Different aggregation overridden by view is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    View.builder().setAggregation(Aggregation.lastValue()).build(), instrument)))
        .isFalse();
    // Different instrument source name is not compatible
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
    // Different instrument source description is not compatible
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
    // Different instrument source unit is not compatible
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
    // Different instrument source type is not compatible
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
    // Different instrument source value type is not compatible
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
}
