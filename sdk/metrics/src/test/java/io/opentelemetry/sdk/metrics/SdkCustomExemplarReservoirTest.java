/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.metrics.internal.view.AggregationExtension;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Unit tests for using the AggregationExtension and ExemplarReservoirFactory internal APIs. */
public class SdkCustomExemplarReservoirTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private final TestClock testClock = TestClock.create();

  private Meter initialize(InMemoryMetricReader memory, ExemplarReservoirFactory reservoirFactory) {
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .setResource(RESOURCE)
            .setExemplarFilter(ExemplarFilter.alwaysOn())
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
                View.builder()
                    .setAggregation(
                        ((AggregationExtension) Aggregation.sum())
                            .setExemplarReservoirFactory(reservoirFactory))
                    .build())
            .registerMetricReader(memory)
            .build();
    return sdkMeterProvider.get(getClass().getName());
  }

  @Test
  void collectMetrics_withCustomExemplarReservoir() {
    // Create reservoir that always samples, and makes sure we get the first two samples.
    ExemplarReservoirFactory testReservor =
        ExemplarReservoirFactory.fixedSize(
            testClock,
            2,
            () ->
                new Random() {
                  @Override
                  public int nextInt(int bound) {
                    return bound - 1;
                  }
                });
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    Meter sdkMeter = initialize(sdkMeterReader, testReservor);
    DoubleCounter instrument = sdkMeter.counterBuilder("test").ofDoubles().build();
    instrument.add(10);
    instrument.add(1);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(11)
                                            // TODO - has exemplars matching reservoir behavior
                                            .hasExemplarsSatisfying(
                                                exemplar -> exemplar.hasValue(10),
                                                exemplar -> exemplar.hasValue(1)))));
  }
}
