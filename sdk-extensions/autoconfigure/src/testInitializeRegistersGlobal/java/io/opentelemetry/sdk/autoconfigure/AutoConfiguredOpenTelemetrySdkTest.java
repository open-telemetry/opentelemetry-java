/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

class AutoConfiguredOpenTelemetrySdkTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(GlobalOpenTelemetry.class);

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
    GlobalLoggerProvider.resetForTest();
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

  @Test
  void globalOpenTelemetry_AutoConfigureDisabled() {
    // Autoconfigure is disabled by default and enabled via otel.java.global-autoconfigure.enabled
    assertThat(GlobalOpenTelemetry.get()).isSameAs(OpenTelemetry.noop());

    logs.assertContains(
        "AutoConfiguredOpenTelemetrySdk found on classpath but automatic configuration is disabled."
            + " To enable, run your JVM with -Dotel.java.global-autoconfigure.enabled=true");
  }

  @Test
  @SetSystemProperty(key = "otel.java.global-autoconfigure.enabled", value = "true")
  void globalOpenTelemetry_AutoConfigureEnabled() {
    assertThat(GlobalOpenTelemetry.get())
        .extracting("delegate")
        .isInstanceOf(OpenTelemetrySdk.class);

    assertThat(logs.getEvents().size()).isEqualTo(0);
  }
}
