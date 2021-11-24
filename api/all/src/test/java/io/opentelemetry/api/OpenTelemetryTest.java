/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenTelemetryTest {

  @BeforeAll
  static void beforeClass() {
    GlobalOpenTelemetry.resetForTest();
  }

  @AfterEach
  void after() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void testDefault() {
    assertThat(OpenTelemetry.noop().getTracerProvider()).isSameAs(TracerProvider.noop());
    assertThat(OpenTelemetry.noop().getPropagators()).isSameAs(ContextPropagators.noop());
    assertThat(OpenTelemetry.noop().getMeterProvider()).isSameAs(MeterProvider.noop());
  }

  @Test
  void propagating() {
    ContextPropagators contextPropagators = mock(ContextPropagators.class);
    OpenTelemetry openTelemetry = OpenTelemetry.propagating(contextPropagators);

    assertThat(openTelemetry.getTracerProvider()).isSameAs(TracerProvider.noop());
    assertThat(openTelemetry.getMeterProvider()).isSameAs(MeterProvider.noop());
    assertThat(openTelemetry.getPropagators()).isSameAs(contextPropagators);
  }

  @Test
  void testGlobalBeforeSet() {
    assertThat(GlobalOpenTelemetry.getTracerProvider()).isSameAs(TracerProvider.noop());
    assertThat(GlobalOpenTelemetry.getTracerProvider())
        .isSameAs(GlobalOpenTelemetry.getTracerProvider());
    assertThat(GlobalOpenTelemetry.getPropagators()).isSameAs(GlobalOpenTelemetry.getPropagators());
  }

  @Test
  void independentNonGlobalPropagators() {
    ContextPropagators propagators1 = mock(ContextPropagators.class);
    OpenTelemetry otel1 = OpenTelemetry.propagating(propagators1);
    ContextPropagators propagators2 = mock(ContextPropagators.class);
    OpenTelemetry otel2 = OpenTelemetry.propagating(propagators2);

    assertThat(otel1.getPropagators()).isSameAs(propagators1);
    assertThat(otel2.getPropagators()).isSameAs(propagators2);
  }

  @Test
  void setThenSet() {
    setOpenTelemetry();
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(OpenTelemetry.noop()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("setOpenTelemetry");
  }

  @Test
  void getThenSet() {
    assertThat(getOpenTelemetry()).isInstanceOf(DefaultOpenTelemetry.class);
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(OpenTelemetry.noop()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("getOpenTelemetry");
  }

  private static void setOpenTelemetry() {
    GlobalOpenTelemetry.set(OpenTelemetry.noop());
  }

  private static OpenTelemetry getOpenTelemetry() {
    return GlobalOpenTelemetry.get();
  }
}
