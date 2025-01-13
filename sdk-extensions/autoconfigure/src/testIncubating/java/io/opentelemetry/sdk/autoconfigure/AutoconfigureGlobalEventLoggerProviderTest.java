/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoconfigureGlobalEventLoggerProviderTest {

  private AutoConfiguredOpenTelemetrySdkBuilder builder;

  @BeforeEach
  void resetGlobal() {
    GlobalOpenTelemetry.resetForTest();
    GlobalEventLoggerProvider.resetForTest();
    builder =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(disableExportPropertySupplier());
  }

  @Test
  void builder_setResultAsGlobalFalse() {
    GlobalOpenTelemetry.set(OpenTelemetry.noop());

    OpenTelemetrySdk openTelemetry = builder.build().getOpenTelemetrySdk();

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isNotSameAs(openTelemetry);
    assertThat(GlobalEventLoggerProvider.get()).isNotSameAs(openTelemetry.getSdkLoggerProvider());
  }

  @Test
  void builder_setResultAsGlobalTrue() {
    OpenTelemetrySdk openTelemetry = builder.setResultAsGlobal().build().getOpenTelemetrySdk();

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(openTelemetry);
    assertThat(GlobalEventLoggerProvider.get())
        .isInstanceOf(SdkEventLoggerProvider.class)
        .extracting("delegateLoggerProvider")
        .isSameAs(openTelemetry.getSdkLoggerProvider());
  }

  private static Supplier<Map<String, String>> disableExportPropertySupplier() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.metrics.exporter", "none");
    props.put("otel.traces.exporter", "none");
    props.put("otel.logs.exporter", "none");
    return () -> props;
  }
}
