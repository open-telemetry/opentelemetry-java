/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressLogger(AsynchronousMetricStorage.class)
@ExtendWith(MockitoExtension.class)
class AsynchronousMetricStorageTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(AsynchronousMetricStorage.class);

  @Mock private MetricReader reader;

  private final TestClock testClock = TestClock.create();
  private final Resource resource = Resource.empty();
  private final InstrumentationScopeInfo scope = InstrumentationScopeInfo.empty();
  private CollectionInfo collectionInfo;

  @BeforeEach
  void setup() {
    CollectionHandle handle = CollectionHandle.createSupplier().get();
    Set<CollectionHandle> all = CollectionHandle.mutableSet();
    all.add(handle);
    collectionInfo = CollectionInfo.create(handle, all, reader);
  }

  @Test
  void recordLong() {
    AsynchronousMetricStorage<?> storage =
        AsynchronousMetricStorage.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    storage.recordLong(1, Attributes.builder().put("key", "a").build());
    storage.recordLong(2, Attributes.builder().put("key", "b").build());
    storage.recordLong(3, Attributes.builder().put("key", "c").build());

    assertThat(
            storage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .satisfiesExactlyInAnyOrder(
                        pointData ->
                            assertThat(pointData)
                                .hasValue(1)
                                .hasAttributes(Attributes.builder().put("key", "a").build()),
                        pointData ->
                            assertThat(pointData)
                                .hasValue(2)
                                .hasAttributes(Attributes.builder().put("key", "b").build()),
                        pointData ->
                            assertThat(pointData)
                                .hasValue(3)
                                .hasAttributes(Attributes.builder().put("key", "c").build())));
    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  void recordDouble() {
    AsynchronousMetricStorage<?> storage =
        AsynchronousMetricStorage.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE));

    storage.recordDouble(1.1, Attributes.builder().put("key", "a").build());
    storage.recordDouble(2.2, Attributes.builder().put("key", "b").build());
    storage.recordDouble(3.3, Attributes.builder().put("key", "c").build());

    assertThat(
            storage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getDoubleSumData().getPoints())
                    .satisfiesExactlyInAnyOrder(
                        pointData ->
                            assertThat(pointData)
                                .hasValue(1.1)
                                .hasAttributes(Attributes.builder().put("key", "a").build()),
                        pointData ->
                            assertThat(pointData)
                                .hasValue(2.2)
                                .hasAttributes(Attributes.builder().put("key", "b").build()),
                        pointData ->
                            assertThat(pointData)
                                .hasValue(3.3)
                                .hasAttributes(Attributes.builder().put("key", "c").build())));
    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  void record_ProcessesAttributes() {
    AsynchronousMetricStorage<?> storage =
        AsynchronousMetricStorage.create(
            View.builder().setAttributeFilter(key -> key.equals("key1")).build(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    storage.recordLong(1, Attributes.builder().put("key1", "a").put("key2", "b").build());

    assertThat(
            storage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .satisfiesExactlyInAnyOrder(
                        pointData ->
                            assertThat(pointData)
                                .hasValue(1)
                                .hasAttributes(Attributes.builder().put("key1", "a").build())));
    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  void record_MaxAccumulations() {
    AsynchronousMetricStorage<?> storage =
        AsynchronousMetricStorage.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    for (int i = 0; i <= MetricStorageUtils.MAX_ACCUMULATIONS + 1; i++) {
      storage.recordLong(1, Attributes.builder().put("key" + i, "val").build());
    }

    assertThat(
            storage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .hasSize(MetricStorageUtils.MAX_ACCUMULATIONS));
    logs.assertContains("Instrument name has exceeded the maximum allowed accumulations");
  }

  @Test
  void record_DuplicateAttributes() {
    AsynchronousMetricStorage<?> storage =
        AsynchronousMetricStorage.create(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    storage.recordLong(1, Attributes.builder().put("key1", "a").build());
    storage.recordLong(2, Attributes.builder().put("key1", "a").build());

    assertThat(
            storage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .satisfiesExactlyInAnyOrder(
                        pointData ->
                            assertThat(pointData)
                                .hasValue(1)
                                .hasAttributes(Attributes.builder().put("key1", "a").build())));
    logs.assertContains("Instrument name has recorded multiple values for the same attributes");
  }
}
