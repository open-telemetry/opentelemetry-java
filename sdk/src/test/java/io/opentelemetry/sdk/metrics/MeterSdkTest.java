/*
 * Copyright 2019, OpenTelemetry Authors
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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MeterSdk}. */
@RunWith(JUnit4.class)
public class MeterSdkTest {
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.MeterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testLongCounter() {
    LongCounterSdk longCounter =
        testSdk
            .longCounterBuilder("testLongCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(longCounter).isNotNull();

    assertThat(
            testSdk
                .longCounterBuilder("testLongCounter")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setMonotonic(true)
                .build())
        .isSameInstanceAs(longCounter);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Instrument with same name and different descriptor already created.");
    testSdk.longCounterBuilder("testLongCounter").build();
  }

  @Test
  public void testLongMeasure() {
    LongMeasureSdk longMeasure =
        testSdk
            .longMeasureBuilder("testLongMeasure")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setAbsolute(true)
            .build();
    assertThat(longMeasure).isNotNull();

    assertThat(
            testSdk
                .longMeasureBuilder("testLongMeasure")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setAbsolute(true)
                .build())
        .isSameInstanceAs(longMeasure);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Instrument with same name and different descriptor already created.");
    testSdk.longMeasureBuilder("testLongMeasure").build();
  }

  @Test
  public void testLongObserver() {
    LongObserverSdk longObserver =
        testSdk
            .longObserverBuilder("testLongObserver")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            testSdk
                .longObserverBuilder("testLongObserver")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setMonotonic(true)
                .build())
        .isSameInstanceAs(longObserver);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Instrument with same name and different descriptor already created.");
    testSdk.longObserverBuilder("testLongObserver").build();
  }

  @Test
  public void testDoubleCounter() {
    DoubleCounterSdk doubleCounter =
        testSdk
            .doubleCounterBuilder("testDoubleCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(doubleCounter).isNotNull();

    assertThat(
            testSdk
                .doubleCounterBuilder("testDoubleCounter")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setMonotonic(true)
                .build())
        .isSameInstanceAs(doubleCounter);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Instrument with same name and different descriptor already created.");
    testSdk.doubleCounterBuilder("testDoubleCounter").build();
  }

  @Test
  public void testDoubleMeasure() {
    DoubleMeasureSdk doubleMeasure =
        testSdk
            .doubleMeasureBuilder("testDoubleMeasure")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setDescription("My very own Measure")
            .setUnit("metric tonnes")
            .setAbsolute(true)
            .build();
    assertThat(doubleMeasure).isNotNull();

    assertThat(
            testSdk
                .doubleMeasureBuilder("testDoubleMeasure")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setDescription("My very own Measure")
                .setUnit("metric tonnes")
                .setAbsolute(true)
                .build())
        .isSameInstanceAs(doubleMeasure);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Instrument with same name and different descriptor already created.");
    testSdk.doubleMeasureBuilder("testDoubleMeasure").build();
  }

  @Test
  public void testDoubleObserver() {
    DoubleObserverSdk doubleObserver =
        testSdk
            .doubleObserverBuilder("testDoubleObserver")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            testSdk
                .doubleObserverBuilder("testDoubleObserver")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setMonotonic(true)
                .build())
        .isSameInstanceAs(doubleObserver);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Instrument with same name and different descriptor already created.");
    testSdk.doubleObserverBuilder("testDoubleObserver").build();
  }

  @Test
  public void testBatchRecorder() {
    BatchRecorder batchRecorder = testSdk.newBatchRecorder("key", "value");
    assertThat(batchRecorder).isNotNull();
    assertThat(batchRecorder).isInstanceOf(BatchRecorderSdk.class);
  }

  @Test
  public void collectAll() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testLongCounter").build();
    longCounter.add(10);
    LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testLongMeasure").build();
    longMeasure.record(10);
    // LongObserver longObserver = testSdk.longObserverBuilder("testLongObserver").build();
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testDoubleCounter").build();
    doubleCounter.add(10.1);
    DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testDoubleMeasure").build();
    doubleMeasure.record(10.1);
    // DoubleObserver doubleObserver = testSdk.doubleObserverBuilder("testDoubleObserver").build();

    assertThat(testSdk.collectAll())
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
                    LongPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        10))),
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
                        testClock.now(),
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        10.1))),
            MetricData.create(
                Descriptor.create(
                    "testLongMeasure",
                    "",
                    "1",
                    Type.SUMMARY,
                    Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    SummaryPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        1,
                        10,
                        Arrays.asList(
                            ValueAtPercentile.create(0, 10), ValueAtPercentile.create(100, 10))))),
            MetricData.create(
                Descriptor.create(
                    "testDoubleMeasure",
                    "",
                    "1",
                    Type.SUMMARY,
                    Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    SummaryPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        1,
                        10.1d,
                        Arrays.asList(
                            ValueAtPercentile.create(0, 10.1d),
                            ValueAtPercentile.create(100, 10.1d))))));
  }
}
