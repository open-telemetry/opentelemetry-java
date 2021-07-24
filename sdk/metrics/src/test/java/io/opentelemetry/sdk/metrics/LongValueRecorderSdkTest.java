/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongHistogram;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongValueRecorderSdk}. */
class LongValueRecorderSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(LongValueRecorderSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void record_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.histogramBuilder("testRecorder").ofLongs().build().record(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.histogramBuilder("testRecorder").ofLongs().build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    LongHistogram longRecorder = sdkMeter.histogramBuilder("testRecorder").ofLongs().build();
    BoundLongHistogram bound = longRecorder.bind(Attributes.builder().put("key", "value").build());
    try {
      assertThat(sdkMeterProvider.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    LongHistogram longRecorder =
        sdkMeter
            .histogramBuilder("testRecorder")
            .ofLongs()
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longRecorder.record(12, Attributes.empty());
    longRecorder.record(12);
    assertThat(sdkMeterProvider.collectAllMetrics())
        .containsExactly(
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testRecorder",
                "description",
                "By",
                DoubleSummaryData.create(
                    Collections.singletonList(
                        DoubleSummaryPointData.create(
                            testClock.now() - SECOND_NANOS,
                            testClock.now(),
                            Attributes.empty(),
                            2,
                            24,
                            valueAtPercentiles(12, 12))))));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongHistogram longRecorder = sdkMeter.histogramBuilder("testRecorder").ofLongs().build();
    BoundLongHistogram bound = longRecorder.bind(Attributes.builder().put("K", "V").build());
    try {
      // Do some records using bounds and direct calls and bindings.
      longRecorder.record(12, Attributes.empty());
      bound.record(123);
      longRecorder.record(-14, Attributes.empty());
      // Advancing time here should not matter.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(321);
      longRecorder.record(-121, Attributes.builder().put("K", "V").build());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testRecorder")
                      .hasDoubleSummary()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point)
                                  .hasCount(3)
                                  .hasSum(323)
                                  .hasPercentileValues(
                                      valueAtPercentiles(-121, 321)
                                          .toArray(new ValueAtPercentile[0]))
                                  .hasAttributes(Attributes.builder().put("K", "V").build()),
                          point ->
                              assertThat(point)
                                  .hasCount(2)
                                  .hasSum(-2)
                                  .hasPercentileValues(
                                      valueAtPercentiles(-14, 12).toArray(new ValueAtPercentile[0]))
                                  .hasAttributes(Attributes.empty())));

      // Repeat to prove we don't keep previous values.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(222);
      longRecorder.record(17, Attributes.empty());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testRecorder")
                      .hasDoubleSummary()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime + SECOND_NANOS)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point)
                                  .hasCount(1)
                                  .hasSum(222)
                                  .hasPercentileValues(
                                      valueAtPercentiles(222, 222)
                                          .toArray(new ValueAtPercentile[0]))
                                  .hasAttributes(Attributes.builder().put("K", "V").build()),
                          point ->
                              assertThat(point)
                                  .hasCount(1)
                                  .hasSum(17)
                                  .hasPercentileValues(
                                      valueAtPercentiles(17, 17).toArray(new ValueAtPercentile[0]))
                                  .hasAttributes(Attributes.empty())));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final LongHistogram longRecorder = sdkMeter.histogramBuilder("testRecorder").ofLongs().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((LongValueRecorderSdk) longRecorder)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new LongValueRecorderSdkTest.OperationUpdaterDirectCall(longRecorder, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new LongValueRecorderSdkTest.OperationUpdaterWithBinding(
                  longRecorder.bind(Attributes.builder().put("K", "V").build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterProvider.collectAllMetrics())
        .containsExactly(
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testRecorder",
                "",
                "1",
                DoubleSummaryData.create(
                    Collections.singletonList(
                        DoubleSummaryPointData.create(
                            testClock.now(),
                            testClock.now(),
                            Attributes.builder().put("K", "V").build(),
                            16_000,
                            160_000,
                            valueAtPercentiles(9, 11))))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final LongHistogram longRecorder = sdkMeter.histogramBuilder("testRecorder").ofLongs().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((LongValueRecorderSdk) longRecorder)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new LongValueRecorderSdkTest.OperationUpdaterDirectCall(
                  longRecorder, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new LongValueRecorderSdkTest.OperationUpdaterWithBinding(
                  longRecorder.bind(Attributes.builder().put(keys[i], values[i]).build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testRecorder")
                    .hasDoubleSummary()
                    .points()
                    .allSatisfy(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasCount(2_000)
                                .hasSum(20_000)
                                .hasPercentileValues(
                                    valueAtPercentiles(9, 11).toArray(new ValueAtPercentile[0])))
                    .extracting(point -> point.getAttributes())
                    .containsExactlyInAnyOrder(
                        Attributes.of(stringKey(keys[0]), values[0]),
                        Attributes.of(stringKey(keys[1]), values[1]),
                        Attributes.of(stringKey(keys[2]), values[2]),
                        Attributes.of(stringKey(keys[3]), values[3])));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundLongHistogram boundLongValueRecorder;

    private OperationUpdaterWithBinding(BoundLongHistogram boundLongValueRecorder) {
      this.boundLongValueRecorder = boundLongValueRecorder;
    }

    @Override
    void update() {
      boundLongValueRecorder.record(9);
    }

    @Override
    void cleanup() {
      boundLongValueRecorder.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final LongHistogram longRecorder;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(LongHistogram longRecorder, String key, String value) {
      this.longRecorder = longRecorder;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longRecorder.record(11, Attributes.builder().put(key, value).build());
    }

    @Override
    void cleanup() {}
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
