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
import io.opentelemetry.metrics.AsynchronousInstrument.Callback;
import io.opentelemetry.metrics.AsynchronousInstrument.LongResult;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongSumObserverSdk}. */
@RunWith(JUnit4.class)
public class LongSumObserverSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(
          "io.opentelemetry.sdk.metrics.LongSumObserverSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void collectMetrics_NoCallback() {
    LongSumObserverSdk longSumObserver =
        testSdk
            .longSumObserverBuilder("testObserver")
            .setConstantLabels(Collections.singletonMap("sk1", "sv1"))
            .setDescription("My own LongSumObserver")
            .setUnit("ms")
            .build();
    assertThat(longSumObserver.collectAll()).isEmpty();
  }

  @Test
  public void collectMetrics_NoRecords() {
    LongSumObserverSdk longSumObserver =
        testSdk
            .longSumObserverBuilder("testObserver")
            .setConstantLabels(Collections.singletonMap("sk1", "sv1"))
            .setDescription("My own LongSumObserver")
            .setUnit("ms")
            .build();
    longSumObserver.setCallback(
        new Callback<LongResult>() {
          @Override
          public void update(LongResult result) {
            // Do nothing.
          }
        });
    assertThat(longSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver",
                    "My own LongSumObserver",
                    "ms",
                    Type.MONOTONIC_LONG,
                    Collections.singletonMap("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>emptyList()));
  }

  @Test
  public void collectMetrics_WithOneRecord() {
    LongSumObserverSdk longSumObserver = testSdk.longSumObserverBuilder("testObserver").build();
    longSumObserver.setCallback(
        new Callback<LongResult>() {
          @Override
          public void update(LongResult result) {
            result.observe(12, "k", "v");
          }
        });
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(longSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver",
                    "",
                    "1",
                    Type.MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    LongPoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Collections.singletonMap("k", "v"),
                        12))));
    testClock.advanceNanos(SECOND_NANOS);
    assertThat(longSumObserver.collectAll())
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testObserver",
                    "",
                    "1",
                    Type.MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    LongPoint.create(
                        testClock.now() - 2 * SECOND_NANOS,
                        testClock.now(),
                        Collections.singletonMap("k", "v"),
                        12))));
  }
}
