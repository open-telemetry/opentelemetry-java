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
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MeterSdkRegistry}. */
@RunWith(JUnit4.class)
public class MeterSdkRegistryTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final MeterSdkRegistry meterRegistry = MeterSdkRegistry.builder().build();

  @Test
  public void builder_HappyPath() {
    assertThat(
            MeterSdkRegistry.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .build())
        .isNotNull();
  }

  @Test
  public void builder_NullClock() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("clock");
    MeterSdkRegistry.builder().setClock(null);
  }

  @Test
  public void builder_NullResource() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("resource");
    MeterSdkRegistry.builder().setResource(null);
  }

  @Test
  public void defaultGet() {
    assertThat(meterRegistry.get("test")).isInstanceOf(MeterSdk.class);
  }

  @Test
  public void getSameInstanceForSameName_WithoutVersion() {
    assertThat(meterRegistry.get("test")).isSameInstanceAs(meterRegistry.get("test"));
    assertThat(meterRegistry.get("test")).isSameInstanceAs(meterRegistry.get("test", null));
  }

  @Test
  public void getSameInstanceForSameName_WithVersion() {
    assertThat(meterRegistry.get("test", "version"))
        .isSameInstanceAs(meterRegistry.get("test", "version"));
  }

  @Test
  public void propagatesInstrumentationLibraryInfoToTracer() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion");
    MeterSdk meter = meterRegistry.get(expected.getName(), expected.getVersion());
    assertThat(meter.getInstrumentationLibraryInfo()).isEqualTo(expected);
  }
}
