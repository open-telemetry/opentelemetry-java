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
import io.opentelemetry.metrics.DoubleGauge;
import io.opentelemetry.metrics.DoubleGauge.BoundDoubleGauge;
import io.opentelemetry.metrics.LabelSet;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SdkLongGauge}. */
@RunWith(JUnit4.class)
public class SdkDoubleGaugeTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDoubleGauge() {
    MeterSdk testSdk = new MeterSdk();
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    DoubleGauge doubleGauge =
        SdkDoubleGauge.Builder.builder("testGauge")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own monotonic gauge")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();

    doubleGauge.set(-45.77d, testSdk.createLabelSet());

    BoundDoubleGauge boundDoubleGauge = doubleGauge.bind(labelSet);
    boundDoubleGauge.set(334.999d);
    BoundDoubleGauge duplicateBoundGauge = doubleGauge.bind(testSdk.createLabelSet("K", "v"));
    assertThat(duplicateBoundGauge).isEqualTo(boundDoubleGauge);

    // todo: verify that this has done something, when it has been done.
    doubleGauge.unbind(boundDoubleGauge);
  }

  @Test
  public void testDoubleGauge_non_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleGauge doubleGauge =
        SdkDoubleGauge.Builder.builder("testGauge").setMonotonic(false).build();
    doubleGauge.set(50.001f, testSdk.createLabelSet());
    doubleGauge.set(10.999, testSdk.createLabelSet());
  }

  @Test
  public void testDoubleGauge_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleGauge doubleGauge =
        SdkDoubleGauge.Builder.builder("testGauge").setMonotonic(true).build();
    doubleGauge.set(50.001f, testSdk.createLabelSet());
    doubleGauge.set(10.999, testSdk.createLabelSet());
  }

  @Test
  public void testBoundDoubleGauge_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleGauge doubleGauge =
        SdkDoubleGauge.Builder.builder("testGauge").setMonotonic(true).build();
    BoundDoubleGauge meter = doubleGauge.bind(testSdk.createLabelSet());
    meter.set(50.001f);
    thrown.expect(IllegalArgumentException.class);
    meter.set(10.999);
  }

  @Test
  public void testBoundDoubleGauge_non_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleGauge doubleGauge =
        SdkDoubleGauge.Builder.builder("testGauge").setMonotonic(false).build();
    BoundDoubleGauge meter = doubleGauge.bind(testSdk.createLabelSet());
    meter.set(50.001f);
    meter.set(10.999);
  }
}
