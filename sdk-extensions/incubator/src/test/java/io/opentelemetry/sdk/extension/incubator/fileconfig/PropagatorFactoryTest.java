/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PropagatorFactoryTest {

  private final SpiHelper spiHelper =
      SpiHelper.create(PropagatorFactoryTest.class.getClassLoader());

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(PropagatorModel model, ContextPropagators expectedPropagators) {
    ContextPropagators propagators =
        PropagatorFactory.getInstance().create(model, spiHelper, Collections.emptyList());

    assertThat(propagators.toString()).isEqualTo(expectedPropagators.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(
            new PropagatorModel()
                .withComposite(
                    Arrays.asList("tracecontext", "baggage", "ottrace", "b3multi", "b3", "jaeger")),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    OtTracePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader(),
                    JaegerPropagator.getInstance()))));
  }
}
