/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleUpDownCounter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for SDK {@link ObservableDoubleUpDownCounter}. */
class SdkObservableDoubleUpDownCounterTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkObservableDoubleUpDownCounterTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProviderBuilder sdkMeterProviderBuilder =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE);

  @Test
  void removeCallback() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    ObservableDoubleUpDownCounter counter =
        sdkMeterProviderBuilder
            .registerMetricReader(sdkMeterReader)
            .build()
            .get(getClass().getName())
            .upDownCounterBuilder("testCounter")
            .ofDoubles()
            .buildWithCallback(measurement -> measurement.record(10));

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testCounter")
                    .hasDoubleSumSatisfying(sum -> sum.hasPointsSatisfying(point -> {})));

    counter.close();

    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void collectMetrics_NoRecords() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    sdkMeterProvider
        .get(getClass().getName())
        .upDownCounterBuilder("testObserver")
        .ofDoubles()
        .setDescription("My own DoubleUpDownSumObserver")
        .setUnit("ms")
        .buildWithCallback(result -> {});
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithOneRecord() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder.registerMetricReader(sdkMeterReader).build();
    sdkMeterProvider
        .get(getClass().getName())
        .upDownCounterBuilder("testObserver")
        .ofDoubles()
        .buildWithCallback(
            result -> result.record(12.1d, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testObserver")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(12.1)
                                            .hasAttributes(attributeEntry("k", "v")))));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testObserver")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - 2 * SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(12.1)
                                            .hasAttributes(attributeEntry("k", "v")))));
  }

  @Test
  void collectMetrics_DeltaSumAggregator() {
    InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.createDelta();
    SdkMeterProvider sdkMeterProvider =
        sdkMeterProviderBuilder
            .registerMetricReader(sdkMeterReader)
            .registerView(
                InstrumentSelector.builder()
                    .setType(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER)
                    .build(),
                View.builder().setAggregation(Aggregation.sum()).build())
            .build();
    sdkMeterProvider
        .get(getClass().getName())
        .upDownCounterBuilder("testObserver")
        .ofDoubles()
        .buildWithCallback(
            result -> result.record(12.1d, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testObserver")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(12.1)
                                            .hasAttributes(attributeEntry("k", "v")))));
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testObserver")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(0)
                                            .hasAttributes(attributeEntry("k", "v")))));
  }

  @Test
  void customAggregationThrows() {
    assertThatThrownBy(() -> View.builder().setAggregation(mock(Aggregation.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Custom Aggregation implementations are currently not supported.");
  }
}
