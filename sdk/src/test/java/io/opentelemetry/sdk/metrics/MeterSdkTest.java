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
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleObserver;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongObserver;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MeterSdk}. */
@RunWith(JUnit4.class)
public class MeterSdkTest {
  private final MeterSdk testSdk =
      MeterSdkProvider.builder().build().get("io.opentelemetry.sdk.metrics.MeterSdkTest");

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testLongCounter() {
    LongCounter longCounter =
        testSdk
            .longCounterBuilder("testLongCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(longCounter).isNotNull();
    assertThat(longCounter).isInstanceOf(LongCounterSdk.class);

    assertThat(
            testSdk
                .longCounterBuilder("testLongCounter")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
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
    LongMeasure longMeasure =
        testSdk
            .longMeasureBuilder("testLongMeasure")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setAbsolute(true)
            .build();
    assertThat(longMeasure).isNotNull();
    assertThat(longMeasure).isInstanceOf(LongMeasureSdk.class);

    assertThat(
            testSdk
                .longMeasureBuilder("testLongMeasure")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
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
    LongObserver longObserver =
        testSdk
            .longObserverBuilder("testLongObserver")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(longObserver).isNotNull();
    assertThat(longObserver).isInstanceOf(LongObserverSdk.class);

    assertThat(
            testSdk
                .longObserverBuilder("testLongObserver")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
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
    DoubleCounter doubleCounter =
        testSdk
            .doubleCounterBuilder("testDoubleCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(doubleCounter).isNotNull();
    assertThat(doubleCounter).isInstanceOf(DoubleCounterSdk.class);

    assertThat(
            testSdk
                .doubleCounterBuilder("testDoubleCounter")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
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
    DoubleMeasure doubleMeasure =
        testSdk
            .doubleMeasureBuilder("testDoubleMeasure")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own Measure")
            .setUnit("metric tonnes")
            .setAbsolute(true)
            .build();
    assertThat(doubleMeasure).isNotNull();
    assertThat(doubleMeasure).isInstanceOf(DoubleMeasureSdk.class);

    assertThat(
            testSdk
                .doubleMeasureBuilder("testDoubleMeasure")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
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
    DoubleObserver doubleObserver =
        testSdk
            .doubleObserverBuilder("testDoubleObserver")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(doubleObserver).isNotNull();
    assertThat(doubleObserver).isInstanceOf(DoubleObserverSdk.class);

    assertThat(
            testSdk
                .doubleObserverBuilder("testDoubleObserver")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
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
    BatchRecorder batchRecorder = testSdk.newBatchRecorder(testSdk.createLabelSet("key", "value"));
    assertThat(batchRecorder).isNotNull();
    assertThat(batchRecorder).isInstanceOf(BatchRecorderSdk.class);
  }

  @Test
  public void testLabelSets() {
    assertThat(testSdk.createLabelSet()).isSameInstanceAs(testSdk.createLabelSet());
    assertThat(testSdk.createLabelSet())
        .isSameInstanceAs(testSdk.createLabelSet(Collections.<String, String>emptyMap()));
    assertThat(testSdk.createLabelSet()).isNotNull();

    assertThat(testSdk.createLabelSet("key", "value"))
        .isEqualTo(testSdk.createLabelSet("key", "value"));

    assertThat(testSdk.createLabelSet("k1", "v1", "k2", "v2"))
        .isEqualTo(testSdk.createLabelSet("k1", "v1", "k2", "v2"));

    assertThat(testSdk.createLabelSet(Collections.singletonMap("key", "value")))
        .isEqualTo(testSdk.createLabelSet("key", "value"));

    assertThat(testSdk.createLabelSet("key", "value"))
        .isNotEqualTo(testSdk.createLabelSet("value", "key"));
  }
}
