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

class AutoConfiguredOpenTelemetrySdkTest {

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void initializeAndGet() {
    OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
    assertThat(GlobalOpenTelemetry.get())
        // ObfuscatedOpenTelemetry
        .extracting("delegate")
        .isSameAs(sdk);
  }

  @Test
  void initializeAndGet_noGlobal() {
    OpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .build()
            .getOpenTelemetrySdk();
    // TODO: calling get() will call initialize() again and autoconfigure another instance of the
    // SDK; in that case the get() method will return OpenTelemetrySdk and not
    // ObfuscatedOpenTelemetry
    assertThat(GlobalOpenTelemetry.get()).isNotSameAs(sdk);
  }
}
