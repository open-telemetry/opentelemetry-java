/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundLongHistogram;
import io.opentelemetry.sdk.metrics.internal.view.ExponentialHistogramAggregation;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link SdkLongHistogram}. */
class SdkLongHistogramTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkLongHistogramTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(sdkMeterReader)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(SdkLongHistogram.class);

  @Test
  void record_PreventNullAttributes() {
    assertThatThrownBy(
            () -> sdkMeter.histogramBuilder("testHistogram").ofLongs().build().record(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void bound_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                ((SdkLongHistogram) sdkMeter.histogramBuilder("testHistogram").ofLongs().build())
                    .bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    BoundLongHistogram bound =
        ((SdkLongHistogram) longHistogram).bind(Attributes.builder().put("key", "value").build());
    try {
      assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    LongHistogram longHistogram =
        sdkMeter
            .histogramBuilder("testHistogram")
            .ofLongs()
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longHistogram.record(12, Attributes.empty());
    longHistogram.record(12);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDescription("description")
                    .hasUnit("By")
                    .hasDoubleHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasCount(2)
                                .hasSum(24)
                                .hasBucketBoundaries(
                                    5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500, 5_000,
                                    7_500, 10_000)
                                .hasBucketCounts(0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    BoundLongHistogram bound =
        ((SdkLongHistogram) longHistogram).bind(Attributes.builder().put("K", "V").build());
    try {
      // Do some records using bounds and direct calls and bindings.
      longHistogram.record(9, Attributes.empty());
      bound.record(123);
      longHistogram.record(14, Attributes.empty());
      // Advancing time here should not matter.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(321);
      longHistogram.record(1, Attributes.builder().put("K", "V").build());
      assertThat(sdkMeterReader.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                      .hasName("testHistogram")
                      .hasDoubleHistogram()
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
                                  .hasSum(445)
                                  .hasBucketCounts(1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.builder().put("K", "V").build()),
                          point ->
                              assertThat(point)
                                  .hasCount(2)
                                  .hasSum(23)
                                  .hasBucketCounts(0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.empty())));

      // Histograms are cumulative by default.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.record(222);
      longHistogram.record(17, Attributes.empty());
      assertThat(sdkMeterReader.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                      .hasName("testHistogram")
                      .hasDoubleHistogram()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point)
                                  .hasCount(4)
                                  .hasSum(667)
                                  .hasBucketCounts(1, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.builder().put("K", "V").build()),
                          point ->
                              assertThat(point)
                                  .hasCount(3)
                                  .hasSum(40)
                                  .hasBucketCounts(0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                  .hasAttributes(Attributes.empty())));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_ExponentialHistogramAggregation() {
    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .setResource(RESOURCE)
            .setClock(testClock)
            .registerMetricReader(sdkMeterReader)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                View.builder()
                    .setAggregation(ExponentialHistogramAggregation.create(-1, 5))
                    .build())
            .build();
    LongHistogram longHistogram =
        sdkMeterProvider
            .get(getClass().getName())
            .histogramBuilder("testHistogram")
            .setDescription("description")
            .setUnit("ms")
            .ofLongs()
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longHistogram.record(12L, Attributes.builder().put("key", "value").build());
    longHistogram.record(12L);
    longHistogram.record(13L);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasExponentialHistogram()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point -> {
                          assertThat(point)
                              .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                              .hasEpochNanos(testClock.now())
                              .hasAttributes(Attributes.empty())
                              .hasCount(2)
                              .hasSum(25)
                              .hasScale(-1)
                              .hasZeroCount(0);
                          assertThat(point.getPositiveBuckets())
                              .hasOffset(1)
                              .hasCounts(Collections.singletonList(2L));
                          assertThat(point.getNegativeBuckets())
                              .hasOffset(0)
                              .hasCounts(Collections.emptyList());
                        },
                        point -> {
                          assertThat(point)
                              .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                              .hasEpochNanos(testClock.now())
                              .hasAttributes(Attributes.builder().put("key", "value").build())
                              .hasCount(1)
                              .hasSum(12)
                              .hasScale(-1)
                              .hasZeroCount(0);
                          assertThat(point.getPositiveBuckets())
                              .hasOffset(1)
                              .hasCounts(Collections.singletonList(1L));
                          assertThat(point.getNegativeBuckets())
                              .hasOffset(0)
                              .hasCounts(Collections.emptyList());
                        }));
  }

  @Test
  @SuppressLogger(SdkLongHistogram.class)
  void longHistogramRecord_NonNegativeCheck() {
    LongHistogram histogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    histogram.record(-45);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
  }

  @Test
  @SuppressLogger(SdkLongHistogram.class)
  void boundLongHistogramRecord_MonotonicityCheck() {
    LongHistogram histogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();
    BoundLongHistogram bound = ((SdkLongHistogram) histogram).bind(Attributes.empty());
    try {
      bound.record(-9);
      assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
      logs.assertContains(
          "Histograms can only record non-negative values. Instrument testHistogram has recorded a negative value.");
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkLongHistogram) longHistogram)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new SdkLongHistogramTest.OperationUpdaterDirectCall(longHistogram, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new SdkLongHistogramTest.OperationUpdaterWithBinding(
                  ((SdkLongHistogram) longHistogram)
                      .bind(Attributes.builder().put("K", "V").build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.of(stringKey("K"), "V"))
                                .hasCount(16_000)
                                .hasSum(160_000)));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    LongHistogram longHistogram = sdkMeter.histogramBuilder("testHistogram").ofLongs().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((SdkLongHistogram) longHistogram)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new SdkLongHistogramTest.OperationUpdaterDirectCall(
                  longHistogram, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new SdkLongHistogramTest.OperationUpdaterWithBinding(
                  ((SdkLongHistogram) longHistogram)
                      .bind(Attributes.builder().put(keys[i], values[i]).build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testHistogram")
                    .hasDoubleHistogram()
                    .points()
                    .allSatisfy(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasCount(2_000)
                                .hasSum(20_000)
                                .hasBucketCounts(0, 1000, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
                    .extracting(PointData::getAttributes)
                    .containsExactlyInAnyOrder(
                        Attributes.of(stringKey(keys[0]), values[0]),
                        Attributes.of(stringKey(keys[1]), values[1]),
                        Attributes.of(stringKey(keys[2]), values[2]),
                        Attributes.of(stringKey(keys[3]), values[3])));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundLongHistogram boundLongHistogram;

    private OperationUpdaterWithBinding(BoundLongHistogram boundLongHistogram) {
      this.boundLongHistogram = boundLongHistogram;
    }

    @Override
    void update() {
      boundLongHistogram.record(9);
    }

    @Override
    void cleanup() {
      boundLongHistogram.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final LongHistogram longHistogram;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(LongHistogram longHistogram, String key, String value) {
      this.longHistogram = longHistogram;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longHistogram.record(11, Attributes.builder().put(key, value).build());
    }

    @Override
    void cleanup() {}
  }
}
