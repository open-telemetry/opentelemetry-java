/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import org.junit.jupiter.api.Test;

class DefaultMeterTest {
  @Test
  void expectDefaultMeter() {
    assertThat(OpenTelemetry.getMeterProvider()).isInstanceOf(DefaultMeterProvider.class);
    assertThat(OpenTelemetry.getMeter("test")).isInstanceOf(DefaultMeter.class);
    assertThat(OpenTelemetry.getMeter("test")).isSameAs(DefaultMeter.getInstance());
    assertThat(OpenTelemetry.getMeter("test", "0.1.0")).isSameAs(DefaultMeter.getInstance());
  }

  @Test
  void expectDefaultMeterProvider() {
    assertThat(OpenTelemetry.getMeterProvider()).isSameAs(DefaultMeterProvider.getInstance());
    assertThat(OpenTelemetry.getMeterProvider().get("test")).isInstanceOf(DefaultMeter.class);
    assertThat(OpenTelemetry.getMeterProvider().get("test", "0.1.0"))
        .isInstanceOf(DefaultMeter.class);
  }
}
