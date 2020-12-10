/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleSumObserverSdk}. */
class DoubleSumObserverSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(
          "io.opentelemetry.sdk.metrics.DoubleSumObserverSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  void collectMetrics_NoCallback() {
    DoubleSumObserverSdk doubleSumObserver =
        testSdk
            .doubleSumObserverBuilder("testObserver")
            .setDescription("My own DoubleSumObserver")
            .setUnit("ms")
            .build();
    assertThat(doubleSumObserver.collectAll()).isEmpty();
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleSumObserverSdk doubleSumObserver =
        testSdk
            .doubleSumObserverBuilder("testObserver")
            .setDescription("My own DoubleSumObserver")
            .setUnit("ms")
            .setCallback(result -> {})
            .build();
    assertThat(doubleSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testObserver",
                "My own DoubleSumObserver",
                "ms",
                MetricData.Type.DOUBLE_SUM,
                Collections.emptyList()));
  }

  @Test
  void collectMetrics_WithOneRecord() {
    DoubleSumObserverSdk doubleSumObserver =
        testSdk
            .doubleSumObserverBuilder("testObserver")
            .setCallback(result -> result.observe(12.1d, Labels.of("k", "v")))
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(doubleSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testObserver",
                "",
                "1",
                MetricData.Type.DOUBLE_SUM,
                Collections.singletonList(
                    DoublePoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Labels.of("k", "v"),
                        12.1d))));
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(doubleSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testObserver",
                "",
                "1",
                MetricData.Type.DOUBLE_SUM,
                Collections.singletonList(
                    DoublePoint.create(
                        testClock.now() - 2 * SECOND_NANOS,
                        testClock.now(),
                        Labels.of("k", "v"),
                        12.1d))));
  }
}
