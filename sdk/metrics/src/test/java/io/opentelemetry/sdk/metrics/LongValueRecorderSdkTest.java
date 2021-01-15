/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BoundLongValueRecorder;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
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
  private final SdkMeter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void record_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.longValueRecorderBuilder("testRecorder").build().record(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.longValueRecorderBuilder("testRecorder").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    LongValueRecorderSdk longRecorder = sdkMeter.longValueRecorderBuilder("testRecorder").build();
    BoundLongValueRecorder bound = longRecorder.bind(Labels.of("key", "value"));
    try {
      assertThat(sdkMeterProvider.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    LongValueRecorderSdk longRecorder =
        sdkMeter
            .longValueRecorderBuilder("testRecorder")
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    longRecorder.record(12, Labels.empty());
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
                            Labels.empty(),
                            2,
                            24,
                            valueAtPercentiles(12, 12))))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongValueRecorderSdk longRecorder = sdkMeter.longValueRecorderBuilder("testRecorder").build();
    BoundLongValueRecorder bound = longRecorder.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      longRecorder.record(12, Labels.empty());
      bound.record(123);
      longRecorder.record(-14, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      bound.record(321);
      longRecorder.record(-121, Labels.of("K", "V"));
      assertThat(sdkMeterProvider.collectAllMetrics())
          .containsExactly(
              MetricData.createDoubleSummary(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testRecorder",
                  "",
                  "1",
                  DoubleSummaryData.create(
                      Arrays.asList(
                          DoubleSummaryPointData.create(
                              startTime,
                              testClock.now(),
                              Labels.of("K", "V"),
                              3,
                              323,
                              valueAtPercentiles(-121, 321)),
                          DoubleSummaryPointData.create(
                              startTime,
                              testClock.now(),
                              Labels.empty(),
                              2,
                              -2,
                              valueAtPercentiles(-14, 12))))));

      // Repeat to prove we don't keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      bound.record(222);
      longRecorder.record(17, Labels.empty());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .containsExactly(
              MetricData.createDoubleSummary(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testRecorder",
                  "",
                  "1",
                  DoubleSummaryData.create(
                      Arrays.asList(
                          DoubleSummaryPointData.create(
                              startTime + SECOND_NANOS,
                              testClock.now(),
                              Labels.of("K", "V"),
                              1,
                              222,
                              valueAtPercentiles(222, 222)),
                          DoubleSummaryPointData.create(
                              startTime + SECOND_NANOS,
                              testClock.now(),
                              Labels.empty(),
                              1,
                              17,
                              valueAtPercentiles(17, 17))))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final LongValueRecorderSdk longRecorder =
        sdkMeter.longValueRecorderBuilder("testRecorder").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longRecorder).setCollectionIntervalMs(100);

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
                  longRecorder.bind(Labels.of("K", "V")))));
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
                            Labels.of("K", "V"),
                            16_000,
                            160_000,
                            valueAtPercentiles(9, 11))))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final LongValueRecorderSdk longRecorder =
        sdkMeter.longValueRecorderBuilder("testRecorder").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longRecorder).setCollectionIntervalMs(100);

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
                  longRecorder.bind(Labels.of(keys[i], values[i])))));
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
                    Arrays.asList(
                        DoubleSummaryPointData.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[0], values[0]),
                            2_000,
                            20_000,
                            valueAtPercentiles(9, 11)),
                        DoubleSummaryPointData.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[1], values[1]),
                            2_000,
                            20_000,
                            valueAtPercentiles(9, 11)),
                        DoubleSummaryPointData.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[2], values[2]),
                            2_000,
                            20_000,
                            valueAtPercentiles(9, 11)),
                        DoubleSummaryPointData.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[3], values[3]),
                            2_000,
                            20_000,
                            valueAtPercentiles(9, 11))))));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundLongValueRecorder boundLongValueRecorder;

    private OperationUpdaterWithBinding(BoundLongValueRecorder boundLongValueRecorder) {
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

    private final LongValueRecorder longRecorder;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(LongValueRecorder longRecorder, String key, String value) {
      this.longRecorder = longRecorder;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longRecorder.record(11, Labels.of(key, value));
    }

    @Override
    void cleanup() {}
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
