/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundDoubleUpDownCounter;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for SDK {@link InstrumentValueType#DOUBLE} {@link InstrumentType#UP_DOWN_COUNTER}. */
class SdkDoubleUpDownCounterTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(SdkDoubleUpDownCounterTest.class.getName(), null);
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
  void add_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testUpDownCounter")
                    .ofDoubles()
                    .build()
                    .add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void bound_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                ((SdkDoubleUpDownCounter)
                        sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build())
                    .bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build();
    BoundDoubleUpDownCounter bound =
        ((SdkDoubleUpDownCounter) doubleUpDownCounter)
            .bind(Attributes.builder().put("foo", "bar").build());
    try {
      sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.getFinishedMetricItems()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter
            .upDownCounterBuilder("testUpDownCounter")
            .ofDoubles()
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleUpDownCounter.add(12d, Attributes.empty());
    doubleUpDownCounter.add(12d);
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testUpDownCounter")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(24)));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build();
    BoundDoubleUpDownCounter bound =
        ((SdkDoubleUpDownCounter) doubleUpDownCounter)
            .bind(Attributes.builder().put("K", "V").build());
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleUpDownCounter.add(12.1d, Attributes.empty());
      bound.add(123.3d);
      doubleUpDownCounter.add(21.4d, Attributes.empty());
      // Advancing time here should not matter.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.add(321.5d);
      doubleUpDownCounter.add(111.1d, Attributes.builder().put("K", "V").build());
      sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.getFinishedMetricItems())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testUpDownCounter")
                      .hasDoubleSum()
                      .isNotMonotonic()
                      .isCumulative()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point).hasAttributes(Attributes.empty()).hasValue(33.5),
                          point ->
                              assertThat(point)
                                  .hasValue(555.9)
                                  .hasAttributes(Attributes.of(stringKey("K"), "V"))));
      exporter.reset();

      // Repeat to prove we keep previous values.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.add(222d);
      doubleUpDownCounter.add(11d, Attributes.empty());
      sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.getFinishedMetricItems())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testUpDownCounter")
                      .hasDoubleSum()
                      .isNotMonotonic()
                      .isCumulative()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point).hasAttributes(Attributes.empty()).hasValue(44.5),
                          point ->
                              assertThat(point)
                                  .hasAttributes(Attributes.of(stringKey("K"), "V"))
                                  .hasValue(777.9)));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkDoubleUpDownCounter) doubleUpDownCounter)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(doubleUpDownCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(
                  ((SdkDoubleUpDownCounter) doubleUpDownCounter)
                      .bind(Attributes.builder().put("K", "V").build()))));
    }

    stressTestBuilder.build().run();
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testUpDownCounter")
                    .hasDoubleSum()
                    .isCumulative()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasValue(80_000)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("K", "V")));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkDoubleUpDownCounter) doubleUpDownCounter)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(doubleUpDownCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(
                  ((SdkDoubleUpDownCounter) doubleUpDownCounter)
                      .bind(Attributes.builder().put(keys[i], values[i]).build()))));
    }

    stressTestBuilder.build().run();
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testUpDownCounter")
                    .hasDoubleSum()
                    .isCumulative()
                    .isNotMonotonic()
                    .points()
                    .allSatisfy(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasValue(40_000))
                    .extracting(PointData::getAttributes)
                    .containsExactlyInAnyOrder(
                        Attributes.of(stringKey(keys[0]), values[0]),
                        Attributes.of(stringKey(keys[1]), values[1]),
                        Attributes.of(stringKey(keys[2]), values[2]),
                        Attributes.of(stringKey(keys[3]), values[3])));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundDoubleUpDownCounter boundDoubleUpDownCounter;

    private OperationUpdaterWithBinding(BoundDoubleUpDownCounter boundDoubleUpDownCounter) {
      this.boundDoubleUpDownCounter = boundDoubleUpDownCounter;
    }

    @Override
    void update() {
      boundDoubleUpDownCounter.add(9.0);
    }

    @Override
    void cleanup() {
      boundDoubleUpDownCounter.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final DoubleUpDownCounter doubleUpDownCounter;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(
        DoubleUpDownCounter doubleUpDownCounter, String key, String value) {
      this.doubleUpDownCounter = doubleUpDownCounter;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleUpDownCounter.add(11.0, Attributes.builder().put(key, value).build());
    }

    @Override
    void cleanup() {}
  }
}
