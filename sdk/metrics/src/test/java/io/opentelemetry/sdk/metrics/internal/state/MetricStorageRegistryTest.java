/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetricStorageRegistry}. */
class MetricStorageRegistryTest {
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "1");
  private static final MetricDescriptor OTHER_METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "other_description", "1");

  @Test
  void register() {
    MeterSharedState meterSharedState = MeterSharedState.create(InstrumentationLibraryInfo.empty());
    TestMetricStorage testInstrument = new TestMetricStorage(METRIC_DESCRIPTOR);
    assertThat(meterSharedState.getMetricStorageRegistry().register(testInstrument))
        .isSameAs(testInstrument);
    assertThat(meterSharedState.getMetricStorageRegistry().register(testInstrument))
        .isSameAs(testInstrument);
    assertThat(
            meterSharedState
                .getMetricStorageRegistry()
                .register(new TestMetricStorage(METRIC_DESCRIPTOR)))
        .isSameAs(testInstrument);
  }

  @Test
  void register_OtherDescriptor() {
    MeterSharedState meterSharedState = MeterSharedState.create(InstrumentationLibraryInfo.empty());
    TestMetricStorage testInstrument = new TestMetricStorage(METRIC_DESCRIPTOR);
    assertThat(meterSharedState.getMetricStorageRegistry().register(testInstrument))
        .isSameAs(testInstrument);

    assertThatThrownBy(
            () ->
                meterSharedState
                    .getMetricStorageRegistry()
                    .register(new TestMetricStorage(OTHER_METRIC_DESCRIPTOR)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void register_OtherInstance() {
    MeterSharedState meterSharedState = MeterSharedState.create(InstrumentationLibraryInfo.empty());
    TestMetricStorage testInstrument = new TestMetricStorage(METRIC_DESCRIPTOR);
    assertThat(meterSharedState.getMetricStorageRegistry().register(testInstrument))
        .isSameAs(testInstrument);

    assertThatThrownBy(
            () ->
                meterSharedState
                    .getMetricStorageRegistry()
                    .register(new OtherTestMetricStorage(METRIC_DESCRIPTOR)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different instrument already created.");
  }

  private static final class TestMetricStorage implements MetricStorage, WriteableMetricStorage {
    private final MetricDescriptor descriptor;

    TestMetricStorage(MetricDescriptor descriptor) {
      this.descriptor = descriptor;
    }

    @Override
    public MetricDescriptor getMetricDescriptor() {
      return descriptor;
    }

    @Override
    public MetricData collectAndReset(
        CollectionHandle collector,
        Set<CollectionHandle> allCollectors,
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        AggregationTemporality temporality,
        long startEpochNanos,
        long epochNanos,
        boolean suppressSynchronousCollection) {
      return null;
    }

    @Override
    public BoundStorageHandle bind(Attributes attributes) {
      return null;
    }
  }

  private static final class OtherTestMetricStorage
      implements MetricStorage, WriteableMetricStorage {
    private final MetricDescriptor descriptor;

    OtherTestMetricStorage(MetricDescriptor descriptor) {
      this.descriptor = descriptor;
    }

    @Override
    public MetricDescriptor getMetricDescriptor() {
      return descriptor;
    }

    @Override
    public MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      AggregationTemporality temporality,
      long startEpochNanos,
      long epochNanos,
      boolean suppressSynchronousCollection) {
      return null;
    }

    @Override
    public BoundStorageHandle bind(Attributes attributes) {
      return null;
    }
  }
}
