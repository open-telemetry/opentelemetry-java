/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.testing.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit tests for No-op {@link OpenTelemetry}. */
public abstract class AbstractOpenTelemetryTest {

  @BeforeAll
  public static void beforeClass() {
    GlobalOpenTelemetry.resetForTest();
  }

  private void setOpenTelemetry() {
    GlobalOpenTelemetry.set(getOpenTelemetry());
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
    assertThat(GlobalOpenTelemetry.isSet()).isFalse();
    setOpenTelemetry();
    assertThat(GlobalOpenTelemetry.isSet()).isTrue();
    assertThatThrownBy(() -> GlobalOpenTelemetry.set(getOpenTelemetry()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("setOpenTelemetry");
  }

  @Test
  void getThenSet() {
    assertThat(GlobalOpenTelemetry.isSet()).isFalse();
    assertThat(GlobalOpenTelemetry.getOrNoop()).isSameAs(OpenTelemetry.noop());

    // Calling GlobalOpenTelemetry.get() has the side affect of setting GlobalOpenTelemetry to an
    // (obfuscated) noop.
    // Call GlobalOpenTelemetry.get() using a utility method so we can later assert it was
    // responsible for setting GlobalOpenTelemetry.
    assertThat(getGlobalOpenTelemetry())
        .satisfies(
            instance ->
                assertThat(instance.getClass().getName())
                    .isEqualTo("io.opentelemetry.api.GlobalOpenTelemetry$ObfuscatedOpenTelemetry"))
        .extracting("delegate")
        .isSameAs(OpenTelemetry.noop());

    assertThat(GlobalOpenTelemetry.isSet()).isTrue();
    assertThat(GlobalOpenTelemetry.getOrNoop()).isNotSameAs(OpenTelemetry.noop());

    assertThatThrownBy(() -> GlobalOpenTelemetry.set(getOpenTelemetry()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalOpenTelemetry.set has already been called")
        .hasStackTraceContaining("getGlobalOpenTelemetry");
  }

  private static OpenTelemetry getGlobalOpenTelemetry() {
    return GlobalOpenTelemetry.get();
  }

  @Test
  void getOrNoop() {
    // Should be able to call getOrNoop multiple times without side effect and later call set.
    assertThat(GlobalOpenTelemetry.getOrNoop()).isSameAs(OpenTelemetry.noop());
    assertThat(GlobalOpenTelemetry.getOrNoop()).isSameAs(OpenTelemetry.noop());
    setOpenTelemetry();
    assertThat(GlobalOpenTelemetry.getOrNoop()).isNotSameAs(OpenTelemetry.noop());
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
