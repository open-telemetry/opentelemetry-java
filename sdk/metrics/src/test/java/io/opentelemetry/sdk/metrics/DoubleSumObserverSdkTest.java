/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleSumObserverSdk}. */
class DoubleSumObserverSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(DoubleSumObserverSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProviderBuilder sdkMeterProviderBuilder =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE);

  @Test
  void collectMetrics_NoRecords() {
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    sdkMeterProvider
        .get(getClass().getName())
        .counterBuilder("testObserver")
        .ofDoubles()
        .setDescription("My own DoubleSumObserver")
        .setUnit("ms")
        .buildWithCallback(result -> {});
    assertThat(sdkMeterProvider.collectAllMetrics()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithOneRecord() {
    SdkMeterProvider sdkMeterProvider = sdkMeterProviderBuilder.build();
    sdkMeterProvider
        .get(getClass().getName())
        .counterBuilder("testObserver")
        .ofDoubles()
        .setDescription("My own DoubleSumObserver")
        .setUnit("ms")
        .buildWithCallback(
            result -> result.observe(12.1d, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasDescription("My own DoubleSumObserver")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isCumulative()
                    .isMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(12.1)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasDoubleSum()
                    .isCumulative()
                    .isMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 2 * SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(12.1)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_DeltaSumAggregator() {
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerView(
                InstrumentSelector.builder().setInstrumentType(InstrumentType.OBSERVABLE_SUM).build(),
                View.builder()
                    .setLabelsProcessorFactory(LabelsProcessorFactory.noop())
                    .setAggregatorFactory(AggregatorFactory.sum(AggregationTemporality.DELTA))
                    .build())
            .build();
    sdkMeterProvider
        .get(getClass().getName())
        .counterBuilder("testObserver")
        .ofDoubles()
        .setDescription("My own DoubleSumObserver")
        .setUnit("ms")
        .buildWithCallback(
            result -> result.observe(12.1d, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasDescription("My own DoubleSumObserver")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isDelta()
                    .isMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasValue(12.1)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("k", "v")));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testObserver")
                    .hasDescription("My own DoubleSumObserver")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isDelta()
                    .isMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
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
