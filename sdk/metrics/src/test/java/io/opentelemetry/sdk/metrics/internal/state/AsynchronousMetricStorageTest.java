/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
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
  private final InstrumentSelector selector = InstrumentSelector.builder().setName("*").build();
  private final RegisteredView registeredView =
      RegisteredView.create(
          selector, View.builder().build(), AttributesProcessor.noop(), SourceInfo.noSourceInfo());
  private CollectionInfo collectionInfo;

  private AsynchronousMetricStorage<?, ?> longCounterStorage;
  private AsynchronousMetricStorage<?, ?> doubleCounterStorage;

  @BeforeEach
  void setup() {
    CollectionHandle handle = CollectionHandle.createSupplier().get();
    Set<CollectionHandle> all = CollectionHandle.mutableSet();
    all.add(handle);
    collectionInfo = CollectionInfo.create(handle, all, reader);
    when(reader.getAggregationTemporality(any())).thenReturn(AggregationTemporality.CUMULATIVE);

    longCounterStorage =
        AsynchronousMetricStorage.create(
            registeredView,
            InstrumentDescriptor.create(
                "long-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG));
    doubleCounterStorage =
        AsynchronousMetricStorage.create(
            registeredView,
            InstrumentDescriptor.create(
                "double-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE));
  }

  @Test
  void lockAndUnlock_long() {
    // Storage is initially locked and recording is not allowed
    longCounterStorage.recordLong(1, Attributes.empty());
    assertThat(
            longCounterStorage
                .collectAndReset(
                    collectionInfo,
                    resource,
                    scope,
                    0,
                    testClock.nanoTime(),
                    /* suppressSynchronousCollection= */ false)
                .isEmpty())
        .isTrue();
    logs.assertContains(
        "Cannot record measurements for instrument long-counter outside registered callbacks.");

    // After unlocking, recording is allowed
    longCounterStorage.unlock();
    longCounterStorage.recordLong(1, Attributes.empty());
    assertThat(
            longCounterStorage
                .collectAndReset(
                    collectionInfo,
                    resource,
                    scope,
                    0,
                    testClock.nanoTime(),
                    /* suppressSynchronousCollection= */ false)
                .isEmpty())
        .isFalse();

    // After locking, recording is once again not allowed
    longCounterStorage.lock();
    assertThat(
            longCounterStorage
                .collectAndReset(
                    collectionInfo,
                    resource,
                    scope,
                    0,
                    testClock.nanoTime(),
                    /* suppressSynchronousCollection= */ false)
                .isEmpty())
        .isTrue();
  }

  @Test
  void lockAndUnlock_double() {
    // Storage is initially locked and recording is not allowed
    doubleCounterStorage.recordDouble(1.1, Attributes.empty());
    assertThat(
            doubleCounterStorage
                .collectAndReset(
                    collectionInfo,
                    resource,
                    scope,
                    0,
                    testClock.nanoTime(),
                    /* suppressSynchronousCollection= */ false)
                .isEmpty())
        .isTrue();
    logs.assertContains(
        "Cannot record measurements for instrument double-counter outside registered callbacks.");

    // After unlocking, recording is allowed
    doubleCounterStorage.unlock();
    doubleCounterStorage.recordDouble(1.1, Attributes.empty());
    assertThat(
            doubleCounterStorage
                .collectAndReset(
                    collectionInfo,
                    resource,
                    scope,
                    0,
                    testClock.nanoTime(),
                    /* suppressSynchronousCollection= */ false)
                .isEmpty())
        .isFalse();

    // After locking, recording is once again not allowed
    doubleCounterStorage.lock();
    assertThat(
            doubleCounterStorage
                .collectAndReset(
                    collectionInfo,
                    resource,
                    scope,
                    0,
                    testClock.nanoTime(),
                    /* suppressSynchronousCollection= */ false)
                .isEmpty())
        .isTrue();
  }

  @Test
  void recordLong() {
    longCounterStorage.unlock();
    longCounterStorage.recordLong(1, Attributes.builder().put("key", "a").build());
    longCounterStorage.recordLong(2, Attributes.builder().put("key", "b").build());
    longCounterStorage.recordLong(3, Attributes.builder().put("key", "c").build());
    longCounterStorage.lock();

    assertThat(
            longCounterStorage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasValue(1).hasAttributes(attributeEntry("key", "a")),
                                point ->
                                    point.hasValue(2).hasAttributes(attributeEntry("key", "b")),
                                point ->
                                    point.hasValue(3).hasAttributes(attributeEntry("key", "c")))));
    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  void recordDouble() {
    doubleCounterStorage.unlock();
    doubleCounterStorage.recordDouble(1.1, Attributes.builder().put("key", "a").build());
    doubleCounterStorage.recordDouble(2.2, Attributes.builder().put("key", "b").build());
    doubleCounterStorage.recordDouble(3.3, Attributes.builder().put("key", "c").build());
    doubleCounterStorage.lock();

    assertThat(
            doubleCounterStorage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasValue(1.1).hasAttributes(attributeEntry("key", "a")),
                                point ->
                                    point.hasValue(2.2).hasAttributes(attributeEntry("key", "b")),
                                point ->
                                    point
                                        .hasValue(3.3)
                                        .hasAttributes(attributeEntry("key", "c")))));
    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  void record_ProcessesAttributes() {
    AsynchronousMetricStorage<?, ?> storage =
        AsynchronousMetricStorage.create(
            RegisteredView.create(
                selector,
                View.builder().build(),
                AttributesProcessor.filterByKeyName(key -> key.equals("key1")),
                SourceInfo.noSourceInfo()),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    storage.unlock();
    storage.recordLong(1, Attributes.builder().put("key1", "a").put("key2", "b").build());
    storage.lock();

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
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasValue(1).hasAttributes(attributeEntry("key1", "a")))));
    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  void record_MaxAccumulations() {
    longCounterStorage.unlock();
    for (int i = 0; i <= MetricStorageUtils.MAX_ACCUMULATIONS + 1; i++) {
      longCounterStorage.recordLong(1, Attributes.builder().put("key" + i, "val").build());
    }
    longCounterStorage.lock();

    assertThat(
            longCounterStorage.collectAndReset(
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
    logs.assertContains("Instrument long-counter has exceeded the maximum allowed accumulations");
  }

  @Test
  void record_DuplicateAttributes() {
    longCounterStorage.unlock();
    longCounterStorage.recordLong(1, Attributes.builder().put("key1", "a").build());
    longCounterStorage.recordLong(2, Attributes.builder().put("key1", "a").build());
    longCounterStorage.lock();

    assertThat(
            longCounterStorage.collectAndReset(
                collectionInfo,
                resource,
                scope,
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasValue(1).hasAttributes(attributeEntry("key1", "a")))));
    logs.assertContains(
        "Instrument long-counter has recorded multiple values for the same attributes");
  }
}
