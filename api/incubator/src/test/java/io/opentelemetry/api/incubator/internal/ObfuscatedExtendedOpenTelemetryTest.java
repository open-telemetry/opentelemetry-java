/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.junit.jupiter.api.Test;

class ObfuscatedExtendedOpenTelemetryTest {

  @Test
  void delegatesAllCallsToDelegate() {
    ExtendedOpenTelemetry delegate = mock(ExtendedOpenTelemetry.class);

    TracerProvider tracerProvider = mock(TracerProvider.class);
    MeterProvider meterProvider = mock(MeterProvider.class);
    LoggerProvider loggerProvider = mock(LoggerProvider.class);
    ContextPropagators propagators = mock(ContextPropagators.class);
    TracerBuilder tracerBuilder = mock(TracerBuilder.class);
    ConfigProvider configProvider = mock(ConfigProvider.class);

    when(delegate.getTracerProvider()).thenReturn(tracerProvider);
    when(delegate.getMeterProvider()).thenReturn(meterProvider);
    when(delegate.getLogsBridge()).thenReturn(loggerProvider);
    when(delegate.getPropagators()).thenReturn(propagators);
    when(delegate.tracerBuilder("test-instrumentation")).thenReturn(tracerBuilder);
    when(delegate.getConfigProvider()).thenReturn(configProvider);

    ObfuscatedExtendedOpenTelemetry obfuscated = new ObfuscatedExtendedOpenTelemetry(delegate);

    assertThat(obfuscated.getTracerProvider()).isSameAs(tracerProvider);
    verify(delegate).getTracerProvider();

    assertThat(obfuscated.getMeterProvider()).isSameAs(meterProvider);
    verify(delegate).getMeterProvider();

    assertThat(obfuscated.getLogsBridge()).isSameAs(loggerProvider);
    verify(delegate).getLogsBridge();

    assertThat(obfuscated.getPropagators()).isSameAs(propagators);
    verify(delegate).getPropagators();

    assertThat(obfuscated.tracerBuilder("test-instrumentation")).isSameAs(tracerBuilder);
    verify(delegate).tracerBuilder("test-instrumentation");

    assertThat(obfuscated.getConfigProvider()).isSameAs(configProvider);
    verify(delegate).getConfigProvider();
  }
}
