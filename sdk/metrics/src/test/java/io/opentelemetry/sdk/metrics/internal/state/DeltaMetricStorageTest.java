/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import org.junit.jupiter.api.Test;

class DeltaMetricStorageTest {

  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);

  private final DeltaMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
      new DeltaMetricStorage<>(
          ((AggregatorFactory) Aggregation.sum())
              .createAggregator(DESCRIPTOR, ExemplarFilter.neverSample()),
          DESCRIPTOR);

  @Test
  void collect() {
    Attributes empty = Attributes.empty();
    BoundStorageHandle boundEmpty = storage.bind(empty);
    Attributes attrs = Attributes.builder().put("key", "value").build();
    BoundStorageHandle boundAttrs = storage.bind(attrs);

    boundEmpty.recordDouble(1, empty, Context.root());
    boundAttrs.recordDouble(2, attrs, Context.root());
    assertThat(storage.collect())
        .hasSize(2)
        .hasEntrySatisfying(empty, value -> assertThat(value.getValue()).isEqualTo(1))
        .hasEntrySatisfying(attrs, value -> assertThat(value.getValue()).isEqualTo(2));

    boundEmpty.recordDouble(2, empty, Context.root());
    assertThat(storage.collect())
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(2));

    assertThat(storage.collect()).isEmpty();
  }
}
