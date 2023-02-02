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
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentracing.propagation.Format;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OpenTracingShimTest {

  @AfterEach
  void tearDown() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void createTracerShim_fromOpenTelemetryInstance() {
    OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
    SdkTracerProvider sdk = SdkTracerProvider.builder().build();
    when(openTelemetry.getTracerProvider()).thenReturn(sdk);
    TextMapPropagator textMapPropagator = mock(TextMapPropagator.class);
    ContextPropagators contextPropagators = mock(ContextPropagators.class);
    when(contextPropagators.getTextMapPropagator()).thenReturn(textMapPropagator);
    when(openTelemetry.getPropagators()).thenReturn(contextPropagators);

    TracerShim tracerShim = (TracerShim) OpenTracingShim.createTracerShim(openTelemetry);
    assertThat(tracerShim.propagation().getPropagator(Format.Builtin.TEXT_MAP))
        .isSameAs(textMapPropagator);
    assertThat(tracerShim.propagation().getPropagator(Format.Builtin.HTTP_HEADERS))
        .isSameAs(textMapPropagator);
    assertThat(tracerShim.tracer()).isEqualTo(sdk.get("opentracing-shim"));
  }

  @Test
  void createTracerShim_withPropagators() {
    TracerProvider tracerProvider = mock(TracerProvider.class);

    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();

    TracerShim tracerShim =
        (TracerShim)
            OpenTracingShim.createTracerShim(
                tracerProvider, textMapPropagator, httpHeadersPropagator);

    assertThat(tracerShim.propagation().getPropagator(Format.Builtin.TEXT_MAP))
        .isSameAs(textMapPropagator);
    assertThat(tracerShim.propagation().getPropagator(Format.Builtin.HTTP_HEADERS))
        .isSameAs(httpHeadersPropagator);
  }

  @Test
  void createTracerShim_withTraceProviderAndPropagator() {
    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();
    SdkTracerProvider sdk = SdkTracerProvider.builder().build();
    TracerShim tracerShim =
        (TracerShim)
            OpenTracingShim.createTracerShim(sdk, textMapPropagator, httpHeadersPropagator);

    assertThat(tracerShim.propagation().getPropagator(Format.Builtin.TEXT_MAP))
        .isSameAs(textMapPropagator);
    assertThat(tracerShim.propagation().getPropagator(Format.Builtin.HTTP_HEADERS))
        .isSameAs(httpHeadersPropagator);
    assertThat(tracerShim.tracer()).isSameAs(sdk.get("opentracing-shim"));
  }
}
