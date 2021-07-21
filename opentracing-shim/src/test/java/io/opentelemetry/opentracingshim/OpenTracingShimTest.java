/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OpenTracingShimTest {

  @AfterEach
  void tearDown() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) OpenTracingShim.createTracerShim();
    assertThat(tracerShim.tracer()).isEqualTo(GlobalOpenTelemetry.getTracer("opentracingshim"));
  }

  @Test
  void createTracerShim_fromOpenTelemetryInstance() {
    OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
    SdkTracerProvider sdk = SdkTracerProvider.builder().build();
    when(openTelemetry.getTracerProvider()).thenReturn(sdk);
    ContextPropagators contextPropagators = mock(ContextPropagators.class);
    when(contextPropagators.getTextMapPropagator()).thenReturn(mock(TextMapPropagator.class));
    when(openTelemetry.getPropagators()).thenReturn(contextPropagators);

    TracerShim tracerShim = (TracerShim) OpenTracingShim.createTracerShim(openTelemetry);
    assertThat(tracerShim.tracer()).isEqualTo(sdk.get("opentracingshim"));
  }

  @Test
  void createTracerShim_withPropagators() {
    Tracer tracer = mock(Tracer.class);

    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();

    TracerShim tracerShim =
        (TracerShim)
            OpenTracingShim.createTracerShim(
                tracer,
                OpenTracingPropagators.builder()
                    .setTextMap(textMapPropagator)
                    .setHttpHeaders(httpHeadersPropagator)
                    .build());

    assertThat(tracerShim.propagators().textMapPropagator()).isSameAs(textMapPropagator);
    assertThat(tracerShim.propagators().httpHeadersPropagator()).isSameAs(httpHeadersPropagator);
  }
}
