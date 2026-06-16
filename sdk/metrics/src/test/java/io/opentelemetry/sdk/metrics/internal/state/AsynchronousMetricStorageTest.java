/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.common.export.MemoryMode.REUSABLE_DATA;
import static io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal.asExemplarFilterInternal;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.data.MutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressLogger(AsynchronousMetricStorage.class)
@ExtendWith(MockitoExtension.class)
class AsynchronousMetricStorageTest {

  private static final long START_SECOND_NANOS = 1_000_000_000L;

  private static final int CARDINALITY_LIMIT = 25;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(AsynchronousMetricStorage.class);

  private final TestClock testClock = TestClock.create(Instant.ofEpochSecond(1));
  private final Resource resource = Resource.empty();
  private final InstrumentationScopeInfo scope = InstrumentationScopeInfo.empty();
  private final InstrumentSelector selector = InstrumentSelector.builder().setName("*").build();
  private final RegisteredView registeredView =
      RegisteredView.create(
          selector,
          View.builder().build(),
          AttributesProcessor.noop(),
          CARDINALITY_LIMIT,
          SourceInfo.noSourceInfo());

  @Mock private MetricReader reader;
  private RegisteredReader registeredReader;

  private AsynchronousMetricStorage<?> longCounterStorage;
  private AsynchronousMetricStorage<?> doubleCounterStorage;

  // Not using @BeforeEach since many methods require executing them for each MemoryMode
  void setup(MemoryMode memoryMode) {
    setup(memoryMode, AggregationTemporality.CUMULATIVE, ExemplarFilter.alwaysOff());
  }

  void setup(MemoryMode memoryMode, AggregationTemporality temporality) {
    setup(memoryMode, temporality, ExemplarFilter.alwaysOff());
  }

  void setup(
      MemoryMode memoryMode, AggregationTemporality temporality, ExemplarFilter exemplarFilter) {
    when(reader.getAggregationTemporality(any())).thenReturn(temporality);
    when(reader.getMemoryMode()).thenReturn(memoryMode);
    registeredReader = RegisteredReader.create(reader, ViewRegistry.create());

    longCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            testClock,
            InstrumentDescriptor.create(
                "long-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            asExemplarFilterInternal(exemplarFilter),
            /* enabled= */ true);
    doubleCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            testClock,
            InstrumentDescriptor.create(
                "double-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.empty()),
            asExemplarFilterInternal(exemplarFilter),
            /* enabled= */ true);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void recordLong(MemoryMode memoryMode) {
    setup(memoryMode);

    longCounterStorage.record(Attributes.builder().put("key", "a").build(), 1);
    longCounterStorage.record(Attributes.builder().put("key", "b").build(), 2);
    longCounterStorage.record(Attributes.builder().put("key", "c").build(), 3);

    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
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

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void recordDouble(MemoryMode memoryMode) {
    setup(memoryMode);

    doubleCounterStorage.record(Attributes.builder().put("key", "a").build(), 1.1);
    doubleCounterStorage.record(Attributes.builder().put("key", "b").build(), 2.2);
    doubleCounterStorage.record(Attributes.builder().put("key", "c").build(), 3.3);

    assertThat(doubleCounterStorage.collect(resource, scope, testClock.now()))
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

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void record_ProcessesAttributes(MemoryMode memoryMode) {
    setup(memoryMode);

    AsynchronousMetricStorage<?> storage =
        AsynchronousMetricStorage.create(
            registeredReader,
            RegisteredView.create(
                selector,
                View.builder().build(),
                AttributesProcessor.filterByKeyName(key -> key.equals("key1")),
                CARDINALITY_LIMIT,
                SourceInfo.noSourceInfo()),
            testClock,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            asExemplarFilterInternal(ExemplarFilter.alwaysOff()),
            /* enabled= */ true);

    storage.record(Attributes.builder().put("key1", "a").put("key2", "b").build(), 1);

    assertThat(storage.collect(resource, scope, testClock.now()))
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

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void record_MaxCardinality(MemoryMode memoryMode) {
    setup(memoryMode);

    for (int i = 0; i <= CARDINALITY_LIMIT + 1; i++) {
      longCounterStorage.record(Attributes.builder().put("key" + i, "val").build(), 1);
    }

    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints()).hasSize(CARDINALITY_LIMIT));
    logs.assertContains("Instrument long-counter has exceeded the maximum allowed cardinality");
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void record_DeltaHandlesSpotsAreReleasedAfterCollection(MemoryMode memoryMode) {
    setup(memoryMode, AggregationTemporality.DELTA);

    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      longCounterStorage.record(Attributes.builder().put("key" + i, "val").build(), 1);
    }

    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints()).hasSize(CARDINALITY_LIMIT - 1));
    logs.assertDoesNotContain(
        "Instrument long-counter has exceeded the maximum allowed cardinality");

    for (int i = 0; i < CARDINALITY_LIMIT - 1; i++) {
      // Different attribute
      longCounterStorage.record(Attributes.builder().put("key" + i, "val2").build(), 1);
    }

    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints()).hasSize(CARDINALITY_LIMIT - 1));
    logs.assertDoesNotContain(
        "Instrument long-counter has exceeded the maximum allowed cardinality");
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void record_DuplicateAttributes(MemoryMode memoryMode) {
    setup(memoryMode);

    longCounterStorage.record(Attributes.builder().put("key1", "a").build(), 1);
    longCounterStorage.record(Attributes.builder().put("key1", "a").build(), 2);

    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .satisfies(
            metricData ->
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasValue(3).hasAttributes(attributeEntry("key1", "a")))));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void collect_CumulativeReportsCumulativeObservations(MemoryMode memoryMode) {
    setup(memoryMode);

    // Record measurement and collect at time START_SECONDS + 10s
    testClock.advance(Duration.ofSeconds(10));
    longCounterStorage.record(Attributes.empty(), 3);
    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(3)
                                .hasAttributes(Attributes.empty())));
    registeredReader.setLastCollectEpochNanos(testClock.now());

    // Record measurements and collect at time START_SECONDS + 30s
    testClock.advance(Duration.ofSeconds(20));
    longCounterStorage.record(Attributes.empty(), 3);
    longCounterStorage.record(Attributes.builder().put("key", "value1").build(), 6);
    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(3)
                                .hasAttributes(Attributes.empty()),
                        point ->
                            point
                                .hasStartEpochNanos(
                                    testClock.now() - Duration.ofSeconds(20).toNanos())
                                .hasEpochNanos(testClock.now())
                                .hasValue(6)
                                .hasAttributes(Attributes.builder().put("key", "value1").build())));
    registeredReader.setLastCollectEpochNanos(testClock.now());

    // Record measurement and collect at time START_SECONDS + 35s
    testClock.advance(Duration.ofSeconds(5));
    longCounterStorage.record(Attributes.empty(), 4);
    longCounterStorage.record(Attributes.builder().put("key", "value2").build(), 5);
    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(4)
                                .hasAttributes(Attributes.empty()),
                        point ->
                            point
                                .hasStartEpochNanos(
                                    testClock.now() - Duration.ofSeconds(5).toNanos())
                                .hasEpochNanos(testClock.now())
                                .hasValue(5)
                                .hasAttributes(Attributes.builder().put("key", "value2").build())));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void collect_CumulativeSeriesDisappearsAndReappears(MemoryMode memoryMode) {
    setup(memoryMode);

    Attributes attrA = Attributes.empty();
    Attributes attrB = Attributes.builder().put("key", "b").build();

    // Collection 1: both series reported. start time = instrument creation time
    // (START_SECOND_NANOS)
    testClock.advance(Duration.ofSeconds(10));
    longCounterStorage.record(attrA, 1);
    longCounterStorage.record(attrB, 2);
    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasAttributes(attrA)
                                .hasValue(1),
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasAttributes(attrB)
                                .hasValue(2)));
    registeredReader.setLastCollectEpochNanos(testClock.now());

    // Collection 2: only attrA reported; attrB disappears and its handle is removed.
    testClock.advance(Duration.ofSeconds(10));
    longCounterStorage.record(attrA, 3);
    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasAttributes(attrA)
                                .hasValue(3)));
    registeredReader.setLastCollectEpochNanos(testClock.now());
    long collectTime2 = testClock.now();

    // Collection 3: attrB reappears. Its start time should be the time of the collection
    // immediately preceding its reappearance (collectTime2), not the original creation time.
    testClock.advance(Duration.ofSeconds(10));
    longCounterStorage.record(attrA, 5);
    longCounterStorage.record(attrB, 7);
    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(START_SECOND_NANOS)
                                .hasAttributes(attrA)
                                .hasValue(5),
                        point ->
                            point
                                .hasStartEpochNanos(collectTime2)
                                .hasAttributes(attrB)
                                .hasValue(7)));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void collect_DeltaComputesDiff(MemoryMode memoryMode) {
    setup(memoryMode);

    when(reader.getAggregationTemporality(any())).thenReturn(AggregationTemporality.DELTA);
    longCounterStorage =
        AsynchronousMetricStorage.create(
            registeredReader,
            registeredView,
            testClock,
            InstrumentDescriptor.create(
                "long-counter",
                "description",
                "unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            asExemplarFilterInternal(ExemplarFilter.alwaysOff()),
            /* enabled= */ true);

    // Record measurement and collect at time 10
    longCounterStorage.record(Attributes.empty(), 3);
    assertThat(longCounterStorage.collect(resource, scope, 10))
        .hasLongSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(10)
                                .hasValue(3)
                                .hasAttributes(Attributes.empty())));
    registeredReader.setLastCollectEpochNanos(10);

    // Record measurement and collect at time 30
    longCounterStorage.record(Attributes.empty(), 3);
    longCounterStorage.record(Attributes.builder().put("key", "value1").build(), 6);
    assertThat(longCounterStorage.collect(resource, scope, 30))
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
    longCounterStorage.record(Attributes.empty(), 4);
    longCounterStorage.record(Attributes.builder().put("key", "value2").build(), 5);
    assertThat(longCounterStorage.collect(resource, scope, 35))
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

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void recordLong_AlwaysOnCollectsExemplars(MemoryMode memoryMode) {
    setup(memoryMode, AggregationTemporality.CUMULATIVE, ExemplarFilter.alwaysOn());

    longCounterStorage.record(Attributes.empty(), 3);

    assertThat(longCounterStorage.collect(resource, scope, testClock.now()))
        .hasLongSumSatisfying(
            sum ->
                sum.hasPointsSatisfying(
                    point ->
                        point
                            .hasValue(3)
                            .hasAttributes(Attributes.empty())
                            .hasExemplarsSatisfying(exemplar -> exemplar.hasValue(3))));
  }

  @Test
  void collect_reusableData_reusedObjectsAreReturnedOnSecondCall() {
    setup(REUSABLE_DATA);

    longCounterStorage.record(Attributes.builder().put("key", "a").build(), 1);
    longCounterStorage.record(Attributes.builder().put("key", "b").build(), 2);
    longCounterStorage.record(Attributes.builder().put("key", "c").build(), 3);

    MetricData firstCollectMetricData =
        longCounterStorage.collect(resource, scope, testClock.now());
    assertThat(firstCollectMetricData)
        .satisfies(
            metricData ->
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(attributeEntry("key", "a"))
                                        .isInstanceOf(MutableLongPointData.class),
                                point ->
                                    point
                                        .hasValue(2)
                                        .hasAttributes(attributeEntry("key", "b"))
                                        .isInstanceOf(MutableLongPointData.class),
                                point ->
                                    point
                                        .hasValue(3)
                                        .hasAttributes(attributeEntry("key", "c"))
                                        .isInstanceOf(MutableLongPointData.class))));

    MetricData secondCollectMetricData =
        longCounterStorage.collect(resource, scope, testClock.now());

    Collection<? extends PointData> secondCollectPoints =
        secondCollectMetricData.getData().getPoints();
    Collection<? extends PointData> firstCollectionPoints =
        firstCollectMetricData.getData().getPoints();
    assertThat(secondCollectPoints).hasSameSizeAs(firstCollectionPoints);

    // Show that second returned objects have been used in first collect response as well
    // which proves there is reuse.
    for (PointData firstCollectionPoint : firstCollectionPoints) {
      assertThat(secondCollectPoints)
          .anySatisfy(point -> assertThat(point).isSameAs(firstCollectionPoint));
    }
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void enabledThenDisable_recordAndCollect(MemoryMode memoryMode) {
    setup(memoryMode);

    longCounterStorage.setEnabled(false);

    longCounterStorage.record(Attributes.empty(), 10);

    assertThat(longCounterStorage.collect(resource, scope, 0).isEmpty()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void enabledThenDisableThenEnable_recordAndCollect(MemoryMode memoryMode) {
    setup(memoryMode);

    longCounterStorage.setEnabled(false);
    longCounterStorage.setEnabled(true);

    longCounterStorage.record(Attributes.empty(), 10);

    assertThat(longCounterStorage.collect(resource, scope, 0).isEmpty()).isFalse();
  }
}
