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
import io.opentelemetry.metrics.LongGauge;
import io.opentelemetry.metrics.LongGauge.BoundLongGauge;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SdkLongCounter}. */
@RunWith(JUnit4.class)
public class SdkLongGaugeTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testLongGauge() {
    MeterSdk testSdk = new MeterSdk();
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    LongGauge longGauge =
        SdkLongGauge.Builder.builder("testGauge")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own monotonic gauge")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();

    longGauge.set(45, testSdk.createLabelSet());

    BoundLongGauge boundLongGauge = longGauge.bind(labelSet);
    boundLongGauge.set(334);
    BoundLongGauge duplicateBoundGauge = longGauge.bind(testSdk.createLabelSet("K", "v"));
    assertThat(duplicateBoundGauge).isEqualTo(boundLongGauge);

    // todo: verify that this has done something, when it has been done.
    longGauge.unbind(boundLongGauge);
  }

  @Test
  public void testLongGauge_non_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    LongGauge longGauge = SdkLongGauge.Builder.builder("testGauge").setMonotonic(false).build();
    longGauge.set(50, testSdk.createLabelSet());
    longGauge.set(10, testSdk.createLabelSet());
  }

  @Test
  public void testLongGauge_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    LongGauge longGauge = SdkLongGauge.Builder.builder("testGauge").setMonotonic(true).build();
    longGauge.set(50, testSdk.createLabelSet());
    longGauge.set(10, testSdk.createLabelSet());
  }

  @Test
  public void testBoundLongGauge_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    LongGauge longGauge = SdkLongGauge.Builder.builder("testGauge").setMonotonic(true).build();
    BoundLongGauge meter = longGauge.bind(testSdk.createLabelSet());
    meter.set(50);
    thrown.expect(IllegalArgumentException.class);
    meter.set(10);
  }

  @Test
  public void testBoundLongGauge_non_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    LongGauge longGauge = SdkLongGauge.Builder.builder("testGauge").setMonotonic(false).build();
    BoundLongGauge meter = longGauge.bind(testSdk.createLabelSet());
    meter.set(50);
    meter.set(10);
  }
}
