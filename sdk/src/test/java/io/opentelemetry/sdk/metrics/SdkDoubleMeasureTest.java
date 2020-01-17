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
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleMeasure.BoundDoubleMeasure;
import io.opentelemetry.metrics.LabelSet;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SdkDoubleMeasure}. */
@RunWith(JUnit4.class)
public class SdkDoubleMeasureTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDoubleMeasure() {
    MeterSdk testSdk = new MeterSdk();
    LabelSet labelSet = testSdk.createLabelSet("K", "v");

    DoubleMeasure doubleMeasure =
        SdkDoubleMeasure.Builder.builder("testMeasure")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own double measure")
            .setUnit("metric tonnes")
            .setAbsolute(true)
            .build();

    doubleMeasure.record(45.0001, testSdk.createLabelSet());

    BoundDoubleMeasure boundDoubleMeasure = doubleMeasure.bind(labelSet);
    boundDoubleMeasure.record(334.999d);
    BoundDoubleMeasure duplicateBoundMeasure = doubleMeasure.bind(testSdk.createLabelSet("K", "v"));
    assertThat(duplicateBoundMeasure).isEqualTo(boundDoubleMeasure);

    // todo: verify that this has done something, when it has been done.
    doubleMeasure.unbind(boundDoubleMeasure);
  }

  @Test
  public void testDoubleMeasure_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleMeasure doubleMeasure =
        SdkDoubleMeasure.Builder.builder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleMeasure.record(-45.77d, testSdk.createLabelSet());
  }

  @Test
  public void testBoundDoubleMeasure_monotonicity() {
    MeterSdk testSdk = new MeterSdk();

    DoubleMeasure doubleMeasure =
        SdkDoubleMeasure.Builder.builder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleMeasure.bind(testSdk.createLabelSet()).record(-9.3f);
  }
}
