/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkAutoConfigurationTest {

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void initializeAndGet() {
    OpenTelemetrySdk sdk = OpenTelemetrySdkAutoConfiguration.initialize();
    assertThat(GlobalOpenTelemetry.get()).isSameAs(sdk);
  }

  @Test
  void initializeAndGet_noGlobal() {
    OpenTelemetrySdkAutoConfiguration.initialize(false);
    assertThat(GlobalOpenTelemetry.get()).isNull();
  }
}
