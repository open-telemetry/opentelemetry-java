/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.metrics.internal.state.MetricStorage.DEFAULT_MAX_CARDINALITY;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.state.DefaultSynchronousMetricStorage;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressLogger(
    loggerName = "io.opentelemetry.sdk.metrics.internal.state.AsynchronousMetricStorage")
@SuppressLogger(DefaultSynchronousMetricStorage.class)
class CardinalityTest {

  private InMemoryMetricReader deltaReader;
  private InMemoryMetricReader cumulativeReader;
  private Meter meter;

  @BeforeEach
  void setup() {
    deltaReader = InMemoryMetricReader.createDelta();
    cumulativeReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(deltaReader)
            .registerMetricReader(cumulativeReader)
            .build();
    meter = sdkMeterProvider.get(CardinalityTest.class.getName());
  }

  /**
   * Records to sync instruments, with distinct attributes each time. Validates that stale metrics
   * are dropped for delta and cumulative readers. Stale metrics are those with attributes that did
   * not receive recordings in the most recent collection.
   *
   * <p>Effectively, we make sure we cap-out at attribute size = {@link
   * MetricStorage#DEFAULT_MAX_CARDINALITY}.
   */
  @Test
  void staleMetricsDropped_synchronousInstrument() {
    LongCounter syncCounter = meter.counterBuilder("sync-counter").build();
    // Note: This constant comes from MetricStorageUtils, but it's package-private.
    for (int i = 1; i <= DEFAULT_MAX_CARDINALITY; i++) {
      syncCounter.add(1, Attributes.builder().put("key", "num_" + i).build());

      // DELTA reader only has latest
      assertThat(deltaReader.collectAllMetrics())
          .as("Delta collection " + i)
          .satisfiesExactly(
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSumSatisfying(sum -> sum.isDelta().hasPointsSatisfying(point -> {})));

      // Make sure we preserve previous cumulatives
      int currentSize = i;
      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .satisfiesExactly(
              metricData ->
                  assertThat(metricData)
                      .hasName("sync-counter")
                      .hasLongSumSatisfying(
                          sum ->
                              sum.isCumulative()
                                  .satisfies(
                                      (Consumer<SumData<LongPointData>>)
                                          sumPointData ->
                                              assertThat(sumPointData.getPoints().size())
                                                  .isEqualTo(currentSize))));
    }
    // Now punch the limit and ONLY metrics we just recorded stay, due to simplistic GC.
    for (int i = DEFAULT_MAX_CARDINALITY + 1; i <= DEFAULT_MAX_CARDINALITY + 10; i++) {
      syncCounter.add(1, Attributes.builder().put("key", "num_" + i).build());
    }
    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection - post limit @ 10")
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(10))));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection - post limit @ 10")
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))));
  }

  /**
   * Records to async instruments, with distinct attributes each time. Validates that stale metrics
   * are dropped for delta and cumulative readers. Stale metrics are those with attributes that did
   * not receive recordings in the most recent collection.
   */
  @Test
  void staleMetricsDropped_asynchronousInstrument() {
    AtomicLong count = new AtomicLong();

    meter
        .counterBuilder("async-counter")
        .buildWithCallback(
            measurement ->
                measurement.record(
                    1, Attributes.builder().put("key", "num_" + count.incrementAndGet()).build()));

    for (int i = 1; i <= 5; i++) {
      assertThat(deltaReader.collectAllMetrics())
          .as("Delta collection " + i)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSumSatisfying(sum -> sum.isDelta().hasPointsSatisfying(point -> {})));

      assertThat(cumulativeReader.collectAllMetrics())
          .as("Cumulative collection " + i)
          .satisfiesExactlyInAnyOrder(
              metricData ->
                  assertThat(metricData)
                      .hasName("async-counter")
                      .hasLongSumSatisfying(
                          sum -> sum.isCumulative().hasPointsSatisfying(point -> {})));
    }
  }

  /**
   * Records to sync instruments, many distinct attributes. Validates that the {@code
   * MetricStorageUtils#MAX_CARDINALITY} is enforced for each instrument.
   */
  @Test
  void defaultCardinalityLimits_synchronousInstrument() {
    LongCounter syncCounter1 = meter.counterBuilder("sync-counter1").build();
    LongCounter syncCounter2 = meter.counterBuilder("sync-counter2").build();
    for (int i = 0; i < DEFAULT_MAX_CARDINALITY + 1; i++) {
      syncCounter1.add(1, Attributes.builder().put("key", "value" + i).build());
      syncCounter2.add(1, Attributes.builder().put("key", "value" + i).build());
    }

    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("sync-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))));
  }

  /**
   * Records to sync instruments, many distinct attributes. Validates that the {@code
   * MetricStorageUtils#MAX_CARDINALITY} is enforced for each instrument.
   */
  @Test
  void defaultCardinalityLimits_asynchronousInstrument() {
    Consumer<ObservableLongMeasurement> callback =
        measurement -> {
          for (int i = 0; i < DEFAULT_MAX_CARDINALITY + 1; i++) {
            measurement.record(1, Attributes.builder().put("key", "value" + i).build());
          }
        };
    meter.counterBuilder("async-counter1").buildWithCallback(callback);
    meter.counterBuilder("async-counter2").buildWithCallback(callback);

    assertThat(deltaReader.collectAllMetrics())
        .as("Delta collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))));

    assertThat(cumulativeReader.collectAllMetrics())
        .as("Cumulative collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))),
            metricData ->
                assertThat(metricData)
                    .hasName("async-counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    (Consumer<SumData<LongPointData>>)
                                        sumPointData ->
                                            assertThat(sumPointData.getPoints().size())
                                                .isEqualTo(DEFAULT_MAX_CARDINALITY))));
  }

  /**
   * Validate ability to customize metric reader cardinality limits via {@link
   * SdkMeterProviderBuilder#registerMetricReader(MetricReader, CardinalityLimitSelector)}, and view
   * cardinality limits via {@link ViewBuilder#setCardinalityLimit(int)}.
   */
  @Test
  void readerAndViewCardinalityConfiguration() {
    int counterLimit = 10;
    int generalLimit = 20;
    int counter2Limit = 30;
    int asyncCounterLimit = 40;

    // Define a cardinality selector which has one limit for counters, and another general limit for
    // other instrument kinds
    CardinalityLimitSelector cardinalityLimitSelector =
        instrumentType -> instrumentType == InstrumentType.COUNTER ? counterLimit : generalLimit;
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            // Register both the delta and cumulative reader with the customized cardinality
            // selector
            .registerMetricReader(deltaReader, cardinalityLimitSelector)
            .registerMetricReader(cumulativeReader, cardinalityLimitSelector)
            // Register a view which defines a custom cardinality limit for instrumented named
            // "counter2"
            .registerView(
                InstrumentSelector.builder().setName("counter2").build(),
                View.builder().setCardinalityLimit(counter2Limit).build())
            // Register a view which defines a custom cardinality limit for instrumented named
            // "asyncCounter"
            .registerView(
                InstrumentSelector.builder().setName("asyncCounter").build(),
                View.builder().setCardinalityLimit(asyncCounterLimit).build())
            .build();

    meter = sdkMeterProvider.get(CardinalityTest.class.getName());

    LongCounter counter1 = meter.counterBuilder("counter1").build();
    LongCounter counter2 = meter.counterBuilder("counter2").build();
    LongHistogram histogram = meter.histogramBuilder("histogram").ofLongs().build();
    meter
        .counterBuilder("asyncCounter")
        .buildWithCallback(
            observableMeasurement -> {
              // Record enough measurements to exceed cardinality threshold
              for (int i = 0; i < DEFAULT_MAX_CARDINALITY; i++) {
                observableMeasurement.record(1, Attributes.builder().put("key", i).build());
              }
            });

    // Record enough measurements to exceed cardinality threshold
    for (int i = 0; i < DEFAULT_MAX_CARDINALITY; i++) {
      counter1.add(1, Attributes.builder().put("key", i).build());
      counter2.add(1, Attributes.builder().put("key", i).build());
      histogram.record(1, Attributes.builder().put("key", i).build());
    }

    // Assert that each instrument has the appropriate number of points based on cardinality limits:
    // - counter1 should have counterLimit points
    // - counter2 should have counter2Limit points
    // - histogram should have generalLimit points
    assertThat(deltaReader.collectAllMetrics())
        .as("delta collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counterLimit,
                                            0,
                                            counterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (counterLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counter2Limit,
                                            0,
                                            counter2Limit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (counter2Limit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogramMetric ->
                            histogramMetric
                                .isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            generalLimit,
                                            0,
                                            generalLimit,
                                            HistogramPointData::getCount,
                                            DEFAULT_MAX_CARDINALITY - (generalLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("asyncCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            asyncCounterLimit,
                                            0,
                                            asyncCounterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (asyncCounterLimit - 1)))));
    assertThat(cumulativeReader.collectAllMetrics())
        .as("cumulative collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counterLimit,
                                            0,
                                            counterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (counterLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counter2Limit,
                                            0,
                                            counter2Limit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (counter2Limit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogramMetric ->
                            histogramMetric
                                .isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            generalLimit,
                                            0,
                                            generalLimit,
                                            HistogramPointData::getCount,
                                            DEFAULT_MAX_CARDINALITY - (generalLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("asyncCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            asyncCounterLimit,
                                            0,
                                            asyncCounterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (asyncCounterLimit - 1)))));

    // Record another round of measurements, again exceeding cardinality limits
    for (int i = DEFAULT_MAX_CARDINALITY; i < DEFAULT_MAX_CARDINALITY * 2; i++) {
      counter1.add(1, Attributes.builder().put("key", i).build());
      counter2.add(1, Attributes.builder().put("key", i).build());
      histogram.record(1, Attributes.builder().put("key", i).build());
    }

    // Delta reader should have new points, forgetting the points with measurements recorded prior
    // to last collection
    assertThat(deltaReader.collectAllMetrics())
        .as("delta collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counterLimit,
                                            DEFAULT_MAX_CARDINALITY,
                                            DEFAULT_MAX_CARDINALITY + counterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (counterLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counter2Limit,
                                            DEFAULT_MAX_CARDINALITY,
                                            DEFAULT_MAX_CARDINALITY + counter2Limit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (counter2Limit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogramMetric ->
                            histogramMetric
                                .isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            generalLimit,
                                            DEFAULT_MAX_CARDINALITY,
                                            DEFAULT_MAX_CARDINALITY + generalLimit,
                                            HistogramPointData::getCount,
                                            DEFAULT_MAX_CARDINALITY - (generalLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("asyncCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            asyncCounterLimit,
                                            0,
                                            asyncCounterLimit,
                                            LongPointData::getValue,
                                            0))));

    // Cumulative reader should retain old points, dropping the new measurements
    assertThat(cumulativeReader.collectAllMetrics())
        .as("cumulative collection")
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counterLimit,
                                            0,
                                            counterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY * 2 - (counterLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("counter2")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            counter2Limit,
                                            0,
                                            counter2Limit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY * 2 - (counter2Limit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("histogram")
                    .hasHistogramSatisfying(
                        histogramMetric ->
                            histogramMetric
                                .isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            generalLimit,
                                            0,
                                            generalLimit,
                                            HistogramPointData::getCount,
                                            DEFAULT_MAX_CARDINALITY * 2 - (generalLimit - 1)))),
            metricData ->
                assertThat(metricData)
                    .hasName("asyncCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .satisfies(
                                    data ->
                                        pointsAssert(
                                            data,
                                            asyncCounterLimit,
                                            0,
                                            asyncCounterLimit,
                                            LongPointData::getValue,
                                            DEFAULT_MAX_CARDINALITY - (asyncCounterLimit - 1)))));
  }

  /**
   * Helper function for {@link #readerAndViewCardinalityConfiguration()}. Asserts that the {@code
   * data} contains the {@code expectedNumPoints}, and has the attribute "key" values in the range
   * [{@code minAttributeValueInclusive}, {@code maxAttributeValueExclusive}). Additionally, asserts
   * that the {@link MetricStorage#CARDINALITY_OVERFLOW} series has a value equal of {@code
   * expectedOverflowValue}.
   */
  private static <T extends PointData> void pointsAssert(
      Data<T> data,
      int expectedNumPoints,
      int minAttributeValueInclusive,
      int maxAttributeValueExclusive,
      Function<T, Long> overflowValueProvider,
      long expectedOverflowValue) {
    assertThat(data.getPoints())
        .hasSize(expectedNumPoints)
        .allSatisfy(
            point -> {
              boolean isOverflowSeries =
                  point.getAttributes().equals(MetricStorage.CARDINALITY_OVERFLOW);
              Long keyValue = point.getAttributes().get(AttributeKey.longKey("key"));
              boolean isKeySeries = keyValue != null;
              assertThat(isOverflowSeries || isKeySeries).isTrue();
              if (isOverflowSeries) {
                assertThat(overflowValueProvider.apply(point)).isEqualTo(expectedOverflowValue);
              }
              if (isKeySeries) {
                assertThat(keyValue)
                    .isGreaterThanOrEqualTo(minAttributeValueInclusive)
                    .isLessThan(maxAttributeValueExclusive);
              }
            });
  }
}
