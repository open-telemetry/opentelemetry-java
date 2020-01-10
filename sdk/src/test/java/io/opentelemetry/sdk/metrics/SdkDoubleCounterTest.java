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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SdkDoubleCounter}. */
@RunWith(JUnit4.class)
public class SdkDoubleCounterTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDoubleCounter() {
    MeterSdk testSdk = new MeterSdk();
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    DoubleCounter doubleCounter =
        SdkDoubleCounter.SdkDoubleCounterBuilder.builder("testCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();

    doubleCounter.add(45.0001, testSdk.emptyLabelSet());

    BoundDoubleCounter boundDoubleCounter = doubleCounter.bind(labelSet);
    boundDoubleCounter.add(334.999d);
    BoundDoubleCounter duplicateBoundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    assertThat(duplicateBoundCounter).isEqualTo(boundDoubleCounter);

    // todo: verify that this has done something, when it has been done.
    doubleCounter.unbind(boundDoubleCounter);
  }

  @Test
  public void testDoubleCounter_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleCounter doubleCounter =
        SdkDoubleCounter.SdkDoubleCounterBuilder.builder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.add(-45.77d, testSdk.emptyLabelSet());
  }

  @Test
  public void testBoundDoubleCounter_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleCounter doubleCounter =
        SdkDoubleCounter.SdkDoubleCounterBuilder.builder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.bind(testSdk.emptyLabelSet()).add(-9.3);
  }
}
