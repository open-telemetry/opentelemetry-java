/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GlobalConfigProviderTest {

  @BeforeAll
  static void beforeClass() {
    GlobalConfigProvider.resetForTest();
  }

  @AfterEach
  void after() {
    GlobalConfigProvider.resetForTest();
  }

  @Test
  void setAndGet() {
    assertThat(GlobalConfigProvider.get()).isEqualTo(ConfigProvider.noop());
    ConfigProvider configProvider = DeclarativeConfigProperties::empty;
    GlobalConfigProvider.set(configProvider);
    assertThat(GlobalConfigProvider.get()).isSameAs(configProvider);
  }

  @Test
  void setThenSet() {
    ConfigProvider configProvider = DeclarativeConfigProperties::empty;
    GlobalConfigProvider.set(configProvider);
    assertThatThrownBy(() -> GlobalConfigProvider.set(configProvider))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalConfigProvider.set has already been called")
        .hasStackTraceContaining("setThenSet");
  }
}
