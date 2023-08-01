/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.events.GlobalEventEmitterProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(key = "otel.traces.exporter", value = "none")
@SetSystemProperty(key = "otel.metrics.exporter", value = "none")
class AutoConfiguredOpenTelemetrySdkTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(GlobalOpenTelemetry.class);

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
    GlobalEventEmitterProvider.resetForTest();
  }

  @Test
  void initializeAndGet() {
    try (OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk()) {
      assertThat(GlobalOpenTelemetry.get())
          // ObfuscatedOpenTelemetry
          .extracting("delegate")
          .isSameAs(sdk);
    }
  }

  @Test
  void initializeAndGet_noGlobal() {
    try (OpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder().build().getOpenTelemetrySdk()) {
      assertThat(GlobalOpenTelemetry.get()).isNotSameAs(sdk);
    }
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
    OpenTelemetry openTelemetry = unobfuscate(GlobalOpenTelemetry.get());
    assertThat(openTelemetry).isInstanceOf(OpenTelemetrySdk.class);
    ((OpenTelemetrySdk) openTelemetry).close();
  }

  private static OpenTelemetry unobfuscate(OpenTelemetry openTelemetry) {
    try {
      Field delegateField =
          Class.forName("io.opentelemetry.api.GlobalOpenTelemetry$ObfuscatedOpenTelemetry")
              .getDeclaredField("delegate");
      delegateField.setAccessible(true);
      Object delegate = delegateField.get(openTelemetry);
      return (OpenTelemetry) delegate;
    } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
      throw new IllegalStateException("Error unobfuscating OpenTelemetry", e);
    }
  }
}
