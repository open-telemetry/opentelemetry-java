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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.metrics.LabelSet;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DoubleCounterSdk}. */
@RunWith(JUnit4.class)
public class DoubleCounterSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private final MeterSdk testSdk =
      MeterSdkRegistry.builder().build().get("io.opentelemetry.sdk.metrics.DoubleCounterSdkTest");

  @Test
  public void testDoubleCounter() {
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    DoubleCounter doubleCounter =
        testSdk
            .doubleCounterBuilder("testCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();

    doubleCounter.add(45.0001, testSdk.createLabelSet());

    BoundDoubleCounter boundDoubleCounter = doubleCounter.bind(labelSet);
    boundDoubleCounter.add(334.999d);
    // TODO: Uncomment.
    // BoundDoubleCounter duplicateBoundCounter = doubleCounter.bind(testSdk.createLabelSet("K",
    // "v"));
    // assertThat(duplicateBoundCounter).isEqualTo(boundDoubleCounter);

    // todo: verify that this has done something, when it has been done.
    boundDoubleCounter.unbind();
  }

  @Test
  public void testDoubleCounter_monotonicity() {
    DoubleCounter doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.add(-45.77d, testSdk.createLabelSet());
  }

  @Test
  public void testBoundDoubleCounter_monotonicity() {
    DoubleCounter doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.bind(testSdk.createLabelSet()).add(-9.3);
  }
}
