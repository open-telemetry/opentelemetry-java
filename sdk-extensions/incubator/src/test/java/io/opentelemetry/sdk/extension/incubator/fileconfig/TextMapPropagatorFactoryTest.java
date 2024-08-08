/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.TextMapPropagatorComponentProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TextMapPropagatorFactoryTest {

  private final SpiHelper spiHelper =
      SpiHelper.create(TextMapPropagatorFactoryTest.class.getClassLoader());

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(List<String> model, TextMapPropagator expectedPropagator) {
    TextMapPropagator propagator =
        TextMapPropagatorFactory.getInstance().create(model, spiHelper, Collections.emptyList());

    assertThat(propagator.toString()).isEqualTo(expectedPropagator.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(
            Collections.emptyList(),
            TextMapPropagator.composite(
                W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())),
        Arguments.of(Collections.singletonList("none"), TextMapPropagator.noop()),
        Arguments.of(
            Arrays.asList("tracecontext", "baggage", "ottrace", "b3multi", "b3", "jaeger"),
            TextMapPropagator.composite(
                W3CTraceContextPropagator.getInstance(),
                W3CBaggagePropagator.getInstance(),
                OtTracePropagator.getInstance(),
                B3Propagator.injectingMultiHeaders(),
                B3Propagator.injectingSingleHeader(),
                JaegerPropagator.getInstance())));
  }

  @Test
  void create_NoneAndOther() {
    assertThatThrownBy(
            () ->
                TextMapPropagatorFactory.getInstance()
                    .create(Arrays.asList("none", "foo"), spiHelper, Collections.emptyList()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("propagators contains \"none\" along with other propagators");
  }

  @Test
  void create_SpiPropagator_Unknown() {
    assertThatThrownBy(
            () ->
                TextMapPropagatorFactory.getInstance()
                    .create(Collections.singletonList("foo"), spiHelper, Collections.emptyList()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.context.propagation.TextMapPropagator with name \"foo\".");
  }

  @Test
  void create_SpiPropagator_Valid() {
    TextMapPropagator textMapPropagator =
        TextMapPropagatorFactory.getInstance()
            .create(Collections.singletonList("test"), spiHelper, new ArrayList<>());
    assertThat(textMapPropagator)
        .isInstanceOf(TextMapPropagatorComponentProvider.TestTextMapPropagator.class);
  }
}
