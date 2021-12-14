/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for SDK {@link InstrumentValueType#LONG} {@link InstrumentType#OBSERVABLE_GAUGE}. */
class SdkLongGaugeBuilderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkLongGaugeBuilderTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter
        .gaugeBuilder("testObserver")
        .ofLongs()
        .setDescription("My own LongValueObserver")
        .setUnit("ms")
        .buildWithCallback(result -> {});
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithOneRecord() {
    sdkMeter
        .gaugeBuilder("testObserver")
        .ofLongs()
        .buildWithCallback(result -> result.record(12, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofSeconds(1));
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 1000000000L)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.builder().put("k", "v").build())
                                .hasValue(12)));
    exporter.reset();

    testClock.advance(Duration.ofSeconds(1));
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 2000000000L)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.builder().put("k", "v").build())
                                .hasValue(12)));
  }
}
