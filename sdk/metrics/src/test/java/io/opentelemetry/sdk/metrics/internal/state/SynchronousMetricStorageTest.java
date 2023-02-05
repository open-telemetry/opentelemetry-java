/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(DefaultSynchronousMetricStorage.class)
public class SynchronousMetricStorageTest {
  private static final Resource RESOURCE = Resource.empty();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.builder("test").setVersion("1.0").build();
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DefaultSynchronousMetricStorage.class);

  private final RegisteredReader deltaReader =
      RegisteredReader.create(InMemoryMetricReader.createDelta(), ViewRegistry.create());
  private final RegisteredReader cumulativeReader =
      RegisteredReader.create(InMemoryMetricReader.create(), ViewRegistry.create());
  private final TestClock testClock = TestClock.create();
  private final Aggregator<LongPointData, LongExemplarData> aggregator =
      spy(
          ((AggregatorFactory) Aggregation.sum())
              .createAggregator(DESCRIPTOR, ExemplarFilter.alwaysOff()));
  private final AttributesProcessor attributesProcessor = AttributesProcessor.noop();

  @Test
  void attributesProcessor_applied() {
    Attributes attributes = Attributes.builder().put("K", "V").build();
    AttributesProcessor attributesProcessor =
        AttributesProcessor.append(Attributes.builder().put("modifiedK", "modifiedV").build());
    AttributesProcessor spyAttributesProcessor = spy(attributesProcessor);
    SynchronousMetricStorage storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader, METRIC_DESCRIPTOR, aggregator, spyAttributesProcessor);
    storage.recordDouble(1, attributes, Context.root());
    MetricData md = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, testClock.now());
    assertThat(md)
        .hasDoubleSumSatisfying(
            sum ->
                sum.hasPointsSatisfying(
                    point ->
                        point.hasAttributes(
                            attributeEntry("K", "V"), attributeEntry("modifiedK", "modifiedV"))));
  }

  @Test
  void recordAndCollect_CumulativeDoesNotReset() {
    DefaultSynchronousMetricStorage<?, ?> storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader, METRIC_DESCRIPTOR, aggregator, attributesProcessor);

    // Record measurement and collect at time 10
    storage.recordDouble(3, Attributes.empty(), Context.current());
    verify(aggregator, times(1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3)));
    cumulativeReader.setLastCollectEpochNanos(10);

    // Record measurement and collect at time 30
    storage.recordDouble(3, Attributes.empty(), Context.current());
    verify(aggregator, times(1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(30).hasValue(6)));
    cumulativeReader.setLastCollectEpochNanos(30);

    // Record measurement and collect at time 35
    storage.recordDouble(2, Attributes.empty(), Context.current());
    verify(aggregator, times(1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 35))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(35).hasValue(8)));
  }

  @Test
  void recordAndCollect_DeltaResets() {
    DefaultSynchronousMetricStorage<?, ?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader, METRIC_DESCRIPTOR, aggregator, attributesProcessor);

    // Record measurement and collect at time 10
    storage.recordDouble(3, Attributes.empty(), Context.current());
    verify(aggregator, times(1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3)));
    assertThat(storage.getAggregatorHandlePool()).hasSize(1);
    deltaReader.setLastCollectEpochNanos(10);

    // Record measurement and collect at time 30
    storage.recordDouble(3, Attributes.empty(), Context.current());
    // AggregatorHandle should be returned to the pool on reset so shouldn't create additional
    // handles
    verify(aggregator, times(1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(10).hasEpochNanos(30).hasValue(3)));
    assertThat(storage.getAggregatorHandlePool()).hasSize(1);
    deltaReader.setLastCollectEpochNanos(30);

    // Record measurement and collect at time 35
    storage.recordDouble(2, Attributes.empty(), Context.current());
    verify(aggregator, times(1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 35))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(30).hasEpochNanos(35).hasValue(2)));
    assertThat(storage.getAggregatorHandlePool()).hasSize(1);
  }

  @Test
  void recordAndCollect_CumulativeAtLimit() {
    DefaultSynchronousMetricStorage<?, ?> storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader, METRIC_DESCRIPTOR, aggregator, attributesProcessor);

    // Record measurements for max number of attributes
    for (int i = 0; i < MetricStorage.MAX_CARDINALITY; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    verify(aggregator, times(MetricStorage.MAX_CARDINALITY)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(MetricStorage.MAX_CARDINALITY)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                  assertThat(point.getEpochNanos()).isEqualTo(10);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })));
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(logs.getEvents()).isEmpty();
    cumulativeReader.setLastCollectEpochNanos(10);

    // Record measurement for additional attribute, exceeding limit
    storage.recordDouble(
        3,
        Attributes.builder().put("key", "value" + MetricStorage.MAX_CARDINALITY + 1).build(),
        Context.current());
    // Should not create additional handles after MAX_CARDINALITY is reached
    verify(aggregator, times(MetricStorage.MAX_CARDINALITY)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 20))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(MetricStorage.MAX_CARDINALITY)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                  assertThat(point.getEpochNanos()).isEqualTo(20);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })
                            .noneMatch(
                                point ->
                                    point
                                        .getAttributes()
                                        .get(AttributeKey.stringKey("key"))
                                        .equals("value" + MetricStorage.MAX_CARDINALITY + 1))));
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    logs.assertContains("Instrument name has exceeded the maximum allowed cardinality");
  }

  @Test
  void recordAndCollect_DeltaAtLimit() {
    DefaultSynchronousMetricStorage<?, ?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader, METRIC_DESCRIPTOR, aggregator, attributesProcessor);

    // Record measurements for max number of attributes
    for (int i = 0; i < MetricStorage.MAX_CARDINALITY; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    verify(aggregator, times(MetricStorage.MAX_CARDINALITY)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(MetricStorage.MAX_CARDINALITY)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                  assertThat(point.getEpochNanos()).isEqualTo(10);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })));
    assertThat(storage.getAggregatorHandlePool()).hasSize(MetricStorage.MAX_CARDINALITY);
    assertThat(logs.getEvents()).isEmpty();
    deltaReader.setLastCollectEpochNanos(10);

    // Record measurement for additional attribute, should not exceed limit due to reset
    storage.recordDouble(
        3,
        Attributes.builder().put("key", "value" + MetricStorage.MAX_CARDINALITY + 1).build(),
        Context.current());
    // Should use handle returned to pool instead of creating new ones
    verify(aggregator, times(MetricStorage.MAX_CARDINALITY)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(MetricStorage.MAX_CARDINALITY - 1);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 20))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(10)
                                .hasEpochNanos(20)
                                .hasValue(3)
                                .hasAttributes(
                                    Attributes.builder()
                                        .put("key", "value" + MetricStorage.MAX_CARDINALITY + 1)
                                        .build())));
    assertThat(storage.getAggregatorHandlePool()).hasSize(MetricStorage.MAX_CARDINALITY);
    assertThat(logs.getEvents()).isEmpty();
    deltaReader.setLastCollectEpochNanos(20);

    // Record measurements exceeding max number of attributes. Last measurement should be dropped
    for (int i = 0; i < MetricStorage.MAX_CARDINALITY + 1; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    // Should use handles returned to pool instead of creating new ones
    verify(aggregator, times(MetricStorage.MAX_CARDINALITY)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(MetricStorage.MAX_CARDINALITY)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(20);
                                  assertThat(point.getEpochNanos()).isEqualTo(30);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })
                            .noneMatch(
                                point ->
                                    point
                                        .getAttributes()
                                        .get(AttributeKey.stringKey("key"))
                                        .equals("value" + MetricStorage.MAX_CARDINALITY + 1))));
    assertThat(storage.getAggregatorHandlePool()).hasSize(MetricStorage.MAX_CARDINALITY);
    logs.assertContains("Instrument name has exceeded the maximum allowed cardinality");
  }
}
