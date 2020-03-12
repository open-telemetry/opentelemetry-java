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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.metrics.LongObserver.ResultLongObserver;
import io.opentelemetry.metrics.Observer.Callback;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongObserverSdk}. */
@RunWith(JUnit4.class)
public class LongObserverSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.LongObserverSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void observeMonotonic_NegativeValue() {
    LongObserverSdk longObserver =
        testSdk.longObserverBuilder("testObserver").setMonotonic(true).build();

    longObserver.setCallback(
        new Callback<ResultLongObserver>() {
          @Override
          public void update(ResultLongObserver result) {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("monotonic observers can only record positive values");
            result.observe(-45, testSdk.createLabelSet());
          }
        });
    longObserver.collectAll();
  }
}
