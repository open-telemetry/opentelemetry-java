/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace.config;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceConfig}. */
@RunWith(JUnit4.class)
public class TraceConfigTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultTraceConfig() {
    assertThat(TraceConfig.DEFAULT.getMaxNumberOfAttributes()).isEqualTo(32);
    assertThat(TraceConfig.DEFAULT.getMaxNumberOfEvents()).isEqualTo(128);
    assertThat(TraceConfig.DEFAULT.getMaxNumberOfLinks()).isEqualTo(32);
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.DEFAULT.toBuilder().setMaxNumberOfAttributes(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.DEFAULT.toBuilder().setMaxNumberOfEvents(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.DEFAULT.toBuilder().setMaxNumberOfLinks(0).build();
  }

  @Test
  public void updateTraceConfig_All() {
    TraceConfig traceConfig =
        TraceConfig.DEFAULT
            .toBuilder()
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .build();
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(11);
  }
}
