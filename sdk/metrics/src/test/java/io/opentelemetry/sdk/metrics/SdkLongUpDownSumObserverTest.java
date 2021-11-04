/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongUpDownSumObserverSdk}. */
class SdkLongUpDownSumObserverTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkLongUpDownSumObserverTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProviderBuilder sdkMeterProviderBuilder =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE);

  @Test
  void collectMetrics_NoRecords() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    sdkMeterProvider
        .get(getClass().getName())
        .upDownCounterBuilder("testObserver")
        .setDescription("My own LongUpDownSumObserver")
        .setUnit("ms")
        .buildWithCallback(result -> {});
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithOneRecord() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    sdkMeterProvider
        .get(getClass().getName())
        .upDownCounterBuilder("testObserver")
        .buildWithCallback(
            result -> result.observe(12, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(12)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 2 * SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(12)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_DeltaSumAggregator() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.createDelta();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(sdkMeterReader)
            .registerView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.OBSERVABLE_UP_DOWN_SUM)
                    .build(),
                View.builder().setAggregation(Aggregation.sum()).build())
            .build();
    sdkMeterProvider
        .get(getClass().getName())
        .upDownCounterBuilder("testObserver")
        .buildWithCallback(
            result -> result.observe(12, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(12)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isDelta()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(0)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
  }
}
