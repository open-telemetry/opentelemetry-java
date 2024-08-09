/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.AbstractOpenTelemetryTest;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.logs.ExtendedDefaultLoggerProvider;
import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.incubator.metrics.ExtendedDefaultMeterProvider;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.incubator.trace.ExtendedDefaultTracerProvider;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import org.junit.jupiter.api.Test;

class ExtendedOpenTelemetryTest extends AbstractOpenTelemetryTest {

  @Override
  protected TracerProvider getTracerProvider() {
    return ExtendedDefaultTracerProvider.getNoop();
  }

  @Override
  protected OpenTelemetry getOpenTelemetry() {
    return ExtendedDefaultOpenTelemetry.getNoop();
  }

  @Override
  protected MeterProvider getMeterProvider() {
    return ExtendedDefaultMeterProvider.getNoop();
  }

  @Override
  protected LoggerProvider getLoggerProvider() {
    return ExtendedDefaultLoggerProvider.getNoop();
  }

  @Test
  void incubatingApiIsLoaded() {
    assertThat(OpenTelemetry.noop().getMeter("test").counterBuilder("test"))
        .isInstanceOf(ExtendedLongCounterBuilder.class);
    assertThat(OpenTelemetry.noop().getLogsBridge().get("test")).isInstanceOf(ExtendedLogger.class);
    assertThat(OpenTelemetry.noop().getTracer("test")).isInstanceOf(ExtendedTracer.class);
  }
}
