/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

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
    assertThat(OpenTelemetry.getDefault().getTracerProvider())
        .isSameAs(TracerProvider.getDefault());
    assertThat(OpenTelemetry.getDefault().getPropagators()).isSameAs(ContextPropagators.noop());
  }

  @Test
  void propagating() {
    ContextPropagators contextPropagators = mock(ContextPropagators.class);
    OpenTelemetry openTelemetry = OpenTelemetry.getPropagating(contextPropagators);

    assertThat(openTelemetry.getTracerProvider()).isSameAs(TracerProvider.getDefault());
    assertThat(openTelemetry.getPropagators()).isSameAs(contextPropagators);
  }

  @Test
  void testGlobalBeforeSet() {
    assertThat(GlobalOpenTelemetry.getTracerProvider()).isSameAs(TracerProvider.getDefault());
    assertThat(GlobalOpenTelemetry.getTracerProvider())
        .isSameAs(GlobalOpenTelemetry.getTracerProvider());
    assertThat(GlobalOpenTelemetry.getPropagators()).isSameAs(GlobalOpenTelemetry.getPropagators());
  }

  @Test
  void independentNonGlobalPropagators() {
    ContextPropagators propagators1 = mock(ContextPropagators.class);
    OpenTelemetry otel1 = OpenTelemetry.getPropagating(propagators1);
    ContextPropagators propagators2 = mock(ContextPropagators.class);
    OpenTelemetry otel2 = OpenTelemetry.getPropagating(propagators2);

    assertThat(otel1.getPropagators()).isSameAs(propagators1);
    assertThat(otel2.getPropagators()).isSameAs(propagators2);
  }

  @Test
  void setThenSet() {
    setOpenTelemetry();
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(OpenTelemetry.getDefault()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("setOpenTelemetry");
  }

  @Test
  void getThenSet() {
    assertThat(getOpenTelemetry()).isInstanceOf(DefaultOpenTelemetry.class);
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(OpenTelemetry.getDefault()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("getOpenTelemetry");
  }

  private static void setOpenTelemetry() {
    GlobalOpenTelemetry.set(OpenTelemetry.getDefault());
  }

  private static OpenTelemetry getOpenTelemetry() {
    return GlobalOpenTelemetry.get();
  }
}
