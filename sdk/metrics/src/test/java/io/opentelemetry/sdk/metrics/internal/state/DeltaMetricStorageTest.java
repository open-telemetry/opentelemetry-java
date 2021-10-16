/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeltaMetricStorageTest {
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");

  private CollectionHandle collector1;
  private CollectionHandle collector2;
  private Set<CollectionHandle> allCollectors;
  private DeltaMetricStorage<DoubleAccumulation> storage;

  @BeforeEach
  void setup() {
    Supplier<CollectionHandle> supplier = CollectionHandle.createSupplier();
    collector1 = supplier.get();
    collector2 = supplier.get();
    allCollectors = CollectionHandle.mutableSet();
    allCollectors.add(collector1);
    allCollectors.add(collector2);
    storage =
        new DeltaMetricStorage<>(
            AggregatorFactory.sum()
                .create(
                    Resource.empty(),
                    InstrumentationLibraryInfo.create("test", "1.0"),
                    DESCRIPTOR,
                    METRIC_DESCRIPTOR,
                    ExemplarReservoir::noSamples));
  }

  @Test
  void collectionDeltaForMultiReader() {
    BoundStorageHandle bound = storage.bind(Attributes.empty());
    bound.recordDouble(1, Attributes.empty(), Context.root());
    // First collector only sees first recording.
    assertThat(storage.collectFor(collector1, allCollectors, /* suppressCollection=*/ false))
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(1));

    bound.recordDouble(2, Attributes.empty(), Context.root());
    // First collector only sees second recording.
    assertThat(storage.collectFor(collector1, allCollectors, /* suppressCollection=*/ false))
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(2));

    // First collector no longer sees a recording.
    assertThat(storage.collectFor(collector1, allCollectors, /* suppressCollection=*/ false))
        .isEmpty();

    // Second collector gets merged recordings
    assertThat(storage.collectFor(collector2, allCollectors, /* suppressCollection=*/ false))
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(3));

    // Second collector no longer sees a recording.
    assertThat(storage.collectFor(collector2, allCollectors, /* suppressCollection=*/ false))
        .isEmpty();
  }

  @Test
  void avoidCollectionInRapidSuccession() {
    BoundStorageHandle bound = storage.bind(Attributes.empty());
    bound.recordDouble(1, Attributes.empty(), Context.root());
    // First collector only sees first recording.
    assertThat(storage.collectFor(collector1, allCollectors, /* suppressCollection=*/ false))
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(1));
    // Add some data immediately after read, but pretent it hasn't been long.
    bound.recordDouble(2, Attributes.empty(), Context.root());
    // Collector1 doesn't see new data, because we don't recollect, but collector2 sees old delta.
    assertThat(storage.collectFor(collector1, allCollectors, /* suppressCollection=*/ true))
        .isEmpty();
    assertThat(storage.collectFor(collector2, allCollectors, /* suppressCollection=*/ true))
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(1));
    // After enough time passes, collector1 sees new data
    assertThat(storage.collectFor(collector1, allCollectors, /* suppressCollection=*/ false))
        .hasSize(1)
        .hasEntrySatisfying(Attributes.empty(), value -> assertThat(value.getValue()).isEqualTo(2));
  }
}
