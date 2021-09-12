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
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongValueObserverSdk}. */
class SdkLongGaugeBuilderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkLongGaugeBuilderTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = new InMemoryMetricReader();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .register(sdkMeterReader)
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
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithOneRecord() {
    sdkMeter
        .gaugeBuilder("testObserver")
        .ofLongs()
        .buildWithCallback(
            result -> result.observe(12, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofSeconds(1));
    assertThat(sdkMeterReader.collectAllMetrics())
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
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.builder().put("k", "v").build())
                                .hasValue(12)));
    testClock.advance(Duration.ofSeconds(1));
    assertThat(sdkMeterReader.collectAllMetrics())
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
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.builder().put("k", "v").build())
                                .hasValue(12)));
  }
}
