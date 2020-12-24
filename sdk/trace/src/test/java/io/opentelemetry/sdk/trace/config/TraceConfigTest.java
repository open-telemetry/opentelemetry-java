/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.Test;

class TraceConfigTest {

  @Test
  void defaultTraceConfig() {
    assertThat(TraceConfig.getDefault().getSampler().getDescription())
        .isEqualTo(Sampler.parentBased(Sampler.alwaysOn()).getDescription());
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributes()).isEqualTo(1000);
    assertThat(TraceConfig.getDefault().getMaxNumberOfEvents()).isEqualTo(1000);
    assertThat(TraceConfig.getDefault().getMaxNumberOfLinks()).isEqualTo(1000);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(32);
  }

  @Test
  void updateTraceConfig_NullSampler() {
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().setSampler(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributes(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().setMaxNumberOfEvents(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().setMaxNumberOfLinks(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerEvent() {
    assertThatThrownBy(
            () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerEvent(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerLink() {
    assertThatThrownBy(
            () -> TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerLink(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_InvalidTraceIdRatioBased() {
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().setTraceIdRatioBased(2))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NegativeTraceIdRatioBased() {
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().setTraceIdRatioBased(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_OffTraceIdRatioBased() {
    TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setTraceIdRatioBased(0).build();
    assertThat(traceConfig.getSampler()).isSameAs(Sampler.alwaysOff());
  }

  @Test
  void updateTraceConfig_OnTraceIdRatioBased() {
    TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setTraceIdRatioBased(1).build();

    Sampler sampler = traceConfig.getSampler();
    assertThat(sampler).isEqualTo(Sampler.parentBased(Sampler.alwaysOn()));
  }

  @Test
  void updateTraceConfig_All() {
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder()
            .setSampler(Sampler.alwaysOff())
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .setMaxNumberOfAttributesPerEvent(1)
            .setMaxNumberOfAttributesPerLink(2)
            .build();
    assertThat(traceConfig.getSampler()).isEqualTo(Sampler.alwaysOff());
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(11);
    assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(1);
    assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(2);

    // Preserves values
    TraceConfig traceConfigDupe = traceConfig.toBuilder().build();
    // Use reflective comparison to catch when new fields are added.
    assertThat(traceConfigDupe).usingRecursiveComparison().isEqualTo(traceConfig);
  }
}
