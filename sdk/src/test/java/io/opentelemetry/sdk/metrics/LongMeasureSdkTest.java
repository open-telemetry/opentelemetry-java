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

import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongMeasure.BoundLongMeasure;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
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
  private static final Resource RESOURCE =
      Resource.create(Collections.singletonMap("resource_key", "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.LongMeasureSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void sameBound_ForSameLabelSet() {
    LongMeasure longMeasure = testSdk.longMeasureBuilder("testMeasure").build();
    BoundLongMeasure boundMeasure = longMeasure.bind(testSdk.createLabelSet("K", "v"));
    BoundLongMeasure duplicateBoundMeasure = longMeasure.bind(testSdk.createLabelSet("K", "v"));
    try {
      assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
    } finally {
      boundMeasure.unbind();
      duplicateBoundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    LongMeasureSdk longMeasure = (LongMeasureSdk) testSdk.longMeasureBuilder("testMeasure").build();
    BoundLongMeasure boundMeasure = longMeasure.bind(testSdk.createLabelSet("K", "v"));
    try {
      longMeasure.collect();
      BoundLongMeasure duplicateBoundMeasure = longMeasure.bind(testSdk.createLabelSet("K", "v"));
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
  public void longMeasureRecord_Absolute() {
    LongMeasure longMeasure = testSdk.longMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    longMeasure.record(-45, testSdk.createLabelSet());
  }

  @Test
  public void boundLongMeasure_Absolute() {
    LongMeasure longMeasure = testSdk.longMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    longMeasure.bind(testSdk.createLabelSet()).record(-9);
  }
}
