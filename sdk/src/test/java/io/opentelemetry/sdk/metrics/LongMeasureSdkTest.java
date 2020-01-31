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
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongMeasure.BoundLongMeasure;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongMeasureSdk}. */
@RunWith(JUnit4.class)
public class LongMeasureSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private final MeterSdk testSdk =
      MeterSdkRegistry.builder().build().get("io.opentelemetry.sdk.metrics.LongMeasureSdkTest");

  @Test
  public void testLongMeasure() {
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    LongMeasure longMeasure =
        LongMeasureSdk.Builder.builder("testMeasure")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own measure")
            .setUnit("metric tonnes")
            .setAbsolute(true)
            .build();

    longMeasure.record(45, testSdk.createLabelSet());

    BoundLongMeasure boundLongMeasure = longMeasure.bind(labelSet);
    boundLongMeasure.record(334);
    BoundLongMeasure duplicateBoundMeasure = longMeasure.bind(testSdk.createLabelSet("K", "v"));
    assertThat(duplicateBoundMeasure).isEqualTo(boundLongMeasure);

    // todo: verify that this has done something, when it has been done.
    longMeasure.unbind(boundLongMeasure);
  }

  @Test
  public void testLongMeasure_absolute() {
    LongMeasure longMeasure =
        LongMeasureSdk.Builder.builder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    longMeasure.record(-45, testSdk.createLabelSet());
  }

  @Test
  public void testBoundLongMeasure_absolute() {
    LongMeasure longMeasure =
        LongMeasureSdk.Builder.builder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    longMeasure.bind(testSdk.createLabelSet()).record(-9);
  }
}
