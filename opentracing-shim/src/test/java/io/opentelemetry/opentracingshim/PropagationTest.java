/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropagationTest {
  private final Tracer tracer = SdkTracerProvider.builder().build().get("PropagationTest");

  @BeforeEach
  @AfterEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  public void defaultPropagators() {
    Propagation propagation =
        new Propagation(new TelemetryInfo(tracer, OpenTracingPropagators.builder().build()));
    assertThat(propagation.propagators().textMapPropagator())
        .isSameAs(propagation.propagators().httpHeadersPropagator());
  }

  @Test
  public void textMapPropagator() {
    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();

    Propagation propagation =
        new Propagation(
            new TelemetryInfo(
                tracer, OpenTracingPropagators.builder().setTextMap(textMapPropagator).build()));

    assertThat(propagation.propagators().textMapPropagator())
        .isNotSameAs(propagation.propagators().httpHeadersPropagator());

    assertThat(propagation.propagators().textMapPropagator()).isSameAs(textMapPropagator);
  }

  @Test
  public void httpHeadersPropagator() {
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();

    Propagation propagation =
        new Propagation(
            new TelemetryInfo(
                tracer,
                OpenTracingPropagators.builder().setHttpHeaders(httpHeadersPropagator).build()));

    assertThat(propagation.propagators().textMapPropagator())
        .isNotSameAs(propagation.propagators().httpHeadersPropagator());

    assertThat(propagation.propagators().httpHeadersPropagator()).isSameAs(httpHeadersPropagator);
  }

  @Test
  public void bothCustomPropagator() {
    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();

    Propagation propagation =
        new Propagation(
            new TelemetryInfo(
                tracer,
                OpenTracingPropagators.builder()
                    .setTextMap(textMapPropagator)
                    .setHttpHeaders(httpHeadersPropagator)
                    .build()));

    assertThat(propagation.propagators().textMapPropagator())
        .isNotSameAs(propagation.propagators().httpHeadersPropagator());

    assertThat(propagation.propagators().httpHeadersPropagator()).isSameAs(httpHeadersPropagator);
  }
}
