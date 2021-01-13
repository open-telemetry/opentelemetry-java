/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Remove after deleting OpenTelemetry SPI
class OpenTelemetryTest {

  @BeforeAll
  static void beforeClass() {
    GlobalOpenTelemetry.reset();
  }

  @AfterEach
  void after() {
    GlobalOpenTelemetry.reset();
  }

  @Test
  void testDefault() {
    assertThat(GlobalOpenTelemetry.getTracerProvider().getClass().getSimpleName())
        .isEqualTo("DefaultTracerProvider");
    assertThat(GlobalOpenTelemetry.getTracerProvider())
        .isSameAs(GlobalOpenTelemetry.getTracerProvider());
    assertThat(GlobalOpenTelemetry.getPropagators()).isSameAs(GlobalOpenTelemetry.getPropagators());
  }

  @Test
  void builder() {
    TracerProvider tracerProvider = mock(TracerProvider.class);
    ContextPropagators contextPropagators = mock(ContextPropagators.class);
    OpenTelemetry openTelemetry =
        DefaultOpenTelemetry.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(contextPropagators)
            .build();

    assertThat(openTelemetry).isNotNull();
    assertThat(openTelemetry.getTracerProvider()).isSameAs(tracerProvider);
    assertThat(openTelemetry.getPropagators()).isSameAs(contextPropagators);
  }

  @Test
  void independentNonGlobalTracers() {
    TracerProvider provider1 = mock(TracerProvider.class);
    Tracer tracer1 = mock(Tracer.class);
    when(provider1.get("foo")).thenReturn(tracer1);
    when(provider1.get("foo", "1.0")).thenReturn(tracer1);
    OpenTelemetry otel1 = DefaultOpenTelemetry.builder().setTracerProvider(provider1).build();
    TracerProvider provider2 = mock(TracerProvider.class);
    Tracer tracer2 = mock(Tracer.class);
    when(provider2.get("foo")).thenReturn(tracer2);
    when(provider2.get("foo", "1.0")).thenReturn(tracer2);
    OpenTelemetry otel2 = DefaultOpenTelemetry.builder().setTracerProvider(provider2).build();

    assertThat(otel1.getTracer("foo")).isSameAs(tracer1);
    assertThat(otel1.getTracer("foo", "1.0")).isSameAs(tracer1);
    assertThat(otel2.getTracer("foo")).isSameAs(tracer2);
    assertThat(otel2.getTracer("foo", "1.0")).isSameAs(tracer2);
  }

  @Test
  void independentNonGlobalPropagators() {
    ContextPropagators propagators1 = mock(ContextPropagators.class);
    OpenTelemetry otel1 = DefaultOpenTelemetry.builder().setPropagators(propagators1).build();
    ContextPropagators propagators2 = mock(ContextPropagators.class);
    OpenTelemetry otel2 = DefaultOpenTelemetry.builder().setPropagators(propagators2).build();

    assertThat(otel1.getPropagators()).isSameAs(propagators1);
    assertThat(otel2.getPropagators()).isSameAs(propagators2);
  }
}
