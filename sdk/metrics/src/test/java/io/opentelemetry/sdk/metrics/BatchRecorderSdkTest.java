/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link BatchRecorderSdk}. */
class BatchRecorderSdkTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.BatchRecorderSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO, new ViewRegistry());

  @Test
  void batchRecorder_badLabelSet() {
    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.newBatchRecorder("key").record(),
        "key/value");
  }

  @Test
  void batchRecorder() {
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testDoubleCounter").build();
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testLongCounter").build();
    DoubleUpDownCounterSdk doubleUpDownCounter =
        testSdk.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build();
    LongUpDownCounterSdk longUpDownCounter =
        testSdk.longUpDownCounterBuilder("testLongUpDownCounter").build();
    DoubleValueRecorderSdk doubleValueRecorder =
        testSdk.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    LongValueRecorderSdk longValueRecorder =
        testSdk.longValueRecorderBuilder("testLongValueRecorder").build();
    Labels labelSet = Labels.of("key", "value");

    testSdk
        .newBatchRecorder("key", "value")
        .put(longCounter, 12)
        .put(doubleUpDownCounter, -12.1d)
        .put(longUpDownCounter, -12)
        .put(doubleCounter, 12.1d)
        .put(longValueRecorder, 13)
        .put(doubleValueRecorder, 13.1d)
        .record();

    assertThat(doubleCounter.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleCounter",
                "",
                "1",
                MetricData.Type.MONOTONIC_DOUBLE,
                Collections.singletonList(
                    DoublePoint.create(testClock.now(), testClock.now(), labelSet, 12.1d))));
    assertThat(longCounter.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongCounter",
                "",
                "1",
                MetricData.Type.MONOTONIC_LONG,
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), labelSet, 12))));
    assertThat(doubleUpDownCounter.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleUpDownCounter",
                "",
                "1",
                MetricData.Type.NON_MONOTONIC_DOUBLE,
                Collections.singletonList(
                    DoublePoint.create(testClock.now(), testClock.now(), labelSet, -12.1d))));
    assertThat(longUpDownCounter.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongUpDownCounter",
                "",
                "1",
                MetricData.Type.NON_MONOTONIC_LONG,
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), labelSet, -12))));
  }
}
