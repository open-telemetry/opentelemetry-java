/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.logs.ExtendedDefaultLoggerProvider;
import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.incubator.metrics.ExtendedDefaultMeterProvider;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.incubator.trace.ExtendedDefaultTracerProvider;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.testing.internal.AbstractOpenTelemetryTest;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExtendedOpenTelemetryTest extends AbstractOpenTelemetryTest {

  @Override
  protected TracerProvider getTracerProvider() {
    return ExtendedDefaultTracerProvider.getNoop();
  }

  @Override
  protected MeterProvider getMeterProvider() {
    return ExtendedDefaultMeterProvider.getNoop();
  }

  @Override
  protected LoggerProvider getLoggerProvider() {
    return ExtendedDefaultLoggerProvider.getNoop();
  }

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void incubatingApiIsLoaded() {
    assertIsExtended(OpenTelemetry.noop());
    assertIsExtended(OpenTelemetry.propagating(ContextPropagators.noop()));
  }

  @Test
  void globalOpenTelemetry() {
    GlobalOpenTelemetry.set(
        ExtendedOpenTelemetrySdk.create(
            OpenTelemetrySdk.builder().build(),
            SdkConfigProvider.create(new OpenTelemetryConfigurationModel())));
    assertThat(GlobalOpenTelemetry.get()).isInstanceOf(ExtendedOpenTelemetry.class);
  }

  private static void assertIsExtended(OpenTelemetry openTelemetry) {
    assertThat(openTelemetry.getMeter("test").counterBuilder("test"))
        .isInstanceOf(ExtendedLongCounterBuilder.class);
    assertThat(openTelemetry.getLogsBridge().get("test")).isInstanceOf(ExtendedLogger.class);
    assertThat(openTelemetry.getTracer("test")).isInstanceOf(ExtendedTracer.class);
  }
}
