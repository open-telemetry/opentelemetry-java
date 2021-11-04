/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class AsyncResetTest {

  @Test
  void demonstrateAsyncResetBug() {
    InMemoryMetricReader reader = InMemoryMetricReader.createDelta();
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build();
    sdkMeterProvider
        .get("my-meter")
        .counterBuilder("my-counter")
        .buildWithCallback(
            new Consumer<ObservableLongMeasurement>() {
              private final AtomicLong counter = new AtomicLong();

              @Override
              public void accept(ObservableLongMeasurement observableLongMeasurement) {
                observableLongMeasurement.observe(
                    10,
                    Attributes.builder().put("key", "value" + counter.incrementAndGet()).build());
              }
            });

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("my-counter")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(10)
                                .hasAttributes(Attributes.builder().put("key", "value1").build())));

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("my-counter")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(10)
                                .hasAttributes(Attributes.builder().put("key", "value2").build())));
  }
}
