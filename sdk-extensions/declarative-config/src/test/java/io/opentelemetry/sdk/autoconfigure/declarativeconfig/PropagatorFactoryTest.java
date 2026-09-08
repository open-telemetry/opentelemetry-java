/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.TextMapPropagatorComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.B3MultiPropagatorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.B3PropagatorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BaggagePropagatorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PropagatorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TextMapPropagatorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TraceContextPropagatorModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PropagatorFactoryTest {

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(ComponentLoader.forClassLoader(getClass().getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(PropagatorModel model, ContextPropagators expectedPropagators) {
    ContextPropagators propagators = PropagatorFactory.getInstance().create(model, context);

    assertThat(propagators.toString()).isEqualTo(expectedPropagators.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.argumentSet(
            "structured list",
            new PropagatorModel()
                .setComposite(
                    Arrays.asList(
                        new TextMapPropagatorModel()
                            .setTracecontext(new TraceContextPropagatorModel()),
                        new TextMapPropagatorModel().setBaggage(new BaggagePropagatorModel()),
                        new TextMapPropagatorModel().setB3multi(new B3MultiPropagatorModel()),
                        new TextMapPropagatorModel().setB3(new B3PropagatorModel()))),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader()))),
        Arguments.argumentSet(
            "string list",
            new PropagatorModel().setCompositeList("tracecontext,baggage,b3multi,b3 ,none"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader()))),
        Arguments.argumentSet(
            "structured list and string list",
            new PropagatorModel()
                .setComposite(
                    Arrays.asList(
                        new TextMapPropagatorModel()
                            .setTracecontext(new TraceContextPropagatorModel()),
                        new TextMapPropagatorModel().setBaggage(new BaggagePropagatorModel())))
                .setCompositeList("b3multi,b3"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader()))),
        Arguments.argumentSet(
            "structured list and string list with overlap",
            new PropagatorModel()
                .setComposite(
                    Arrays.asList(
                        new TextMapPropagatorModel()
                            .setTracecontext(new TraceContextPropagatorModel()),
                        new TextMapPropagatorModel().setBaggage(new BaggagePropagatorModel())))
                .setCompositeList("tracecontext,b3multi,b3"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader()))),
        Arguments.argumentSet(
            "spi structured list",
            new PropagatorModel()
                .setComposite(
                    Collections.singletonList(
                        new TextMapPropagatorModel().setExtensionProperty("test", null))),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    new TextMapPropagatorComponentProvider.TestTextMapPropagator(
                        DeclarativeConfigProperties.empty())))),
        Arguments.argumentSet(
            "spi string list",
            new PropagatorModel().setCompositeList("test"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    new TextMapPropagatorComponentProvider.TestTextMapPropagator(
                        DeclarativeConfigProperties.empty())))));
  }

  @Test
  void create_SpiPropagator_Unknown() {
    assertThatThrownBy(
            () ->
                PropagatorFactory.getInstance()
                    .create(new PropagatorModel().setCompositeList("foo"), context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.context.propagation.TextMapPropagator with name \"foo\".");
  }
}
