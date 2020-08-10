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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MeterSdk}. */
class MeterSdkTest {
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.of("resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.MeterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO, new ViewRegistry());

  @Test
  void testLongCounter() {
    LongCounterSdk longCounter =
        testSdk
            .longCounterBuilder("testLongCounter")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longCounter).isNotNull();

    assertThat(
            testSdk
                .longCounterBuilder("testLongCounter")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longCounter);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.longCounterBuilder("testLongCounter").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownCounter() {
    LongUpDownCounterSdk longUpDownCounter =
        testSdk
            .longUpDownCounterBuilder("testLongUpDownCounter")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longUpDownCounter).isNotNull();

    assertThat(
            testSdk
                .longUpDownCounterBuilder("testLongUpDownCounter")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longUpDownCounter);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.longUpDownCounterBuilder("testLongUpDownCounter").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongValueRecorder() {
    LongValueRecorderSdk longValueRecorder =
        testSdk
            .longValueRecorderBuilder("testLongValueRecorder")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longValueRecorder).isNotNull();

    assertThat(
            testSdk
                .longValueRecorderBuilder("testLongValueRecorder")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longValueRecorder);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.longValueRecorderBuilder("testLongValueRecorder").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongSumObserver() {
    LongSumObserverSdk longObserver =
        testSdk
            .longSumObserverBuilder("testLongSumObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            testSdk
                .longSumObserverBuilder("testLongSumObserver")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longObserver);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.longSumObserverBuilder("testLongSumObserver").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testLongUpDownSumObserver() {
    LongUpDownSumObserverSdk longObserver =
        testSdk
            .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(longObserver).isNotNull();

    assertThat(
            testSdk
                .longUpDownSumObserverBuilder("testLongUpDownSumObserver")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(longObserver);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.longUpDownSumObserverBuilder("testLongUpDownSumObserver").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleCounter() {
    DoubleCounterSdk doubleCounter =
        testSdk
            .doubleCounterBuilder("testDoubleCounter")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleCounter).isNotNull();

    assertThat(
            testSdk
                .doubleCounterBuilder("testDoubleCounter")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleCounter);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.doubleCounterBuilder("testDoubleCounter").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleUpDownCounter() {
    DoubleUpDownCounterSdk doubleUpDownCounter =
        testSdk
            .doubleUpDownCounterBuilder("testDoubleUpDownCounter")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleUpDownCounter).isNotNull();

    assertThat(
            testSdk
                .doubleUpDownCounterBuilder("testDoubleUpDownCounter")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleUpDownCounter);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleValueRecorder() {
    DoubleValueRecorderSdk doubleValueRecorder =
        testSdk
            .doubleValueRecorderBuilder("testDoubleValueRecorder")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own ValueRecorder")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleValueRecorder).isNotNull();

    assertThat(
            testSdk
                .doubleValueRecorderBuilder("testDoubleValueRecorder")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own ValueRecorder")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleValueRecorder);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.doubleValueRecorderBuilder("testDoubleValueRecorder").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleSumObserver() {
    DoubleSumObserverSdk doubleObserver =
        testSdk
            .doubleSumObserverBuilder("testDoubleSumObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            testSdk
                .doubleSumObserverBuilder("testDoubleSumObserver")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleObserver);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.doubleSumObserverBuilder("testDoubleSumObserver").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testDoubleUpDownSumObserver() {
    DoubleUpDownSumObserverSdk doubleObserver =
        testSdk
            .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .build();
    assertThat(doubleObserver).isNotNull();

    assertThat(
            testSdk
                .doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver")
                .setConstantLabels(Labels.of("sk1", "sv1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .build())
        .isSameAs(doubleObserver);

    assertThrows(
        IllegalArgumentException.class,
        () -> testSdk.doubleUpDownSumObserverBuilder("testDoubleUpDownSumObserver").build(),
        "Instrument with same name and different descriptor already created.");
  }

  @Test
  void testBatchRecorder() {
    BatchRecorder batchRecorder = testSdk.newBatchRecorder("key", "value");
    assertThat(batchRecorder).isNotNull();
    assertThat(batchRecorder).isInstanceOf(BatchRecorderSdk.class);
  }

  @Test
  void collectAll() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testLongCounter").build();
    longCounter.add(10, Labels.empty());
    LongValueRecorderSdk longValueRecorder =
        testSdk.longValueRecorderBuilder("testLongValueRecorder").build();
    longValueRecorder.record(10, Labels.empty());
    // LongSumObserver longObserver = testSdk.longSumObserverBuilder("testLongSumObserver").build();
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testDoubleCounter").build();
    doubleCounter.add(10.1, Labels.empty());
    DoubleValueRecorderSdk doubleValueRecorder =
        testSdk.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    doubleValueRecorder.record(10.1, Labels.empty());
    // DoubleSumObserver doubleObserver =
    // testSdk.doubleSumObserverBuilder("testDoubleSumObserver").build();

    assertThat(testSdk.collectAll())
        .containsExactlyInAnyOrder(
            MetricData.create(
                Descriptor.create(
                    "testLongCounter", "", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    LongPoint.create(testClock.now(), testClock.now(), Labels.empty(), 10))),
            MetricData.create(
                Descriptor.create(
                    "testDoubleCounter", "", "1", Descriptor.Type.MONOTONIC_DOUBLE, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    DoublePoint.create(testClock.now(), testClock.now(), Labels.empty(), 10.1))),
            MetricData.create(
                Descriptor.create(
                    "testLongValueRecorder", "", "1", Descriptor.Type.SUMMARY, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    SummaryPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Labels.empty(),
                        1,
                        10,
                        Arrays.asList(
                            ValueAtPercentile.create(0, 10), ValueAtPercentile.create(100, 10))))),
            MetricData.create(
                Descriptor.create(
                    "testDoubleValueRecorder", "", "1", Descriptor.Type.SUMMARY, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.singletonList(
                    SummaryPoint.create(
                        testClock.now(),
                        testClock.now(),
                        Labels.empty(),
                        1,
                        10.1d,
                        Arrays.asList(
                            ValueAtPercentile.create(0, 10.1d),
                            ValueAtPercentile.create(100, 10.1d))))));
  }
}
