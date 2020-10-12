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
    assertThat(OpenTelemetry.get().getMeterProvider()).isInstanceOf(DefaultMeterProvider.class);
    assertThat(OpenTelemetry.get().getMeter("test")).isInstanceOf(DefaultMeter.class);
    assertThat(OpenTelemetry.get().getMeter("test")).isSameAs(DefaultMeter.getInstance());
    assertThat(OpenTelemetry.get().getMeter("test", "0.1.0")).isSameAs(DefaultMeter.getInstance());
  }

  @Test
  void expectDefaultMeterProvider() {
    assertThat(OpenTelemetry.get().getMeterProvider()).isSameAs(DefaultMeterProvider.getInstance());
    assertThat(OpenTelemetry.get().getMeterProvider().get("test")).isInstanceOf(DefaultMeter.class);
    assertThat(OpenTelemetry.get().getMeterProvider().get("test", "0.1.0"))
        .isInstanceOf(DefaultMeter.class);
  }
}
