/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleValueObserverSdk}. */
class DoubleValueObserverSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.of("resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(
          "io.opentelemetry.sdk.metrics.DoubleValueObserverSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO, new ViewRegistry());

  @Test
  void collectMetrics_NoCallback() {
    DoubleValueObserverSdk doubleValueObserver =
        testSdk
            .doubleValueObserverBuilder("testObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My own DoubleValueObserver")
            .setUnit("ms")
            .build();
    assertThat(doubleValueObserver.collectAll()).isEmpty();
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleValueObserverSdk doubleValueObserver =
        testSdk
            .doubleValueObserverBuilder("testObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My own DoubleValueObserver")
            .setUnit("ms")
            .build();
    doubleValueObserver.setCallback(
        result -> {
          // Do nothing.
        });
    assertThat(doubleValueObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver",
                    "My own DoubleValueObserver",
                    "ms",
                    Descriptor.Type.SUMMARY,
                    Labels.of("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.emptyList()));
  }

  @Test
  void collectMetrics_WithOneRecord() {
    DoubleValueObserverSdk doubleValueObserver =
        testSdk.doubleValueObserverBuilder("testObserver").build();
    doubleValueObserver.setCallback(result -> result.observe(12.1d, Labels.of("k", "v")));
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(doubleValueObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create("testObserver", "", "1", Descriptor.Type.SUMMARY, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    SummaryPoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Labels.of("k", "v"),
                        1,
                        12.1d,
                        valueAtPercentiles(12.1d, 12.1d)))));
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(doubleValueObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create("testObserver", "", "1", Descriptor.Type.SUMMARY, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    SummaryPoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Labels.of("k", "v"),
                        1,
                        12.1d,
                        valueAtPercentiles(12.1d, 12.1d)))));
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
