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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MeterSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testLongCounter() {
    MeterSdk testSdk = new MeterSdk();
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    LongCounter longCounter =
        testSdk
            .longCounterBuilder("testCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();

    longCounter.add(45, testSdk.emptyLabelSet());

    BoundLongCounter boundLongCounter = longCounter.bind(labelSet);
    boundLongCounter.add(334);
    BoundLongCounter duplicateBoundCounter = longCounter.bind(testSdk.createLabelSet("K", "v"));
    assertEquals(boundLongCounter, duplicateBoundCounter);

    longCounter.unbind(boundLongCounter);

    // todo: verify that the MeterSdk has kept track of what has been created, once that's in place
  }

  @Test
  public void testLongCounter_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    LongCounter longCounter = testSdk.longCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    longCounter.add(-45, testSdk.emptyLabelSet());
  }

  @Test
  public void testBoundLongCounter_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    LongCounter longCounter = testSdk.longCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    longCounter.bind(testSdk.emptyLabelSet()).add(-9);
  }

  @Test
  public void testLabelSets() {
    MeterSdk testSdk = new MeterSdk();

    assertEquals(testSdk.emptyLabelSet(), testSdk.emptyLabelSet());
    assertEquals(testSdk.createLabelSet("key", "value"), testSdk.createLabelSet("key", "value"));
    assertEquals(
        testSdk.createLabelSet("k1", "v1", "k2", "v2"),
        testSdk.createLabelSet("k1", "v1", "k2", "v2"));
    assertEquals(
        testSdk.createLabelSet("k1", "v1", "k2", "v2", "k3", "v3"),
        testSdk.createLabelSet("k1", "v1", "k2", "v2", "k3", "v3"));
    assertEquals(
        testSdk.createLabelSet("k1", "v1", "k2", "v2", "k3", "v3", "k4", "v4"),
        testSdk.createLabelSet("k1", "v1", "k2", "v2", "k3", "v3", "k4", "v4"));
  }
}
