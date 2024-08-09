/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.AbstractDefaultTracerTest;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import org.junit.jupiter.api.Test;

public class ExtendedDefaultTracerTest extends AbstractDefaultTracerTest {

  @Override
  public Tracer getTracer() {
    return ExtendedDefaultTracer.getNoop();
  }

  @Override
  public TracerProvider getTracerProvider() {
    return ExtendedDefaultTracerProvider.getNoop();
  }

  @Test
  void incubatingApiIsLoaded() {
    Tracer tracer = TracerProvider.noop().get("test");
    assertThat(tracer).isSameAs(OpenTelemetry.noop().getTracer("test"));

    assertThat(tracer).isInstanceOf(ExtendedTracer.class);
    assertThat(tracer.spanBuilder("test")).isInstanceOf(ExtendedSpanBuilder.class);
  }
}
