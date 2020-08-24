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
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link BatchRecorderSdk}. */
class BatchRecorderSdkTest {
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.of("resource_key", AttributeValue.stringAttributeValue("resource_value")));
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
                Descriptor.create(
                    "testDoubleCounter", "", "1", Descriptor.Type.MONOTONIC_DOUBLE, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    DoublePoint.create(testClock.now(), testClock.now(), labelSet, 12.1d))));
    assertThat(longCounter.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testLongCounter", "", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), labelSet, 12))));
    assertThat(doubleUpDownCounter.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testDoubleUpDownCounter",
                    "",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_DOUBLE,
                    Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    DoublePoint.create(testClock.now(), testClock.now(), labelSet, -12.1d))));
    assertThat(longUpDownCounter.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testLongUpDownCounter",
                    "",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_LONG,
                    Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), labelSet, -12))));
  }
}
