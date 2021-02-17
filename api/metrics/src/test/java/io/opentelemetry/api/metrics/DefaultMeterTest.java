/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultMeterTest {
  @Test
  void expectDefaultMeter() {
    assertThat(MeterProvider.noop().get("test")).isInstanceOf(DefaultMeter.class);
    assertThat(MeterProvider.noop().get("test")).isSameAs(DefaultMeter.getInstance());
    assertThat(MeterProvider.noop().get("test", "0.1.0")).isSameAs(DefaultMeter.getInstance());
  }
}
