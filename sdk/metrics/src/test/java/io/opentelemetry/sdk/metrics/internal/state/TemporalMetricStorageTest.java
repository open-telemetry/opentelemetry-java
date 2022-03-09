/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemporalMetricStorageTest {
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private static final InstrumentDescriptor ASYNC_DESCRIPTOR =
      InstrumentDescriptor.create(
          "name",
          "description",
          "unit",
          InstrumentType.OBSERVABLE_COUNTER,
          InstrumentValueType.DOUBLE);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final Aggregator<DoubleAccumulation> SUM =
      ((AggregatorFactory) Aggregation.sum())
          .createAggregator(DESCRIPTOR, ExemplarFilter.neverSample());

  private static final Aggregator<DoubleAccumulation> ASYNC_SUM =
      ((AggregatorFactory) Aggregation.sum())
          .createAggregator(ASYNC_DESCRIPTOR, ExemplarFilter.neverSample());

  private CollectionHandle collector1;
  private CollectionHandle collector2;
  private Set<CollectionHandle> allCollectors;

  @BeforeEach
  void setup() {
    Supplier<CollectionHandle> supplier = CollectionHandle.createSupplier();
    collector1 = supplier.get();
    collector2 = supplier.get();
    allCollectors = CollectionHandle.mutableSet();
    allCollectors.add(collector1);
    allCollectors.add(collector2);
  }

  private static Map<Attributes, DoubleAccumulation> createMeasurement(double value) {
    Map<Attributes, DoubleAccumulation> measurement = new HashMap<>();
    measurement.put(Attributes.empty(), DoubleAccumulation.create(value));
    return measurement;
  }

  @Test
  void synchronousCumulative_joinsWithLastMeasurementForCumulative() {
    AggregationTemporality temporality = AggregationTemporality.CUMULATIVE;
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(SUM, /* isSynchronous= */ true);
    // Send in new measurement at time 10 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                10))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));
    // Send in new measurement at time 30 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                30))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(30).hasValue(6));
    // Send in new measurement at time 40 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(4),
                0,
                60))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(4));
    // Send in new measurement at time 35 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(2),
                0,
                35))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(35).hasValue(8));
  }

  @Test
  void synchronousCumulative_dropsStaleAtLimit() {
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(SUM, /* isSynchronous= */ true);

    // Send in new measurement at time 10 for collector 1, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    for (int i = 0; i < MetricStorageUtils.MAX_ACCUMULATIONS; i++) {
      Attributes attr1 = Attributes.builder().put("key", "value" + i).build();
      measurement1.put(attr1, DoubleAccumulation.create(3));
    }
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                measurement1,
                0,
                10))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .hasSize(MetricStorageUtils.MAX_ACCUMULATIONS)
        .isNotEmpty()
        .allSatisfy(point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));

    // Send in new measurement at time 20 for collector 1, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 =
        Attributes.builder()
            .put("key", "value" + (MetricStorageUtils.MAX_ACCUMULATIONS + 1))
            .build();
    measurement2.put(attr2, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                measurement2,
                0,
                20))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .hasSize(1) // Limiting to only recent measurements means we cut everything here.
        .isNotEmpty()
        .extracting(PointData::getAttributes)
        .contains(attr2);
  }

  @Test
  void synchronousDelta_dropsStale() {
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(SUM, /* isSynchronous= */ true);

    // Send in new measurement at time 10 for collector 1, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    Attributes attr1 = Attributes.builder().put("key", "value1").build();
    measurement1.put(attr1, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                measurement1,
                0,
                10))
        .hasDoubleSum()
        .isDelta()
        .points()
        .hasSize(1)
        .isNotEmpty()
        .contains(ImmutableDoublePointData.create(0, 10, attr1, 3));

    // Send in new measurement at time 20 for collector 1, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 = Attributes.builder().put("key", "value2").build();
    measurement2.put(attr2, DoubleAccumulation.create(7));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                measurement2,
                0,
                20))
        .hasDoubleSum()
        .isDelta()
        .points()
        .hasSize(1)
        .isNotEmpty()
        .containsExactly(ImmutableDoublePointData.create(10, 20, attr2, 7));
  }

  @Test
  void synchronousDelta_useLastTimestamp() {
    AggregationTemporality temporality = AggregationTemporality.DELTA;
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(SUM, /* isSynchronous= */ true);
    // Send in new measurement at time 10 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                10))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));
    // Send in new measurement at time 30 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                30))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(10).hasEpochNanos(30).hasValue(3));
    // Send in new measurement at time 40 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(4),
                0,
                60))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(4));
    // Send in new measurement at time 35 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(2),
                0,
                35))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(30).hasEpochNanos(35).hasValue(2));
  }

  @Test
  void synchronous_deltaAndCumulative() {
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(SUM, /* isSynchronous= */ true);
    // Send in new measurement at time 10 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                createMeasurement(3),
                0,
                10))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));
    // Send in new measurement at time 30 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                createMeasurement(3),
                0,
                30))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(10).hasEpochNanos(30).hasValue(3));
    // Send in new measurement at time 40 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                createMeasurement(4),
                0,
                40))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(40).hasValue(4));
    // Send in new measurement at time 35 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                createMeasurement(2),
                0,
                35))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(30).hasEpochNanos(35).hasValue(2));
    // Send in new measurement at time 60 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                createMeasurement(4),
                0,
                60))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(8));
  }

  @Test
  void asynchronousCumulative_doesNotJoin() {
    AggregationTemporality temporality = AggregationTemporality.CUMULATIVE;
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(ASYNC_SUM, /* isSynchronous= */ false);
    // Send in new measurement at time 10 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                10))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));
    // Send in new measurement at time 30 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                30))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(30).hasValue(3));
    // Send in new measurement at time 40 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(4),
                0,
                60))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(4));
    // Send in new measurement at time 35 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(2),
                0,
                35))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(35).hasValue(2));
  }

  @Test
  void asynchronousCumulative_dropsStale() {
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(ASYNC_SUM, /* isSynchronous= */ false);

    // Send in new measurement at time 10 for collector 1, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    Attributes attr1 = Attributes.builder().put("key", "value1").build();
    measurement1.put(attr1, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                measurement1,
                0,
                10))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .hasSize(1)
        .isNotEmpty()
        .contains(ImmutableDoublePointData.create(0, 10, attr1, 3));

    // Send in new measurement at time 20 for collector 1, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 = Attributes.builder().put("key", "value2").build();
    measurement2.put(attr2, DoubleAccumulation.create(7));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                measurement2,
                0,
                20))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .hasSize(1)
        .isNotEmpty()
        .containsExactly(ImmutableDoublePointData.create(0, 20, attr2, 7));
  }

  @Test
  void asynchronousDelta_dropsStale() {
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(ASYNC_SUM, /* isSynchronous= */ false);

    // Send in new measurement at time 10 for collector 1, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    Attributes attr1 = Attributes.builder().put("key", "value1").build();
    measurement1.put(attr1, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                measurement1,
                0,
                10))
        .hasDoubleSum()
        .isDelta()
        .points()
        .hasSize(1)
        .isNotEmpty()
        .contains(ImmutableDoublePointData.create(0, 10, attr1, 3));

    // Send in new measurement at time 20 for collector 1, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 = Attributes.builder().put("key", "value2").build();
    measurement2.put(attr2, DoubleAccumulation.create(7));
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                measurement2,
                0,
                20))
        .hasDoubleSum()
        .isDelta()
        .points()
        .hasSize(1)
        .isNotEmpty()
        .containsExactly(ImmutableDoublePointData.create(10, 20, attr2, 7));
  }

  @Test
  void asynchronousDelta_diffsLastTimestamp() {
    AggregationTemporality temporality = AggregationTemporality.DELTA;
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(ASYNC_SUM, /* isSynchronous= */ false);
    // Send in new measurement at time 10 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                10))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));
    // Send in new measurement at time 30 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(3),
                0,
                30))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(10).hasEpochNanos(30).hasValue(0));
    // Send in new measurement at time 40 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(4),
                0,
                60))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(4));
    // Send in new measurement at time 35 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                temporality,
                createMeasurement(2),
                0,
                35))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(30).hasEpochNanos(35).hasValue(-1));
  }

  @Test
  void asynchronous_DeltaAndCumulative() {
    TemporalMetricStorage<DoubleAccumulation> storage =
        new TemporalMetricStorage<>(ASYNC_SUM, /* isSynchronous= */ false);

    // Send in new measurement at time 10 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                createMeasurement(3),
                0,
                10))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3));
    // Send in new measurement at time 30 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                createMeasurement(3),
                0,
                30))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(10).hasEpochNanos(30).hasValue(0));
    // Send in new measurement at time 40 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                createMeasurement(4),
                0,
                60))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(4));
    // Send in new measurement at time 35 for collector 1
    assertThat(
            storage.buildMetricFor(
                collector1,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.DELTA,
                createMeasurement(2),
                0,
                35))
        .hasDoubleSum()
        .isDelta()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(30).hasEpochNanos(35).hasValue(-1));

    // Send in new measurement at time 60 for collector 2
    assertThat(
            storage.buildMetricFor(
                collector2,
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                METRIC_DESCRIPTOR,
                AggregationTemporality.CUMULATIVE,
                createMeasurement(5),
                0,
                60))
        .hasDoubleSum()
        .isCumulative()
        .points()
        .isNotEmpty()
        .satisfiesExactly(
            point -> assertThat(point).hasStartEpochNanos(0).hasEpochNanos(60).hasValue(5));
  }
}
