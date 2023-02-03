/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.internal.state.Measurement.doubleMeasurement;
import static io.opentelemetry.sdk.metrics.internal.state.Measurement.longMeasurement;
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
                InstrumentValueType.LONG));
    doubleCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            InstrumentDescriptor.create(
                "double-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE));
  }

  @Test
  void recordLong() {
    longCounterStorage.record(
        longMeasurement(0, 1, 1, Attributes.builder().put("key", "a").build()));
    longCounterStorage.record(
        longMeasurement(0, 1, 2, Attributes.builder().put("key", "b").build()));
    longCounterStorage.record(
        longMeasurement(0, 1, 3, Attributes.builder().put("key", "c").build()));

    assertThat(longCounterStorage.collect(resource, scope, 0, testClock.nanoTime()))
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
    doubleCounterStorage.record(
        doubleMeasurement(0, 1, 1.1, Attributes.builder().put("key", "a").build()));
    doubleCounterStorage.record(
        doubleMeasurement(0, 1, 2.2, Attributes.builder().put("key", "b").build()));
    doubleCounterStorage.record(
        doubleMeasurement(0, 1, 3.3, Attributes.builder().put("key", "c").build()));

    assertThat(doubleCounterStorage.collect(resource, scope, 0, testClock.nanoTime()))
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
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    storage.record(
        longMeasurement(0, 1, 1, Attributes.builder().put("key1", "a").put("key2", "b").build()));

    assertThat(storage.collect(resource, scope, 0, testClock.nanoTime()))
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
  void record_MaxCardinality() {
    for (int i = 0; i <= MetricStorage.MAX_CARDINALITY + 1; i++) {
      longCounterStorage.record(
          longMeasurement(0, 1, 1, Attributes.builder().put("key" + i, "val").build()));
    }

    assertThat(longCounterStorage.collect(resource, scope, 0, testClock.nanoTime()))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .hasSize(MetricStorage.MAX_CARDINALITY));
    logs.assertContains("Instrument long-counter has exceeded the maximum allowed cardinality");
  }

  @Test
  void record_DuplicateAttributes() {
    longCounterStorage.record(
        longMeasurement(0, 1, 1, Attributes.builder().put("key1", "a").build()));
    longCounterStorage.record(
        longMeasurement(0, 1, 2, Attributes.builder().put("key1", "a").build()));

    assertThat(longCounterStorage.collect(resource, scope, 0, testClock.nanoTime()))
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

  @Test
  void collect_CumulativeReportsCumulativeObservations() {
    // Record measurement and collect at time 10
    longCounterStorage.record(longMeasurement(0, 10, 3, Attributes.empty()));
    assertThat(longCounterStorage.collect(resource, scope, 0, 0))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(10)
                                .hasValue(3)
                                .hasAttributes(Attributes.empty())));
    registeredReader.setLastCollectEpochNanos(10);

    // Record measurements and collect at time 30
    longCounterStorage.record(longMeasurement(0, 30, 3, Attributes.empty()));
    longCounterStorage.record(
        longMeasurement(0, 30, 6, Attributes.builder().put("key", "value1").build()));
    assertThat(longCounterStorage.collect(resource, scope, 0, 0))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(30)
                                .hasValue(3)
                                .hasAttributes(Attributes.empty()),
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(30)
                                .hasValue(6)
                                .hasAttributes(Attributes.builder().put("key", "value1").build())));
    registeredReader.setLastCollectEpochNanos(30);

    // Record measurement and collect at time 35
    longCounterStorage.record(longMeasurement(0, 35, 4, Attributes.empty()));
    longCounterStorage.record(
        longMeasurement(0, 35, 5, Attributes.builder().put("key", "value2").build()));
    assertThat(longCounterStorage.collect(resource, scope, 0, 0))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(35)
                                .hasValue(4)
                                .hasAttributes(Attributes.empty()),
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(35)
                                .hasValue(5)
                                .hasAttributes(Attributes.builder().put("key", "value2").build())));
  }

  @Test
  void collect_DeltaComputesDiff() {
    when(reader.getAggregationTemporality(any())).thenReturn(AggregationTemporality.DELTA);
    longCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            InstrumentDescriptor.create(
                "long-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG));

    // Record measurement and collect at time 10
    longCounterStorage.record(longMeasurement(0, 10, 3, Attributes.empty()));
    assertThat(longCounterStorage.collect(resource, scope, 0, 0))
        .hasLongSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(10)
                                .hasValue(3)
                                .hasAttributes(Attributes.empty())));
    registeredReader.setLastCollectEpochNanos(10);

    // Record measurement and collect at time 30
    longCounterStorage.record(longMeasurement(0, 30, 3, Attributes.empty()));
    longCounterStorage.record(
        longMeasurement(0, 30, 6, Attributes.builder().put("key", "value1").build()));
    assertThat(longCounterStorage.collect(resource, scope, 0, 0))
        .hasLongSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(10)
                                .hasEpochNanos(30)
                                .hasValue(0)
                                .hasAttributes(Attributes.empty()),
                        point ->
                            point
                                .hasStartEpochNanos(10)
                                .hasEpochNanos(30)
                                .hasValue(6)
                                .hasAttributes(Attributes.builder().put("key", "value1").build())));
    registeredReader.setLastCollectEpochNanos(30);

    // Record measurement and collect at time 35
    longCounterStorage.record(longMeasurement(0, 35, 4, Attributes.empty()));
    longCounterStorage.record(
        longMeasurement(0, 35, 5, Attributes.builder().put("key", "value2").build()));
    assertThat(longCounterStorage.collect(resource, scope, 0, 0))
        .hasLongSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(30)
                                .hasEpochNanos(35)
                                .hasValue(1)
                                .hasAttributes(Attributes.empty()),
                        point ->
                            point
                                .hasStartEpochNanos(30)
                                .hasEpochNanos(35)
                                .hasValue(5)
                                .hasAttributes(Attributes.builder().put("key", "value2").build())));
  }
}
