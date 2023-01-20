/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

final class PropagatorConfiguration {

  private static final List<String> DEFAULT_PROPAGATORS = Arrays.asList("tracecontext", "baggage");

  static ContextPropagators configurePropagators(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer) {
    Set<TextMapPropagator> propagators = new LinkedHashSet<>();
    List<String> requestedPropagators = config.getList("otel.propagators", DEFAULT_PROPAGATORS);

    NamedSpiManager<TextMapPropagator> spiPropagatorsManager =
        SpiUtil.loadConfigurable(
            ConfigurablePropagatorProvider.class,
            ConfigurablePropagatorProvider::getName,
            ConfigurablePropagatorProvider::getPropagator,
            config,
            serviceClassLoader);

    if (requestedPropagators.contains("none")) {
      if (requestedPropagators.size() > 1) {
        throw new ConfigurationException(
            "otel.propagators contains 'none' along with other propagators");
      }
      return ContextPropagators.noop();
    }
    for (String propagatorName : requestedPropagators) {
      propagators.add(
          propagatorCustomizer.apply(getPropagator(propagatorName, spiPropagatorsManager), config));
    }

    return ContextPropagators.create(TextMapPropagator.composite(propagators));
  }

  private static TextMapPropagator getPropagator(
      String name, NamedSpiManager<TextMapPropagator> spiPropagatorsManager) {
    if (name.equals("tracecontext")) {
      return W3CTraceContextPropagator.getInstance();
    }
    if (name.equals("baggage")) {
      return W3CBaggagePropagator.getInstance();
    }

    TextMapPropagator spiPropagator = spiPropagatorsManager.getByName(name);
    if (spiPropagator != null) {
      return spiPropagator;
    }
    throw new ConfigurationException(
        "Unrecognized value for otel.propagators: "
            + name
            + ". Make sure the artifact including the propagator is on the classpath.");
  }

  private PropagatorConfiguration() {}
}
