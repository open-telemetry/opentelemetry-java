/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchCallbackTest {

  private InMemoryMetricReader reader;
  private SdkMeterProvider meterProvider;

  @BeforeEach
  void setup() {
    reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
  }

  @Test
  void batchCallbackDemonstration() {
    Meter meter = meterProvider.get("meter");

    ObservableLongMeasurement counter1 = meter.counterBuilder("counter1").buildObserver();
    ObservableDoubleMeasurement counter2 =
        meter.counterBuilder("counter2").ofDoubles().buildObserver();
    ObservableLongMeasurement upDownCounter1 =
        meter.upDownCounterBuilder("upDownCounter1").buildObserver();
    ObservableDoubleMeasurement upDownCounter2 =
        meter.upDownCounterBuilder("upDownCounter2").ofDoubles().buildObserver();
    ObservableLongMeasurement gauge1 = meter.gaugeBuilder("gauge1").ofLongs().buildObserver();
    ObservableDoubleMeasurement gauge2 = meter.gaugeBuilder("gauge2").buildObserver();

    AtomicLong longVal = new AtomicLong(0);
    AtomicDouble doubleVal = new AtomicDouble(0);
    BatchCallback batchCallback =
        meter
            .batchCallbackBuilder()
            .add(counter1, upDownCounter1, gauge1)
            .add(counter2, upDownCounter2, gauge2)
            .build(
                () -> {
                  long curLong = longVal.incrementAndGet();
                  double curDouble = doubleVal.addAndGet(1.1);
                  counter1.record(curLong, Attributes.builder().put("key", "counter1").build());
                  counter2.record(curDouble, Attributes.builder().put("key", "counter2").build());
                  upDownCounter1.record(
                      curLong, Attributes.builder().put("key", "upDownCounter1").build());
                  upDownCounter2.record(
                      curDouble, Attributes.builder().put("key", "upDownCounter2").build());
                  gauge1.record(curLong, Attributes.builder().put("key", "gauge1").build());
                  gauge2.record(curDouble, Attributes.builder().put("key", "gauge2").build());
                });

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("counter1")
                    .hasLongSum()
                    .isMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1)
                                .hasAttributes(
                                    Attributes.builder().put("key", "counter1").build())),
            metric ->
                assertThat(metric)
                    .hasName("counter2")
                    .hasDoubleSum()
                    .isMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1.1)
                                .hasAttributes(
                                    Attributes.builder().put("key", "counter2").build())),
            metric ->
                assertThat(metric)
                    .hasName("upDownCounter1")
                    .hasLongSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1)
                                .hasAttributes(
                                    Attributes.builder().put("key", "upDownCounter1").build())),
            metric ->
                assertThat(metric)
                    .hasName("upDownCounter2")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1.1)
                                .hasAttributes(
                                    Attributes.builder().put("key", "upDownCounter2").build())),
            metric ->
                assertThat(metric)
                    .hasName("gauge1")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1)
                                .hasAttributes(Attributes.builder().put("key", "gauge1").build())),
            metric ->
                assertThat(metric)
                    .hasName("gauge2")
                    .hasDoubleGauge()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1.1)
                                .hasAttributes(Attributes.builder().put("key", "gauge2").build())));

    batchCallback.close();
    assertThat(reader.collectAllMetrics()).isEmpty();
  }
}
