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

import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleMeasure.BoundDoubleMeasure;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.AttributeValue;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DoubleMeasureSdk}. */
@RunWith(JUnit4.class)
public class DoubleMeasureSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.DoubleMeasureSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void sameBound_ForSameLabelSet() {
    DoubleMeasure doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();
    BoundDoubleMeasure boundMeasure = doubleMeasure.bind(testSdk.createLabelSet("K", "v"));
    BoundDoubleMeasure duplicateBoundMeasure = doubleMeasure.bind(testSdk.createLabelSet("K", "v"));
    try {
      assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
    } finally {
      boundMeasure.unbind();
      duplicateBoundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();
    BoundDoubleMeasure boundMeasure = doubleMeasure.bind(testSdk.createLabelSet("K", "v"));
    try {
      doubleMeasure.collectAll();
      BoundDoubleMeasure duplicateBoundMeasure =
          doubleMeasure.bind(testSdk.createLabelSet("K", "v"));
      try {
        assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
      } finally {
        duplicateBoundMeasure.unbind();
      }
    } finally {
      boundMeasure.unbind();
    }
  }

  @Test
  public void doubleMeasureRecord_Absolute() {
    DoubleMeasure doubleMeasure =
        testSdk.doubleMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleMeasure.record(-45.77d, testSdk.createLabelSet());
  }

  @Test
  public void boundDoubleMeasureRecord_Absolute() {
    DoubleMeasure doubleMeasure =
        testSdk.doubleMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleMeasure.bind(testSdk.createLabelSet()).record(-9.3f);
  }
}
