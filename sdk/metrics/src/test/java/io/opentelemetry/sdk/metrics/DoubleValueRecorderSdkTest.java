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
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.DoubleValueRecorder.BoundDoubleValueRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleValueRecorderSdk}. */
class DoubleValueRecorderSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(DoubleValueRecorderSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final SdkMeter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void record_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.doubleValueRecorderBuilder("testRecorder").build().record(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.doubleValueRecorderBuilder("testRecorder").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleValueRecorderSdk doubleRecorder =
        sdkMeter.doubleValueRecorderBuilder("testRecorder").build();
    BoundDoubleValueRecorder bound = doubleRecorder.bind(Labels.of("key", "value"));
    try {
      assertThat(sdkMeterProvider.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    DoubleValueRecorderSdk doubleRecorder =
        sdkMeter
            .doubleValueRecorderBuilder("testRecorder")
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    doubleRecorder.record(12d, Labels.empty());
    doubleRecorder.record(12d);
    assertThat(sdkMeterProvider.collectAllMetrics())
        .containsExactly(
            MetricData.createDoubleSummary(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testRecorder",
                "description",
                "ms",
                MetricData.DoubleSummaryData.create(
                    Collections.singletonList(
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now() - SECOND_NANOS,
                            testClock.now(),
                            Labels.empty(),
                            2,
                            24d,
                            valueAtPercentiles(12d, 12d))))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleValueRecorderSdk doubleRecorder =
        sdkMeter.doubleValueRecorderBuilder("testRecorder").build();
    BoundDoubleValueRecorder bound = doubleRecorder.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleRecorder.record(12.1d, Labels.empty());
      bound.record(123.3d);
      doubleRecorder.record(-13.1d, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      bound.record(321.5d);
      doubleRecorder.record(-121.5d, Labels.of("K", "V"));
      assertThat(sdkMeterProvider.collectAllMetrics())
          .containsExactly(
              MetricData.createDoubleSummary(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testRecorder",
                  "",
                  "1",
                  MetricData.DoubleSummaryData.create(
                      Arrays.asList(
                          MetricData.DoubleSummaryPoint.create(
                              startTime,
                              testClock.now(),
                              Labels.of("K", "V"),
                              3,
                              323.3d,
                              valueAtPercentiles(-121.5d, 321.5d)),
                          MetricData.DoubleSummaryPoint.create(
                              startTime,
                              testClock.now(),
                              Labels.empty(),
                              2,
                              -1.0d,
                              valueAtPercentiles(-13.1d, 12.1d))))));

      // Repeat to prove we don't keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      bound.record(222d);
      doubleRecorder.record(17d, Labels.empty());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .containsExactly(
              MetricData.createDoubleSummary(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testRecorder",
                  "",
                  "1",
                  MetricData.DoubleSummaryData.create(
                      Arrays.asList(
                          MetricData.DoubleSummaryPoint.create(
                              startTime + SECOND_NANOS,
                              testClock.now(),
                              Labels.of("K", "V"),
                              1,
                              222.0d,
                              valueAtPercentiles(222.0, 222.0d)),
                          MetricData.DoubleSummaryPoint.create(
                              startTime + SECOND_NANOS,
                              testClock.now(),
                              Labels.empty(),
                              1,
                              17.0d,
                              valueAtPercentiles(17d, 17d))))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final DoubleValueRecorderSdk doubleRecorder =
        sdkMeter.doubleValueRecorderBuilder("testRecorder").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleRecorder).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new DoubleValueRecorderSdkTest.OperationUpdaterDirectCall(doubleRecorder, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterWithBinding(doubleRecorder.bind(Labels.of("K", "V")))));
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
                MetricData.DoubleSummaryData.create(
                    Collections.singletonList(
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of("K", "V"),
                            8_000,
                            80_000,
                            valueAtPercentiles(9.0, 11.0))))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleValueRecorderSdk doubleRecorder =
        sdkMeter.doubleValueRecorderBuilder("testRecorder").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleRecorder).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new DoubleValueRecorderSdkTest.OperationUpdaterDirectCall(
                  doubleRecorder, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(doubleRecorder.bind(Labels.of(keys[i], values[i])))));
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
                MetricData.DoubleSummaryData.create(
                    Arrays.asList(
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[0], values[0]),
                            4_000,
                            40_000d,
                            valueAtPercentiles(9.0, 11.0)),
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[1], values[1]),
                            4_000,
                            40_000d,
                            valueAtPercentiles(9.0, 11.0)),
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[2], values[2]),
                            4_000,
                            40_000d,
                            valueAtPercentiles(9.0, 11.0)),
                        MetricData.DoubleSummaryPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[3], values[3]),
                            4_000,
                            40_000d,
                            valueAtPercentiles(9.0, 11.0))))));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundDoubleValueRecorder boundDoubleValueRecorder;

    private OperationUpdaterWithBinding(BoundDoubleValueRecorder boundDoubleValueRecorder) {
      this.boundDoubleValueRecorder = boundDoubleValueRecorder;
    }

    @Override
    void update() {
      boundDoubleValueRecorder.record(11.0);
    }

    @Override
    void cleanup() {
      boundDoubleValueRecorder.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {
    private final DoubleValueRecorder doubleValueRecorder;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(
        DoubleValueRecorder doubleValueRecorder, String key, String value) {
      this.doubleValueRecorder = doubleValueRecorder;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleValueRecorder.record(9.0, Labels.of(key, value));
    }

    @Override
    void cleanup() {}
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
