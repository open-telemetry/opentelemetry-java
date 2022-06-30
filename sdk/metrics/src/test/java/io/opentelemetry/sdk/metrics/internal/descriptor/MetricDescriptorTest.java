/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import org.junit.jupiter.api.Test;

class MetricDescriptorTest {

  @Test
  void metricDescriptor_preservesInstrumentDescriptor() {
    View view = View.builder().build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MetricDescriptor simple =
        MetricDescriptor.create(view, SourceInfo.fromCurrentStack(), instrument);
    assertThat(simple.getName()).isEqualTo("name");
    assertThat(simple.getDescription()).isEqualTo("description");
    assertThat(simple.getView()).isEqualTo(view);
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
    assertThat(simple.getAggregationName()).isEqualTo("default");
  }

  @Test
  void metricDescriptor_overridesFromView() {
    View view = View.builder().setName("new_name").setDescription("new_description").build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.HISTOGRAM, InstrumentValueType.DOUBLE);
    MetricDescriptor simple =
        MetricDescriptor.create(view, SourceInfo.fromCurrentStack(), instrument);
    assertThat(simple.getName()).isEqualTo("new_name");
    assertThat(simple.getDescription()).isEqualTo("new_description");
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
    assertThat(simple.getView()).isEqualTo(view);
    assertThat(simple.getAggregationName()).isEqualTo("default");
  }

  @Test
  void metricDescriptor_isCompatible() {
    View view = View.builder().build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MetricDescriptor descriptor =
        MetricDescriptor.create(view, SourceInfo.fromCurrentStack(), instrument);
    // Same name, description, source name, source description, source unit, source type, and source
    // value type is compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    View.builder().build(),
                    SourceInfo.fromCurrentStack(),
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
                MetricDescriptor.create(
                    View.builder().setName("bar").build(),
                    SourceInfo.fromCurrentStack(),
                    instrument)))
        .isFalse();
    // Different description overridden by view is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    View.builder().setDescription("foo").build(),
                    SourceInfo.fromCurrentStack(),
                    instrument)))
        .isFalse();
    // Different aggregation overridden by view is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    View.builder().setAggregation(Aggregation.lastValue()).build(),
                    SourceInfo.fromCurrentStack(),
                    instrument)))
        .isFalse();
    // Different instrument source name is not compatible
    assertThat(
            descriptor.isCompatibleWith(
                MetricDescriptor.create(
                    view,
                    SourceInfo.fromCurrentStack(),
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
                    SourceInfo.fromCurrentStack(),
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
                    SourceInfo.fromCurrentStack(),
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
                    SourceInfo.fromCurrentStack(),
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
                    SourceInfo.fromCurrentStack(),
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.LONG))))
        .isFalse();
  }
}
