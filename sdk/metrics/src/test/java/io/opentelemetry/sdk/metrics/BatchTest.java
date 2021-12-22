/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchTest {

  private final InMemoryMetricReader reader = InMemoryMetricReader.create();

  @BeforeEach
  void setup() {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(
        OpenTelemetrySdk.builder()
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .setMinimumCollectionInterval(Duration.ZERO)
                    .registerMetricReader(reader)
                    .build())
            .build());
  }

  @AfterEach
  void cleanup() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void batch() {
    Meter meter = GlobalOpenTelemetry.get().getMeterProvider().get("meter");

    DoubleHistogram doubleHistogram = meter.histogramBuilder("double-histogram").build();
    LongHistogram longHistogram = meter.histogramBuilder("long-histogram").ofLongs().build();

    DoubleCounter doubleCounter = meter.counterBuilder("double-counter").ofDoubles().build();
    LongCounter longCounter = meter.counterBuilder("long-counter").build();

    DoubleUpDownCounter doubleUpDownCounter =
        meter.upDownCounterBuilder("double-up-down-counter").ofDoubles().build();
    LongUpDownCounter longUpDownCounter =
        meter.upDownCounterBuilder("long-up-down-counter").build();

    // Recording through existing API
    Attributes attributes = Attributes.builder().put("foo", "bar").build();
    longHistogram.record(10, attributes);
    longCounter.add(10, attributes);
    longUpDownCounter.add(10, attributes);
    doubleHistogram.record(10.0, attributes);
    doubleCounter.add(10.0, attributes);
    doubleUpDownCounter.add(10.0, attributes);

    collectAndPrint();

    // Recording through batch api
    meter
        .batch()
        .addMeasurements(10L, longHistogram, longCounter, longUpDownCounter)
        .addMeasurements(10.0, doubleHistogram, doubleCounter, doubleUpDownCounter)
        .record(Attributes.builder().put("foo", "bar").build(), Context.current());

    collectAndPrint();
  }

  @SuppressWarnings("SystemOut")
  private void collectAndPrint() {
    reader
        .collectAllMetrics()
        .forEach(
            metricData -> {
              System.out.println();
              System.out.println(metricData);
            });
  }
}
