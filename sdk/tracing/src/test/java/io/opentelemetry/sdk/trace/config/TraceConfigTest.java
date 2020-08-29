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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import org.junit.jupiter.api.Test;

class TraceConfigTest {

  @Test
  void defaultTraceConfig() {
    assertThat(TraceConfig.getDefault().getSampler().getDescription())
        .isEqualTo(Samplers.parentBased(Samplers.alwaysOn()).getDescription());
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributes()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfEvents()).isEqualTo(128);
    assertThat(TraceConfig.getDefault().getMaxNumberOfLinks()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(32);
  }

  @Test
  void updateTraceConfig_NullSampler() {
    assertThrows(
        NullPointerException.class,
        () -> TraceConfig.getDefault().toBuilder().setSampler(null).build());
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributes(0).build());
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfEvents(0).build());
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfLinks(0).build());
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerEvent() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerEvent(0).build());
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerLink() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerLink(0).build());
  }

  @Test
  void updateTraceConfig_InvalidSamplerProbability() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setSamplerProbability(2).build());
  }

  @Test
  void updateTraceConfig_NegativeSamplerProbability() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TraceConfig.getDefault().toBuilder().setSamplerProbability(-1).build());
  }

  @Test
  void updateTraceConfig_OffSamplerProbability() {
    TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSamplerProbability(0).build();
    assertThat(traceConfig.getSampler()).isSameAs(Samplers.alwaysOff());
  }

  @Test
  void updateTraceConfig_OnSamplerProbability() {
    TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSamplerProbability(1).build();

    Sampler sampler = traceConfig.getSampler();
    assertThat(sampler).isEqualTo(Samplers.parentBased(Samplers.alwaysOn()));
  }

  @Test
  void updateTraceConfig_All() {
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
