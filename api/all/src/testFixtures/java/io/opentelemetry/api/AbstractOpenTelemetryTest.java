/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit tests for {@link OpenTelemetry}. */
public abstract class AbstractOpenTelemetryTest {
  @BeforeAll
  public static void beforeClass() {
    GlobalOpenTelemetry.resetForTest();
  }

  private void setOpenTelemetry() {
    GlobalOpenTelemetry.set(getOpenTelemetry());
  }

  private static OpenTelemetry getGlobalOpenTelemetry() {
    return GlobalOpenTelemetry.get();
  }

  @AfterEach
  public void after() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void testDefault() {
    assertThat(getOpenTelemetry().getTracerProvider()).isSameAs(getTracerProvider());
    assertThat(getOpenTelemetry().getPropagators()).isSameAs(ContextPropagators.noop());
    assertThat(getOpenTelemetry().getMeterProvider()).isSameAs(getMeterProvider());
    assertThat(getOpenTelemetry().getLogsBridge()).isSameAs(getLoggerProvider());
  }

  protected abstract TracerProvider getTracerProvider();

  protected OpenTelemetry getOpenTelemetry() {
    return OpenTelemetry.noop();
  }

  protected abstract MeterProvider getMeterProvider();

  protected abstract LoggerProvider getLoggerProvider();

  @Test
  void propagating() {
    ContextPropagators contextPropagators = Mockito.mock(ContextPropagators.class);
    OpenTelemetry openTelemetry = OpenTelemetry.propagating(contextPropagators);

    assertThat(openTelemetry.getTracerProvider()).isSameAs(getTracerProvider());
    assertThat(openTelemetry.getMeterProvider()).isSameAs(getMeterProvider());
    assertThat(openTelemetry.getLogsBridge()).isSameAs(getLoggerProvider());
    assertThat(openTelemetry.getPropagators()).isSameAs(contextPropagators);
  }

  @Test
  void testGlobalBeforeSet() {
    assertThat(GlobalOpenTelemetry.getTracerProvider()).isSameAs(getTracerProvider());
    assertThat(GlobalOpenTelemetry.getTracerProvider())
        .isSameAs(GlobalOpenTelemetry.getTracerProvider());
    assertThat(GlobalOpenTelemetry.getPropagators()).isSameAs(GlobalOpenTelemetry.getPropagators());
  }

  @Test
  void independentNonGlobalPropagators() {
    ContextPropagators propagators1 = Mockito.mock(ContextPropagators.class);
    OpenTelemetry otel1 = OpenTelemetry.propagating(propagators1);
    ContextPropagators propagators2 = Mockito.mock(ContextPropagators.class);
    OpenTelemetry otel2 = OpenTelemetry.propagating(propagators2);

    assertThat(otel1.getPropagators()).isSameAs(propagators1);
    assertThat(otel2.getPropagators()).isSameAs(propagators2);
  }

  @Test
  void setThenSet() {
    setOpenTelemetry();
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(getOpenTelemetry()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("setOpenTelemetry");
  }

  @Test
  void getThenSet() {
    assertThat(getGlobalOpenTelemetry()).isInstanceOf(DefaultOpenTelemetry.class);
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(getOpenTelemetry()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("getGlobalOpenTelemetry");
  }

  @Test
  void toString_noop_Valid() {
    assertThat(getOpenTelemetry().toString())
        .isEqualTo(
            "DefaultOpenTelemetry{"
                + "propagators=DefaultContextPropagators{textMapPropagator=NoopTextMapPropagator}"
                + "}");
  }
}
