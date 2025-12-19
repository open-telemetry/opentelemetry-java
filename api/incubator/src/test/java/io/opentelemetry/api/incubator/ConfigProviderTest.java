/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import org.junit.jupiter.api.Test;

class ConfigProviderTest {

  @Test
  void noopEquality() {
    ConfigProvider noop = ConfigProvider.noop();
    assertThat(ConfigProvider.noop()).isSameAs(noop);
  }

  @Test
  void instrumentationConfigFallback() {
    ConfigProvider configProvider = ConfigProvider.noop();
    assertThat(configProvider.getInstrumentationConfig()).isNull();
    assertThat(configProvider.getJavaInstrumentationConfig("servlet")).isNotNull();
    assertThat(configProvider.getGeneralInstrumentationConfig("http")).isNotNull();
  }
}
