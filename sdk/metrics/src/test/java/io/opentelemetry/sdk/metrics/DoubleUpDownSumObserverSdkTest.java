/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleUpDownSumObserverSdk}. */
class DoubleUpDownSumObserverSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.of("resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(
          "io.opentelemetry.sdk.metrics.DoubleUpDownSumObserverSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO, new ViewRegistry());

  @Test
  void collectMetrics_NoCallback() {
    DoubleUpDownSumObserverSdk doubleUpDownSumObserver =
        testSdk
            .doubleUpDownSumObserverBuilder("testObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own DoubleUpDownSumObserver")
            .setUnit("ms")
            .build();
    assertThat(doubleUpDownSumObserver.collectAll()).isEmpty();
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleUpDownSumObserverSdk doubleUpDownSumObserver =
        testSdk
            .doubleUpDownSumObserverBuilder("testObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My own DoubleUpDownSumObserver")
            .setUnit("ms")
            .build();
    doubleUpDownSumObserver.setCallback(
        result -> {
          // Do nothing.
        });
    assertThat(doubleUpDownSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver",
                    "My own DoubleUpDownSumObserver",
                    "ms",
                    Descriptor.Type.NON_MONOTONIC_DOUBLE,
                    Labels.of("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.emptyList()));
  }

  @Test
  void collectMetrics_WithOneRecord() {
    DoubleUpDownSumObserverSdk doubleUpDownSumObserver =
        testSdk.doubleUpDownSumObserverBuilder("testObserver").build();
    doubleUpDownSumObserver.setCallback(result -> result.observe(12.1d, Labels.of("k", "v")));
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(doubleUpDownSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver", "", "1", Descriptor.Type.NON_MONOTONIC_DOUBLE, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    DoublePoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Labels.of("k", "v"),
                        12.1d))));
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(doubleUpDownSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver", "", "1", Descriptor.Type.NON_MONOTONIC_DOUBLE, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    DoublePoint.create(
                        testClock.now() - 2 * SECOND_NANOS,
                        testClock.now(),
                        Labels.of("k", "v"),
                        12.1d))));
  }
}
