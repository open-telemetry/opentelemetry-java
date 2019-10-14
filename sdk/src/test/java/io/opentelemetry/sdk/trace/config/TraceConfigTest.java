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

import io.opentelemetry.sdk.trace.Samplers;
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
    assertThat(TraceConfig.getDefault().getSampler()).isEqualTo(Samplers.alwaysOn());
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributes()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfEvents()).isEqualTo(128);
    assertThat(TraceConfig.getDefault().getMaxNumberOfLinks()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(32);
  }

  @Test
  public void updateTraceConfig_NullSampler() {
    thrown.expect(NullPointerException.class);
    TraceConfig.getDefault().toBuilder().setSampler(null).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributes(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfEvents(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfLinks(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerEvent() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerEvent(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerLink() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerLink(0).build();
  }

  @Test
  public void updateTraceConfig_All() {
    TraceConfig traceConfig =
        TraceConfig.getDefault()
            .toBuilder()
            .setSampler(Samplers.alwaysOff())
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .setMaxNumberOfAttributesPerEvent(1)
            .setMaxNumberOfAttributesPerLink(2)
            .build();
    assertThat(traceConfig.getSampler()).isEqualTo(Samplers.alwaysOff());
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(11);
    assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(1);
    assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(2);
  }
}
