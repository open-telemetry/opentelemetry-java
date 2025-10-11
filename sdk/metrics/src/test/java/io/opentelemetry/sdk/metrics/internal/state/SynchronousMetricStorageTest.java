/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.common.export.MemoryMode.IMMUTABLE_DATA;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.Uninterruptibles;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.assertj.DoubleSumAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;

@SuppressLogger(DefaultSynchronousMetricStorage.class)
public class SynchronousMetricStorageTest {
  private static final Resource RESOURCE = Resource.empty();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.builder("test").setVersion("1.0").build();
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name",
          "description",
          "unit",
          InstrumentType.COUNTER,
          InstrumentValueType.DOUBLE,
          Advice.empty());
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final int CARDINALITY_LIMIT = 25;

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForType(DefaultSynchronousMetricStorage.class, Level.DEBUG);

  private RegisteredReader deltaReader;
  private RegisteredReader cumulativeReader;
  private final TestClock testClock = TestClock.create();
  private Aggregator<LongPointData> aggregator;
  private final AttributesProcessor attributesProcessor = AttributesProcessor.noop();

  private void initialize(MemoryMode memoryMode) {
    deltaReader =
        RegisteredReader.create(
            InMemoryMetricReader.builder()
                .setAggregationTemporalitySelector(unused -> AggregationTemporality.DELTA)
                .setMemoryMode(memoryMode)
                .build(),
            ViewRegistry.create());

    cumulativeReader =
        RegisteredReader.create(
            InMemoryMetricReader.builder().setMemoryMode(memoryMode).build(),
            ViewRegistry.create());

    aggregator =
        spy(
            ((AggregatorFactory) Aggregation.sum())
                .createAggregator(DESCRIPTOR, ExemplarFilter.alwaysOff(), memoryMode));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void recordDouble_NaN(MemoryMode memoryMode) {
    initialize(memoryMode);
    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    storage.recordDouble(Double.NaN, Attributes.empty(), Context.current());

    logs.assertContains(
        "Instrument name has recorded measurement Not-a-Number (NaN) value with attributes {}. Dropping measurement.");
    verify(aggregator, never()).createHandle();
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .isEqualTo(EmptyMetricData.getInstance());
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void attributesProcessor_applied(MemoryMode memoryMode) {
    initialize(memoryMode);

    Attributes attributes = Attributes.builder().put("K", "V").build();
    AttributesProcessor attributesProcessor =
        AttributesProcessor.append(Attributes.builder().put("modifiedK", "modifiedV").build());
    AttributesProcessor spyAttributesProcessor = spy(attributesProcessor);
    SynchronousMetricStorage storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader,
            METRIC_DESCRIPTOR,
            aggregator,
            spyAttributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);
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

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void recordAndCollect_CumulativeDoesNotReset(MemoryMode memoryMode) {
    initialize(memoryMode);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

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
  void recordAndCollect_DeltaResets_ImmutableData() {
    initialize(IMMUTABLE_DATA);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

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
  void recordAndCollect_DeltaResets_ReusableData() {
    initialize(MemoryMode.REUSABLE_DATA);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

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
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);

    deltaReader.setLastCollectEpochNanos(10);

    // Record measurement and collect at time 30
    storage.recordDouble(3, Attributes.empty(), Context.current());

    // We're switched to secondary map so a handle will be created
    verify(aggregator, times(2)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(10).hasEpochNanos(30).hasValue(3)));
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);

    deltaReader.setLastCollectEpochNanos(30);

    // Record measurements and collect at time 35
    storage.recordDouble(2, Attributes.empty(), Context.current());
    storage.recordDouble(4, Attributes.of(AttributeKey.stringKey("foo"), "bar"), Context.current());

    // We don't delete aggregator handles unless max cardinality reached, hence
    // aggregator handle is still there, thus no handle was created for empty(), but it will for
    // the "foo"
    verify(aggregator, times(3)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);

    MetricData metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 35);
    assertThat(metricData).hasDoubleSumSatisfying(DoubleSumAssert::isDelta);
    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(2)
                            .anySatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(30);
                                  assertThat(point.getEpochNanos()).isEqualTo(35);
                                  assertThat(point.getValue()).isEqualTo(2);
                                  assertThat(point.getAttributes()).isEqualTo(Attributes.empty());
                                })
                            .anySatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(30);
                                  assertThat(point.getEpochNanos()).isEqualTo(35);
                                  assertThat(point.getValue()).isEqualTo(4);
                                  assertThat(point.getAttributes())
                                      .isEqualTo(
                                          Attributes.of(AttributeKey.stringKey("foo"), "bar"));
                                })));

    assertThat(storage.getAggregatorHandlePool()).hasSize(0);

    deltaReader.setLastCollectEpochNanos(40);
    storage.recordDouble(6, Attributes.of(AttributeKey.stringKey("foo"), "bar"), Context.current());

    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 45))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(1)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(40);
                                  assertThat(point.getEpochNanos()).isEqualTo(45);
                                  assertThat(point.getValue()).isEqualTo(6);
                                  assertThat(point.getAttributes())
                                      .isEqualTo(
                                          Attributes.of(AttributeKey.stringKey("foo"), "bar"));
                                })));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void recordAndCollect_CumulativeAtLimit(MemoryMode memoryMode) {
    initialize(memoryMode);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            cumulativeReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    // Record measurements for CARDINALITY_LIMIT - 1, since 1 slot is reserved for the overflow
    // series
    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    verify(aggregator, times(CARDINALITY_LIMIT - 1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(CARDINALITY_LIMIT - 1)
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
        3, Attributes.builder().put("key", "value" + CARDINALITY_LIMIT).build(), Context.current());
    // Should not create an additional handles for the overflow series
    verify(aggregator, times(CARDINALITY_LIMIT)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 20))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(CARDINALITY_LIMIT)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                  assertThat(point.getEpochNanos()).isEqualTo(20);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })
                            .noneMatch(
                                point ->
                                    ("value" + CARDINALITY_LIMIT + 1)
                                        .equals(
                                            point
                                                .getAttributes()
                                                .get(AttributeKey.stringKey("key"))))
                            .satisfiesOnlyOnce(
                                point ->
                                    assertThat(point.getAttributes())
                                        .isEqualTo(MetricStorage.CARDINALITY_OVERFLOW))));
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    logs.assertContains("Instrument name has exceeded the maximum allowed cardinality");
  }

  @Test
  void recordAndCollect_DeltaAtLimit_ImmutableDataMemoryMode() {
    initialize(IMMUTABLE_DATA);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    // Record measurements for CARDINALITY_LIMIT - 1, since 1 slot is reserved for the overflow
    // series
    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    verify(aggregator, times(CARDINALITY_LIMIT - 1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(CARDINALITY_LIMIT - 1)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                  assertThat(point.getEpochNanos()).isEqualTo(10);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })));
    assertThat(storage.getAggregatorHandlePool()).hasSize(CARDINALITY_LIMIT - 1);

    assertThat(logs.getEvents()).isEmpty();
    deltaReader.setLastCollectEpochNanos(10);

    // Record measurement for additional attribute, should not exceed limit due to reset
    storage.recordDouble(
        3, Attributes.builder().put("key", "value" + CARDINALITY_LIMIT).build(), Context.current());
    // Should use handle returned to pool instead of creating new ones
    verify(aggregator, times(CARDINALITY_LIMIT - 1)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(CARDINALITY_LIMIT - 2);
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
                                        .put("key", "value" + CARDINALITY_LIMIT)
                                        .build())));
    assertThat(storage.getAggregatorHandlePool()).hasSize(CARDINALITY_LIMIT - 1);
    assertThat(logs.getEvents()).isEmpty();
    deltaReader.setLastCollectEpochNanos(20);

    // Record CARDINALITY_LIMIT measurements, causing one measurement to exceed the cardinality
    // limit and fall into the overflow series
    for (int i = 0; i < CARDINALITY_LIMIT; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    // Should use handles returned to pool instead of creating new ones
    verify(aggregator, times(CARDINALITY_LIMIT)).createHandle();
    assertThat(storage.getAggregatorHandlePool()).hasSize(0);
    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(CARDINALITY_LIMIT)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(20);
                                  assertThat(point.getEpochNanos()).isEqualTo(30);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })
                            .noneMatch(
                                point ->
                                    ("value" + CARDINALITY_LIMIT + 1)
                                        .equals(
                                            point
                                                .getAttributes()
                                                .get(AttributeKey.stringKey("key"))))
                            .satisfiesOnlyOnce(
                                point ->
                                    assertThat(point.getAttributes())
                                        .isEqualTo(MetricStorage.CARDINALITY_OVERFLOW))));

    assertThat(storage.getAggregatorHandlePool()).hasSize(CARDINALITY_LIMIT);
    logs.assertContains("Instrument name has exceeded the maximum allowed cardinality");
  }

  @Test
  void recordAndCollect_DeltaAtLimit_ReusableDataMemoryMode() {
    initialize(MemoryMode.REUSABLE_DATA);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    // Record measurements for CARDINALITY_LIMIT - 1, since 1 slot is reserved for the overflow
    // series
    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }
    verify(aggregator, times(CARDINALITY_LIMIT - 1)).createHandle();

    // First collect
    MetricData metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10);

    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        Assertions.assertThat(sumData.getPoints())
                            .hasSize(CARDINALITY_LIMIT - 1)
                            .allSatisfy(
                                point -> {
                                  Assertions.assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                  Assertions.assertThat(point.getEpochNanos()).isEqualTo(10);
                                  Assertions.assertThat(point.getValue()).isEqualTo(3);
                                })));

    assertThat(logs.getEvents()).isEmpty();

    deltaReader.setLastCollectEpochNanos(10);

    // Record CARDINALITY_LIMIT measurements, causing one measurement to exceed the cardinality
    // limit and fall into the overflow series
    for (int i = 0; i < CARDINALITY_LIMIT; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    // After first collection, we expect the secondary map which is empty to be used,
    // hence handle creation will still take place
    // The +1 is for the overflow handle
    verify(aggregator, times((CARDINALITY_LIMIT - 1) * 2 + 1)).createHandle();

    // Second collect
    metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 20);

    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(CARDINALITY_LIMIT)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(10);
                                  assertThat(point.getEpochNanos()).isEqualTo(20);
                                  assertThat(point.getValue()).isEqualTo(3);
                                })
                            .noneMatch(
                                point ->
                                    ("value" + CARDINALITY_LIMIT)
                                        .equals(
                                            point
                                                .getAttributes()
                                                .get(AttributeKey.stringKey("key"))))
                            .satisfiesOnlyOnce(
                                point ->
                                    assertThat(point.getAttributes())
                                        .isEqualTo(MetricStorage.CARDINALITY_OVERFLOW))));

    assertThat(storage.getAggregatorHandlePool()).isEmpty();

    logs.assertContains("Instrument name has exceeded the maximum allowed cardinality");
  }

  @Test
  void recordAndCollect_DeltaAtLimit_ReusableDataMemoryMode_ExpireUnused() {
    initialize(MemoryMode.REUSABLE_DATA);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    // 1st recording: Recording goes to active map
    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    // This will switch next recordings to the secondary map (which is empty)
    // by making it the active map
    storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10);

    // 2nd recording
    deltaReader.setLastCollectEpochNanos(10);
    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    // This switches maps again, so next recordings will be to the first map
    storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 10, 20);

    // 3rd recording: We're recording unseen attributes to a map we know is full,
    // since it was filled during 1st recording
    deltaReader.setLastCollectEpochNanos(20);
    for (int i = CARDINALITY_LIMIT - 1; i < (CARDINALITY_LIMIT - 1) + 15; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    MetricData metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 20, 30);

    assertOnlyOverflowWasRecorded(metricData, 20, 30, 15 * 3);

    // 4th recording: We're recording unseen attributes to a map we know is full,
    // since it was filled during *2nd* recording
    deltaReader.setLastCollectEpochNanos(30);
    for (int i = CARDINALITY_LIMIT - 1; i < (CARDINALITY_LIMIT - 1) + 15; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 30, 40);

    assertOnlyOverflowWasRecorded(metricData, 30, 40, 15 * 3);

    // 5th recording: Map should be empty, since all handlers were removed due to
    // no recording being done to them
    deltaReader.setLastCollectEpochNanos(40);
    for (int i = 0; i < 10; i++) {
      storage.recordDouble(
          3, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 40, 50);

    assertNumberOfPoints(metricData, 10);
    assertAllPointsWithValue(metricData, 40, 50, 3);
    assertOverflowDoesNotExists(metricData);

    // 6th recording: Map should be empty (we switched to secondary map), since all handlers
    // were removed due to no recordings being done to them
    deltaReader.setLastCollectEpochNanos(50);
    for (int i = 0; i < 12; i++) {
      storage.recordDouble(
          4, Attributes.builder().put("key", "value" + i).build(), Context.current());
    }

    metricData = storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 50, 60);

    assertNumberOfPoints(metricData, 12);
    assertAllPointsWithValue(metricData, 50, 60, 4);
    assertOverflowDoesNotExists(metricData);
  }

  @SuppressWarnings("SameParameterValue")
  private static void assertOnlyOverflowWasRecorded(
      MetricData metricData, long startTime, long endTime, double value) {

    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .hasSize(1)
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(startTime);
                                  assertThat(point.getEpochNanos()).isEqualTo(endTime);
                                  assertThat(point.getValue()).isEqualTo(value);
                                  assertThat(point.getAttributes())
                                      .isEqualTo(MetricStorage.CARDINALITY_OVERFLOW);
                                })));
  }

  private static void assertNumberOfPoints(MetricData metricData, int numberOfPoints) {
    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(sumData -> assertThat(sumData.getPoints()).hasSize(numberOfPoints)));
  }

  private static void assertAllPointsWithValue(
      MetricData metricData, long startTime, long endTime, double value) {
    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .allSatisfy(
                                point -> {
                                  assertThat(point.getStartEpochNanos()).isEqualTo(startTime);
                                  assertThat(point.getEpochNanos()).isEqualTo(endTime);
                                  assertThat(point.getValue()).isEqualTo(value);
                                })));
  }

  private static void assertOverflowDoesNotExists(MetricData metricData) {
    assertThat(metricData)
        .hasDoubleSumSatisfying(
            sum ->
                sum.satisfies(
                    sumData ->
                        assertThat(sumData.getPoints())
                            .noneMatch(
                                point ->
                                    point
                                        .getAttributes()
                                        .equals(MetricStorage.CARDINALITY_OVERFLOW))));
  }

  @ParameterizedTest
  @MethodSource("concurrentStressTestArguments")
  void recordAndCollect_concurrentStressTest(
      DefaultSynchronousMetricStorage<?> storage, BiConsumer<Double, AtomicDouble> collect) {
    // Define record threads. Each records a value of 1.0, 2000 times
    List<Thread> threads = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(4);
    for (int i = 0; i < 4; i++) {
      Thread thread =
          new Thread(
              () -> {
                for (int j = 0; j < 2000; j++) {
                  storage.recordDouble(1.0, Attributes.empty(), Context.current());
                  Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(1));
                }
                latch.countDown();
              });
      threads.add(thread);
    }

    // Define collect thread. Collect thread collects and aggregates the
    AtomicDouble cumulativeSum = new AtomicDouble();
    Thread collectThread =
        new Thread(
            () -> {
              int extraCollects = 0;
              // If we terminate when latch.count() == 0, the last collect may have occurred before
              // the last recorded measurement. To ensure we collect all measurements, we collect
              // one extra time after latch.count() == 0.
              while (latch.getCount() != 0 || extraCollects <= 1) {
                Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(1));
                MetricData metricData =
                    storage.collect(Resource.empty(), InstrumentationScopeInfo.empty(), 0, 1);
                if (!metricData.isEmpty()) {
                  metricData.getDoubleSumData().getPoints().stream()
                      .findFirst()
                      .ifPresent(pointData -> collect.accept(pointData.getValue(), cumulativeSum));
                }
                if (latch.getCount() == 0) {
                  extraCollects++;
                }
              }
            });

    // Start all the threads
    collectThread.start();
    threads.forEach(Thread::start);

    // Wait for the collect thread to end, which collects until the record threads are done
    Uninterruptibles.joinUninterruptibly(collectThread);

    assertThat(cumulativeSum.get()).isEqualTo(8000.0);
  }

  private static Stream<Arguments> concurrentStressTestArguments() {
    List<Arguments> argumentsList = new ArrayList<>();

    for (MemoryMode memoryMode : MemoryMode.values()) {
      Aggregator<PointData> aggregator =
          ((AggregatorFactory) Aggregation.sum())
              .createAggregator(DESCRIPTOR, ExemplarFilter.alwaysOff(), memoryMode);

      argumentsList.add(
          Arguments.of(
              // Delta
              new DefaultSynchronousMetricStorage<>(
                  RegisteredReader.create(
                      InMemoryMetricReader.builder()
                          .setAggregationTemporalitySelector(unused -> AggregationTemporality.DELTA)
                          .setMemoryMode(memoryMode)
                          .build(),
                      ViewRegistry.create()),
                  METRIC_DESCRIPTOR,
                  aggregator,
                  AttributesProcessor.noop(),
                  CARDINALITY_LIMIT,
                  /* enabled= */ true),
              (BiConsumer<Double, AtomicDouble>)
                  (value, cumulativeCount) -> cumulativeCount.addAndGet(value)));

      argumentsList.add(
          Arguments.of(
              // Cumulative
              new DefaultSynchronousMetricStorage<>(
                  RegisteredReader.create(
                      InMemoryMetricReader.builder().setMemoryMode(memoryMode).build(),
                      ViewRegistry.create()),
                  METRIC_DESCRIPTOR,
                  aggregator,
                  AttributesProcessor.noop(),
                  CARDINALITY_LIMIT,
                  /* enabled= */ true),
              (BiConsumer<Double, AtomicDouble>)
                  (value, cumulativeCount) -> cumulativeCount.set(value)));
    }

    return argumentsList.stream();
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void enabledThenDisable_isEnabled(MemoryMode memoryMode) {
    initialize(memoryMode);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    storage.setEnabled(false);

    assertThat(storage.isEnabled()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void enabledThenDisableThenEnable_isEnabled(MemoryMode memoryMode) {
    initialize(memoryMode);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    storage.setEnabled(false);
    storage.setEnabled(true);

    assertThat(storage.isEnabled()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void enabledThenDisable_recordAndCollect(MemoryMode memoryMode) {
    initialize(memoryMode);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    storage.setEnabled(false);

    storage.recordDouble(10d, Attributes.empty(), Context.current());

    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10).isEmpty()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void enabledThenDisableThenEnable_recordAndCollect(MemoryMode memoryMode) {
    initialize(memoryMode);

    DefaultSynchronousMetricStorage<?> storage =
        new DefaultSynchronousMetricStorage<>(
            deltaReader,
            METRIC_DESCRIPTOR,
            aggregator,
            attributesProcessor,
            CARDINALITY_LIMIT,
            /* enabled= */ true);

    storage.setEnabled(false);
    storage.setEnabled(true);

    storage.recordDouble(10d, Attributes.empty(), Context.current());

    assertThat(storage.collect(RESOURCE, INSTRUMENTATION_SCOPE_INFO, 0, 10).isEmpty()).isFalse();
  }
}
