/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.propagation.TextMapPropagator;
import org.junit.jupiter.api.Test;

class OpenTracingPropagatorsTest {

  @Test
  public void defaultPropagators() {
    OpenTracingPropagators openTracingPropagators = OpenTracingPropagators.builder().build();
    assertThat(openTracingPropagators.textMapPropagator())
        .isSameAs(openTracingPropagators.httpHeadersPropagator());
  }

  @Test
  public void textMapPropagator() {
    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();

    OpenTracingPropagators openTracingPropagators =
        OpenTracingPropagators.builder().setTextMap(textMapPropagator).build();

    assertThat(openTracingPropagators.textMapPropagator())
        .isNotSameAs(openTracingPropagators.httpHeadersPropagator());

    assertThat(openTracingPropagators.textMapPropagator()).isSameAs(textMapPropagator);
  }

  @Test
  public void httpHeadersPropagator() {
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();

    OpenTracingPropagators openTracingPropagators =
        OpenTracingPropagators.builder().setHttpHeaders(httpHeadersPropagator).build();

    assertThat(openTracingPropagators.textMapPropagator())
        .isNotSameAs(openTracingPropagators.httpHeadersPropagator());

    assertThat(openTracingPropagators.httpHeadersPropagator()).isSameAs(httpHeadersPropagator);
  }

  @Test
  public void bothCustomPropagator() {
    TextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    TextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();

    OpenTracingPropagators openTracingPropagators =
        OpenTracingPropagators.builder()
            .setTextMap(textMapPropagator)
            .setHttpHeaders(httpHeadersPropagator)
            .build();

    assertThat(openTracingPropagators.textMapPropagator())
        .isNotSameAs(openTracingPropagators.httpHeadersPropagator());

    assertThat(openTracingPropagators.textMapPropagator()).isSameAs(textMapPropagator);
    assertThat(openTracingPropagators.httpHeadersPropagator()).isSameAs(httpHeadersPropagator);
  }
}
