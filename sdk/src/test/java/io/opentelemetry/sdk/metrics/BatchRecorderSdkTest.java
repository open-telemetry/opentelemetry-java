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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BatchRecorderSdk}. */
@RunWith(JUnit4.class)
public class BatchRecorderSdkTest {
  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.BatchRecorderSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void batchRecorder_badLabelSet() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("key/value");
    testSdk.newBatchRecorder("key").record();
  }

  @Test
  public void batchRecorder() {
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testDoubleCounter").build();
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testLongCounter").build();
    DoubleCounterSdk doubleMeasure = testSdk.doubleCounterBuilder("testDoubleMeasure").build();
    DoubleCounterSdk longMeasure = testSdk.doubleCounterBuilder("testLongMeasure").build();
    LabelSetSdk labelSet = LabelSetSdk.create("key", "value");

    testSdk
        .newBatchRecorder("key", "value")
        .put(longCounter, 12)
        .put(doubleCounter, 12.1d)
        .put(longMeasure, 13)
        .put(doubleMeasure, 13.1d)
        .record();

    assertThat(doubleCounter.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testDoubleCounter",
                    "",
                    "1",
                    Type.MONOTONIC_DOUBLE,
                    Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    DoublePoint.create(
                        testClock.now(), testClock.now(), labelSet.getLabels(), 12.1d))));
    assertThat(longCounter.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testLongCounter",
                    "",
                    "1",
                    Type.MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), labelSet.getLabels(), 12))));
  }
}
