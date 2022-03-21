/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link MetricStorageRegistry}. */
@SuppressLogger(MetricStorageRegistry.class)
class MetricStorageRegistryTest {
  private static final MetricDescriptor SYNC_DESCRIPTOR =
      descriptor("sync", "description", InstrumentType.COUNTER);
  private static final MetricDescriptor OTHER_SYNC_DESCRIPTOR =
      descriptor("sync", "other_description", InstrumentType.COUNTER);
  private static final MetricDescriptor ASYNC_DESCRIPTOR =
      descriptor("async", "description", InstrumentType.OBSERVABLE_GAUGE);
  private static final MetricDescriptor OTHER_ASYNC_DESCRIPTOR =
      descriptor("async", "other_description", InstrumentType.OBSERVABLE_GAUGE);

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(MetricStorageRegistry.class);

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
    assertThat(logs.getEvents()).isEmpty();
    assertThat(metricStorageRegistry.register(new TestMetricStorage(OTHER_SYNC_DESCRIPTOR)))
        .isNotSameAs(storage);
    logs.assertContains("Found duplicate metric definition");
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
    assertThat(logs.getEvents()).isEmpty();
    assertThat(metricStorageRegistry.register(new TestMetricStorage(OTHER_ASYNC_DESCRIPTOR)))
        .isNotSameAs(storage);
    logs.assertContains("Found duplicate metric definition");
  }

  private static MetricDescriptor descriptor(
      String name, String description, InstrumentType instrumentType) {
    return MetricDescriptor.create(
        View.builder().build(),
        SourceInfo.fromCurrentStack(),
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
        InstrumentationScopeInfo instrumentationScopeInfo,
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
