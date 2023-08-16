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
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MetricDescriptorTest {

  @Test
  void metricDescriptor_preservesInstrumentDescriptor() {
    View view = View.builder().build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name",
            "description",
            "unit",
            InstrumentType.COUNTER,
            InstrumentValueType.DOUBLE,
            Advice.empty());
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
            "name",
            "description",
            "unit",
            InstrumentType.HISTOGRAM,
            InstrumentValueType.DOUBLE,
            Advice.empty());
    MetricDescriptor simple =
        MetricDescriptor.create(view, SourceInfo.fromCurrentStack(), instrument);
    assertThat(simple.getName()).isEqualTo("new_name");
    assertThat(simple.getDescription()).isEqualTo("new_description");
    assertThat(simple.getSourceInstrument()).isEqualTo(instrument);
    assertThat(simple.getView()).isEqualTo(view);
    assertThat(simple.getAggregationName()).isEqualTo("default");
  }

  @Test
  void metricDescriptor_equals() {
    View view = View.builder().build();
    InstrumentDescriptor instrument =
        InstrumentDescriptor.create(
            "name",
            "description",
            "unit",
            InstrumentType.COUNTER,
            InstrumentValueType.DOUBLE,
            Advice.empty());
    MetricDescriptor descriptor1 =
        MetricDescriptor.create(view, SourceInfo.fromCurrentStack(), instrument);
    // Same name (case-insensitive), description, view, source name (case-insensitive), source unit,
    // source description, source type, and source value type is equal. Advice is not part of
    // equals.
    MetricDescriptor descriptor2 =
        MetricDescriptor.create(
            View.builder().build(),
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "Name",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.builder().explicitBucketBoundaries(Arrays.asList(1.0, 2.0)).build()));
    assertThat(descriptor1).isEqualTo(descriptor2).hasSameHashCodeAs(descriptor2);
    // Different name overridden by view is not equal
    descriptor2 =
        MetricDescriptor.create(
            View.builder().setName("bar").build(), SourceInfo.fromCurrentStack(), instrument);
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different description overridden by view is not equal
    descriptor2 =
        MetricDescriptor.create(
            View.builder().setDescription("foo").build(),
            SourceInfo.fromCurrentStack(),
            instrument);
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different aggregation overridden by view is not equal
    descriptor2 =
        MetricDescriptor.create(
            View.builder().setAggregation(Aggregation.lastValue()).build(),
            SourceInfo.fromCurrentStack(),
            instrument);
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different instrument source name is not equal
    descriptor2 =
        MetricDescriptor.create(
            view,
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "foo",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.empty()));
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different instrument source description is not equal
    descriptor2 =
        MetricDescriptor.create(
            view,
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name",
                "foo",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.empty()));
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different instrument source unit is not equal
    descriptor2 =
        MetricDescriptor.create(
            view,
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "foo",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.empty()));
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different instrument source type is not equal
    descriptor2 =
        MetricDescriptor.create(
            view,
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.HISTOGRAM,
                InstrumentValueType.DOUBLE,
                Advice.empty()));
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
    // Different instrument source value type is not equal
    descriptor2 =
        MetricDescriptor.create(
            view,
            SourceInfo.fromCurrentStack(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()));
    assertThat(descriptor1).isNotEqualTo(descriptor2).doesNotHaveSameHashCodeAs(descriptor2);
  }
}
