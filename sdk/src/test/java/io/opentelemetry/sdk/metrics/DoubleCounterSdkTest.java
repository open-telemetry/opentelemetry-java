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

import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
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
  private static final Resource RESOURCE =
      Resource.create(Collections.singletonMap("resource_key", "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.DoubleCounterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void sameBound_ForSameLabelSet() {
    DoubleCounter doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();

    BoundDoubleCounter boundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    BoundDoubleCounter duplicateBoundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    try {
      assertThat(duplicateBoundCounter).isEqualTo(boundCounter);
    } finally {
      boundCounter.unbind();
      duplicateBoundCounter.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    DoubleCounterSdk doubleCounter =
        (DoubleCounterSdk) testSdk.doubleCounterBuilder("testCounter").build();

    BoundDoubleCounter boundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    try {
      assertThat(doubleCounter.collect()).isEmpty();
      BoundDoubleCounter duplicateBoundCounter =
          doubleCounter.bind(testSdk.createLabelSet("K", "v"));
      try {
        assertThat(duplicateBoundCounter).isEqualTo(boundCounter);
      } finally {
        duplicateBoundCounter.unbind();
      }
    } finally {
      boundCounter.unbind();
    }
  }

  @Test
  public void doubleCounterAdd_Monotonicity() {
    DoubleCounter doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.add(-45.77d, testSdk.createLabelSet());
  }

  @Test
  public void boundDoubleCounterAdd_Monotonicity() {
    DoubleCounter doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.bind(testSdk.createLabelSet()).add(-9.3);
  }
}
