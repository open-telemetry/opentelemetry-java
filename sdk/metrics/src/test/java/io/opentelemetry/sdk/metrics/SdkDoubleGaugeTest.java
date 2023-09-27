/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.extension.incubator.metrics.DoubleGauge;
import io.opentelemetry.extension.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.state.DefaultSynchronousMetricStorage;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkDoubleGauge}. */
class SdkDoubleGaugeTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkDoubleGaugeTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader cumulativeReader = InMemoryMetricReader.create();
  private final InMemoryMetricReader deltaReader = InMemoryMetricReader.createDelta();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .registerMetricReader(cumulativeReader)
          .registerMetricReader(deltaReader)
          .setResource(RESOURCE)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void set_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                ((ExtendedDoubleGaugeBuilder) sdkMeter.gaugeBuilder("testGauge"))
                    .build()
                    .set(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  @SuppressLogger(DefaultSynchronousMetricStorage.class)
  void set_NaN() {
    DoubleGauge gauge = ((ExtendedDoubleGaugeBuilder) sdkMeter.gaugeBuilder("testGauge")).build();
    gauge.set(Double.NaN);
    assertThat(cumulativeReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void observable_RemoveCallback() {
    ObservableDoubleGauge gauge =
        sdkMeter.gaugeBuilder("testGauge").buildWithCallback(measurement -> measurement.record(10));

    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testGauge")
                    .hasDoubleGaugeSatisfying(
                        doubleGauge -> doubleGauge.hasPointsSatisfying(point -> {})));

    gauge.close();

    assertThat(cumulativeReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  @SuppressLogger(SdkObservableMeasurement.class)
  void observable_NaN() {
    sdkMeter
        .gaugeBuilder("testGauge")
        .buildWithCallback(measurement -> measurement.record(Double.NaN));
    assertThat(cumulativeReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void collectMetrics_NoRecords() {
    ((ExtendedDoubleGaugeBuilder) sdkMeter.gaugeBuilder("testGauge")).build();
    assertThat(cumulativeReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    DoubleGauge doubleGauge =
        ((ExtendedDoubleGaugeBuilder)
                sdkMeter.gaugeBuilder("testGauge").setDescription("description").setUnit("K"))
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleGauge.set(12d, Attributes.empty());
    doubleGauge.set(13d);
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDescription("description")
                    .hasUnit("K")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(13d))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleGauge doubleGauge =
        ((ExtendedDoubleGaugeBuilder) sdkMeter.gaugeBuilder("testGauge")).build();
    doubleGauge.set(12.1d, Attributes.empty());
    doubleGauge.set(123.3d, Attributes.builder().put("K", "V").build());
    doubleGauge.set(21.4d, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleGauge.set(321.5d, Attributes.builder().put("K", "V").build());
    doubleGauge.set(111.1d, Attributes.builder().put("K", "V").build());
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDescription("")
                    .hasUnit("")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(21.4d),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(111.1d)
                                        .hasAttributes(attributeEntry("K", "V")))));
    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDescription("")
                    .hasUnit("")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(21.4d),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(111.1d)
                                        .hasAttributes(attributeEntry("K", "V")))));

    // Repeat to prove we keep previous values.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleGauge.set(222d, Attributes.builder().put("K", "V").build());
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(21.4d),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(222d)
                                        .hasAttributes(attributeEntry("K", "V")))));
    // Delta reader should only have point for {K=V} series, since the {} did not have any
    // measurements
    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime + SECOND_NANOS)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(222d)
                                        .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  void stressTest() {
    DoubleGauge doubleGauge =
        ((ExtendedDoubleGaugeBuilder) sdkMeter.gaugeBuilder("testGauge")).build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              1,
              () -> {
                doubleGauge.set(10, Attributes.builder().put("K", "V").build());
                doubleGauge.set(11, Attributes.builder().put("K", "V").build());
              }));
    }

    stressTestBuilder.build().run();
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    DoubleGauge doubleGauge =
        ((ExtendedDoubleGaugeBuilder) sdkMeter.gaugeBuilder("testGauge")).build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    IntStream.range(0, 4)
        .forEach(
            i ->
                stressTestBuilder.addOperation(
                    StressTestRunner.Operation.create(
                        2_000,
                        1,
                        () -> {
                          doubleGauge.set(10, Attributes.builder().put(keys[i], values[i]).build());
                          doubleGauge.set(11, Attributes.builder().put(keys[i], values[i]).build());
                        })));

    stressTestBuilder.build().run();
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[0], values[0])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[1], values[1])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[2], values[2])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[3], values[3])))));
  }
}
