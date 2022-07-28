/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.SdkMeterProvider.MAX_ACCUMULATIONS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
  private static final Aggregator<DoubleAccumulation, DoubleExemplarData> SUM =
      ((AggregatorFactory) Aggregation.sum())
          .createAggregator(DESCRIPTOR, ExemplarFilter.neverSample());

  private static final Aggregator<DoubleAccumulation, DoubleExemplarData> ASYNC_SUM =
      ((AggregatorFactory) Aggregation.sum())
          .createAggregator(ASYNC_DESCRIPTOR, ExemplarFilter.neverSample());

  @Mock private MetricReader reader;
  private RegisteredReader registeredReader;

  @BeforeEach
  void setup() {
    registeredReader = RegisteredReader.create(reader, ViewRegistry.create());
    registeredReader.setLastCollectEpochNanos(0);
  }

  private static Map<Attributes, DoubleAccumulation> createMeasurement(double value) {
    Map<Attributes, DoubleAccumulation> measurement = new HashMap<>();
    measurement.put(Attributes.empty(), DoubleAccumulation.create(value));
    return measurement;
  }

  @Test
  void synchronousCumulative_joinsWithLastMeasurementForCumulative() {
    AggregationTemporality temporality = AggregationTemporality.CUMULATIVE;
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            SUM, /* isSynchronous= */ true, registeredReader, temporality, METRIC_DESCRIPTOR);
    // Send in new measurement at time 10
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 30
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(30).hasValue(6)));
    registeredReader.setLastCollectEpochNanos(30);

    // Send in new measurement at time 35
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(2), 0, 35))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(35).hasValue(8)));
  }

  @Test
  void synchronousCumulative_dropsStaleAtLimit() {
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            SUM,
            /* isSynchronous= */ true,
            registeredReader,
            AggregationTemporality.CUMULATIVE,
            METRIC_DESCRIPTOR);

    // Send in new measurement at time 10, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    for (int i = 0; i < MAX_ACCUMULATIONS; i++) {
      Attributes attr1 = Attributes.builder().put("key", "value" + i).build();
      measurement1.put(attr1, DoubleAccumulation.create(3));
    }
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement1, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .satisfies(
                        sumData ->
                            assertThat(sumData.getPoints())
                                .hasSize(MAX_ACCUMULATIONS)
                                .allSatisfy(
                                    point -> {
                                      assertThat(point.getStartEpochNanos()).isEqualTo(0);
                                      assertThat(point.getEpochNanos()).isEqualTo(10);
                                      assertThat(point.getValue()).isEqualTo(3);
                                    })));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 20, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 =
        Attributes.builder()
            .put("key", "value" + (MAX_ACCUMULATIONS + 1))
            .build();
    measurement2.put(attr2, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement2, 0, 20))
        .hasDoubleSumSatisfying(
            sum -> sum.isCumulative().hasPointsSatisfying(point -> point.hasAttributes(attr2)));
  }

  @Test
  void synchronousDelta_dropsStale() {
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            SUM,
            /* isSynchronous= */ true,
            registeredReader,
            AggregationTemporality.DELTA,
            METRIC_DESCRIPTOR);

    // Send in new measurement at time 10, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    Attributes attr1 = Attributes.builder().put("key", "value1").build();
    measurement1.put(attr1, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement1, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(10)
                                .hasAttributes(attr1)
                                .hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 20, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 = Attributes.builder().put("key", "value2").build();
    measurement2.put(attr2, DoubleAccumulation.create(7));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement2, 0, 20))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(10)
                                .hasEpochNanos(20)
                                .hasAttributes(attr2)
                                .hasValue(7)));
  }

  @Test
  void synchronousDelta_useLastTimestamp() {
    AggregationTemporality temporality = AggregationTemporality.DELTA;
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            SUM, /* isSynchronous= */ true, registeredReader, temporality, METRIC_DESCRIPTOR);
    // Send in new measurement at time 10
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 30
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(10).hasEpochNanos(30).hasValue(3)));
    registeredReader.setLastCollectEpochNanos(30);

    // Send in new measurement at time 35
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(2), 0, 35))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(30).hasEpochNanos(35).hasValue(2)));
  }

  @Test
  void asynchronousCumulative_doesNotJoin() {
    AggregationTemporality temporality = AggregationTemporality.CUMULATIVE;
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            ASYNC_SUM,
            /* isSynchronous= */ false,
            registeredReader,
            temporality,
            METRIC_DESCRIPTOR);
    // Send in new measurement at time 10
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 30
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(30).hasValue(3)));
    registeredReader.setLastCollectEpochNanos(30);

    // Send in new measurement at time 35
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(2), 0, 35))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(35).hasValue(2)));
  }

  @Test
  void asynchronousCumulative_dropsStale() {
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            ASYNC_SUM,
            /* isSynchronous= */ false,
            registeredReader,
            AggregationTemporality.CUMULATIVE,
            METRIC_DESCRIPTOR);

    // Send in new measurement at time 10, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    Attributes attr1 = Attributes.builder().put("key", "value1").build();
    measurement1.put(attr1, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement1, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(10)
                                .hasAttributes(attr1)
                                .hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 20, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 = Attributes.builder().put("key", "value2").build();
    measurement2.put(attr2, DoubleAccumulation.create(7));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement2, 0, 20))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(20)
                                .hasAttributes(attr2)
                                .hasValue(7)));
  }

  @Test
  void asynchronousDelta_dropsStale() {
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            ASYNC_SUM,
            /* isSynchronous= */ false,
            registeredReader,
            AggregationTemporality.DELTA,
            METRIC_DESCRIPTOR);

    // Send in new measurement at time 10, with attr1
    Map<Attributes, DoubleAccumulation> measurement1 = new HashMap<>();
    Attributes attr1 = Attributes.builder().put("key", "value1").build();
    measurement1.put(attr1, DoubleAccumulation.create(3));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement1, 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(10)
                                .hasAttributes(attr1)
                                .hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 20, with attr2
    // Result should drop accumulation for attr1, only reporting accumulation for attr2
    Map<Attributes, DoubleAccumulation> measurement2 = new HashMap<>();
    Attributes attr2 = Attributes.builder().put("key", "value2").build();
    measurement2.put(attr2, DoubleAccumulation.create(7));
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), measurement2, 0, 20))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(10)
                                .hasEpochNanos(20)
                                .hasAttributes(attr2)
                                .hasValue(7)));
  }

  @Test
  void asynchronousDelta_diffsLastTimestamp() {
    AggregationTemporality temporality = AggregationTemporality.DELTA;
    TemporalMetricStorage<DoubleAccumulation, DoubleExemplarData> storage =
        new TemporalMetricStorage<>(
            ASYNC_SUM,
            /* isSynchronous= */ false,
            registeredReader,
            temporality,
            METRIC_DESCRIPTOR);
    // Send in new measurement at time 10
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 10))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(0).hasEpochNanos(10).hasValue(3)));
    registeredReader.setLastCollectEpochNanos(10);

    // Send in new measurement at time 30
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(3), 0, 30))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(10).hasEpochNanos(30).hasValue(0)));
    registeredReader.setLastCollectEpochNanos(30);

    // Send in new measurement at time 35
    assertThat(
            storage.buildMetricFor(
                Resource.empty(), InstrumentationScopeInfo.empty(), createMeasurement(2), 0, 35))
        .hasDoubleSumSatisfying(
            sum ->
                sum.isDelta()
                    .hasPointsSatisfying(
                        point -> point.hasStartEpochNanos(30).hasEpochNanos(35).hasValue(-1)));
  }
}
