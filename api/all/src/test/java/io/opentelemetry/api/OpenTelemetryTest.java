/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.testing.internal.AbstractOpenTelemetryTest;
import io.opentelemetry.api.trace.TracerProvider;

class OpenTelemetryTest extends AbstractOpenTelemetryTest {

  @Override
  protected TracerProvider getTracerProvider() {
    return TracerProvider.noop();
  }

  @Override
  protected MeterProvider getMeterProvider() {
    return MeterProvider.noop();
  }

  @Override
  protected LoggerProvider getLoggerProvider() {
    return LoggerProvider.noop();
  }
}
