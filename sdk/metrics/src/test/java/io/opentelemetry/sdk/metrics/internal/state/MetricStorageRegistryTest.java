/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetricStorageRegistry}. */
class MetricStorageRegistryTest {
  private static final MetricDescriptor SYNC_DESCRIPTOR =
      descriptor("sync", "description", InstrumentType.COUNTER);
  private static final MetricDescriptor OTHER_SYNC_DESCRIPTOR =
      descriptor("sync", "other_description", InstrumentType.COUNTER);
  private static final MetricDescriptor ASYNC_DESCRIPTOR =
      descriptor("async", "description", InstrumentType.OBSERVABLE_GAUGE);
  private static final MetricDescriptor OTHER_ASYNC_DESCRIPTOR =
      descriptor("async", "other_description", InstrumentType.OBSERVABLE_GAUGE);

  private final MetricStorageRegistry metricStorageRegistry = new MetricStorageRegistry();

  @Test
  void register_Sync() {
    TestMetricStorage storage = new TestMetricStorage(SYNC_DESCRIPTOR);
    assertThat(metricStorageRegistry.register(storage)).isSameAs(storage);
    assertThat(metricStorageRegistry.register(storage)).isSameAs(storage);
    assertThat(metricStorageRegistry.register(new TestMetricStorage(SYNC_DESCRIPTOR)))
        .isSameAs(storage);
  }

  @Test
  void register_SyncIncompatibleDescriptor() {
    TestMetricStorage storage = new TestMetricStorage(SYNC_DESCRIPTOR);
    assertThat(metricStorageRegistry.register(storage)).isSameAs(storage);

    assertThatThrownBy(
            () -> metricStorageRegistry.register(new TestMetricStorage(OTHER_SYNC_DESCRIPTOR)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void register_Async() {
    TestMetricStorage storage = new TestMetricStorage(ASYNC_DESCRIPTOR);
    assertThat(metricStorageRegistry.register(storage)).isSameAs(storage);
    assertThat(metricStorageRegistry.register(storage)).isSameAs(storage);
    assertThat(metricStorageRegistry.register(new TestMetricStorage(ASYNC_DESCRIPTOR)))
        .isSameAs(storage);
  }

  @Test
  void register_AsyncIncompatibleDescriptor() {
    TestMetricStorage storage = new TestMetricStorage(ASYNC_DESCRIPTOR);
    assertThat(metricStorageRegistry.register(storage)).isSameAs(storage);

    assertThatThrownBy(
            () -> metricStorageRegistry.register(new TestMetricStorage(OTHER_ASYNC_DESCRIPTOR)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric with same name and different descriptor already created.");
  }

  @Test
  void unregister() {
    TestMetricStorage storage1 = new TestMetricStorage(ASYNC_DESCRIPTOR);
    TestMetricStorage storage2 = new TestMetricStorage(OTHER_SYNC_DESCRIPTOR);

    metricStorageRegistry.register(storage1);
    metricStorageRegistry.register(storage2);
    assertThat(metricStorageRegistry.getMetrics())
        .hasSameElementsAs(Arrays.asList(storage1, storage2));

    metricStorageRegistry.unregister(storage1);
    assertThat(metricStorageRegistry.getMetrics())
        .hasSameElementsAs(Collections.singletonList(storage2));
  }

  private static MetricDescriptor descriptor(
      String name, String description, InstrumentType instrumentType) {
    return MetricDescriptor.create(
        View.builder().build(),
        InstrumentDescriptor.create(
            name, description, "1", instrumentType, InstrumentValueType.DOUBLE));
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
        CollectionInfo collectionInfo,
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
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
