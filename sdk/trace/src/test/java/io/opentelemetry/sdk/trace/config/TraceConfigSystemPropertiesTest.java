/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TraceConfigSystemPropertiesTest {

  @AfterEach
  void tearDown() {
    System.clearProperty("otel.config.sampler.probability");
    System.clearProperty("otel.span.attribute.count.limit");
    System.clearProperty("otel.span.event.count.limit");
    System.clearProperty("otel.span.link.count.limit");
    System.clearProperty("otel.config.max.event.attrs");
    System.clearProperty("otel.config.max.link.attrs");
  }

  @Test
  void updateTraceConfig_SystemProperties() {
    System.setProperty("otel.config.sampler.probability", "0.3");
    System.setProperty("otel.span.attribute.count.limit", "5");
    System.setProperty("otel.span.event.count.limit", "6");
    System.setProperty("otel.span.link.count.limit", "9");
    System.setProperty("otel.config.max.event.attrs", "7");
    System.setProperty("otel.config.max.link.attrs", "11");
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder()
            .readEnvironmentVariables()
            .readSystemProperties()
            .build();
    // this is not a useful assertion. How can we do better?
    assertThat(traceConfig.getSampler())
        .isEqualTo(Sampler.parentBased(Sampler.traceIdRatioBased(0.3)));
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(5);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(6);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(9);
    assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(7);
    assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(11);
  }

  @Test
  void updateTraceConfig_InvalidSamplerProbability() {
    System.setProperty("otel.config.sampler.probability", "-1");
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().readSystemProperties().build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
    System.setProperty("otel.span.attribute.count.limit", "-5");
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().readSystemProperties().build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
    System.setProperty("otel.span.event.count.limit", "-6");
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().readSystemProperties().build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
    System.setProperty("otel.span.link.count.limit", "-9");
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().readSystemProperties().build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerEvent() {
    System.setProperty("otel.config.max.event.attrs", "-7");
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().readSystemProperties().build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerLink() {
    System.setProperty("otel.config.max.link.attrs", "-10");
    assertThatThrownBy(() -> TraceConfig.getDefault().toBuilder().readSystemProperties().build())
        .isInstanceOf(IllegalArgumentException.class);
  }
}
