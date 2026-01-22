/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UpDownCounterStaleTimeSeriesTest {

  @ParameterizedTest
  @EnumSource(AggregationTemporality.class)
  void test(AggregationTemporality aggregationTemporality) {
    InMemoryMetricReader reader =
        InMemoryMetricReader.builder()
            .setAggregationTemporalitySelector(unused -> aggregationTemporality)
            .build();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build();

    Attributes tags1 = Attributes.builder().put("key", "value1").build();
    Attributes tags2 = Attributes.builder().put("key", "value2").build();
    AtomicInteger invocationCount = new AtomicInteger(0);

    meterProvider
        .get("test")
        .upDownCounterBuilder("instrument")
        .buildWithCallback(
            observableLongMeasurement -> {
              int count = invocationCount.getAndIncrement();
              if (count == 0) {
                observableLongMeasurement.record(10L, tags1);
                observableLongMeasurement.record(10L, tags2);
              } else if (count == 1) {
                observableLongMeasurement.record(10L, tags1);
              }
            });

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("instrument")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point -> point.hasAttributes(tags1),
                                point -> point.hasAttributes(tags2))));

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("instrument")
                    .hasLongSumSatisfying(
                        sum -> sum.hasPointsSatisfying(point -> point.hasAttributes(tags1))));

    assertThat(reader.collectAllMetrics()).isEmpty();
  }
}
