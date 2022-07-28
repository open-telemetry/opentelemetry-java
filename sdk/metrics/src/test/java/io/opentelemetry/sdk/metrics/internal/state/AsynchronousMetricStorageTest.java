/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.SdkMeterProvider.MAX_ACCUMULATIONS;
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
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
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

  private final TestClock testClock = TestClock.create();
  private final Resource resource = Resource.empty();
  private final InstrumentationScopeInfo scope = InstrumentationScopeInfo.empty();
  private final InstrumentSelector selector = InstrumentSelector.builder().setName("*").build();
  private final RegisteredView registeredView =
      RegisteredView.create(
          selector, View.builder().build(), AttributesProcessor.noop(), SourceInfo.noSourceInfo());

  @Mock private MetricReader reader;
  private RegisteredReader registeredReader;

  private AsynchronousMetricStorage<?, ?> longCounterStorage;
  private AsynchronousMetricStorage<?, ?> doubleCounterStorage;

  @BeforeEach
  void setup() {
    when(reader.getAggregationTemporality(any())).thenReturn(AggregationTemporality.CUMULATIVE);
    registeredReader = RegisteredReader.create(reader, ViewRegistry.create());

    longCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            InstrumentDescriptor.create(
                "long-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG),
            MAX_ACCUMULATIONS);
    doubleCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            InstrumentDescriptor.create(
                "double-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE),
            MAX_ACCUMULATIONS);
  }

  @Test
  void recordLong() {
    longCounterStorage.recordLong(1, Attributes.builder().put("key", "a").build());
    longCounterStorage.recordLong(2, Attributes.builder().put("key", "b").build());
    longCounterStorage.recordLong(3, Attributes.builder().put("key", "c").build());

    assertThat(longCounterStorage.collectAndReset(resource, scope, 0, testClock.nanoTime()))
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
    doubleCounterStorage.recordDouble(1.1, Attributes.builder().put("key", "a").build());
    doubleCounterStorage.recordDouble(2.2, Attributes.builder().put("key", "b").build());
    doubleCounterStorage.recordDouble(3.3, Attributes.builder().put("key", "c").build());

    assertThat(doubleCounterStorage.collectAndReset(resource, scope, 0, testClock.nanoTime()))
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
            registeredReader,
            RegisteredView.create(
                selector,
                View.builder().build(),
                AttributesProcessor.filterByKeyName(key -> key.equals("key1")),
                SourceInfo.noSourceInfo()),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG),
            MAX_ACCUMULATIONS);

    storage.recordLong(1, Attributes.builder().put("key1", "a").put("key2", "b").build());

    assertThat(storage.collectAndReset(resource, scope, 0, testClock.nanoTime()))
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
    for (int i = 0; i <= MAX_ACCUMULATIONS + 1; i++) {
      longCounterStorage.recordLong(1, Attributes.builder().put("key" + i, "val").build());
    }

    assertThat(longCounterStorage.collectAndReset(resource, scope, 0, testClock.nanoTime()))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .hasSize(MAX_ACCUMULATIONS));
    logs.assertContains("Instrument long-counter has exceeded the maximum allowed accumulations");
  }

  @Test
  void record_DuplicateAttributes() {
    longCounterStorage.recordLong(1, Attributes.builder().put("key1", "a").build());
    longCounterStorage.recordLong(2, Attributes.builder().put("key1", "a").build());

    assertThat(longCounterStorage.collectAndReset(resource, scope, 0, testClock.nanoTime()))
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
