/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.TextMapPropagatorComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.B3MultiPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.B3PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BaggagePropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.JaegerPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceContextPropagatorModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PropagatorFactoryTest {

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(SpiHelper.create(PropagatorFactoryTest.class.getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(PropagatorModel model, ContextPropagators expectedPropagators) {
    ContextPropagators propagators = PropagatorFactory.getInstance().create(model, context);

    assertThat(propagators.toString()).isEqualTo(expectedPropagators.toString());
  }

  @SuppressWarnings("deprecation")
  private static Stream<Arguments> createArguments() {
    return Stream.of(
        // structured list
        Arguments.of(
            new PropagatorModel()
                .withComposite(
                    Arrays.asList(
                        new TextMapPropagatorModel()
                            .withTracecontext(new TraceContextPropagatorModel()),
                        new TextMapPropagatorModel().withBaggage(new BaggagePropagatorModel()),
                        new TextMapPropagatorModel().withB3multi(new B3MultiPropagatorModel()),
                        new TextMapPropagatorModel().withB3(new B3PropagatorModel()),
                        new TextMapPropagatorModel().withJaeger(new JaegerPropagatorModel()))),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader(),
                    JaegerPropagator.getInstance()))),
        // string list
        Arguments.of(
            new PropagatorModel().withCompositeList("tracecontext,baggage,b3multi,b3,jaeger ,none"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader(),
                    JaegerPropagator.getInstance()))),
        // structured list and string list
        Arguments.of(
            new PropagatorModel()
                .withComposite(
                    Arrays.asList(
                        new TextMapPropagatorModel()
                            .withTracecontext(new TraceContextPropagatorModel()),
                        new TextMapPropagatorModel().withBaggage(new BaggagePropagatorModel())))
                .withCompositeList("b3multi,b3,jaeger"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader(),
                    JaegerPropagator.getInstance()))),
        // structured list and string list with overlap
        Arguments.of(
            new PropagatorModel()
                .withComposite(
                    Arrays.asList(
                        new TextMapPropagatorModel()
                            .withTracecontext(new TraceContextPropagatorModel()),
                        new TextMapPropagatorModel().withBaggage(new BaggagePropagatorModel())))
                .withCompositeList("tracecontext,b3multi,b3,jaeger"),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance(),
                    B3Propagator.injectingMultiHeaders(),
                    B3Propagator.injectingSingleHeader(),
                    JaegerPropagator.getInstance()))),
        // spi
        Arguments.of(
            new PropagatorModel()
                .withComposite(
                    Collections.singletonList(
                        new TextMapPropagatorModel().withAdditionalProperty("test", null))),
            ContextPropagators.create(
                TextMapPropagator.composite(
                    new TextMapPropagatorComponentProvider.TestTextMapPropagator(
                        DeclarativeConfigProperties.empty())))),
        Arguments.of(
            new PropagatorModel().withCompositeList("test"),
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
                    .create(new PropagatorModel().withCompositeList("foo"), context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.context.propagation.TextMapPropagator with name \"foo\".");
  }
}
