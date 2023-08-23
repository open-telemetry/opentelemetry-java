/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

final class PropagatorsFactory implements Factory<List<String>, ContextPropagators> {

  private static final PropagatorsFactory INSTANCE = new PropagatorsFactory();

  private PropagatorsFactory() {}

  static PropagatorsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ContextPropagators create(
      @Nullable List<String> model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null || model.isEmpty()) {
      model = Arrays.asList("tracecontext", "baggage");
    }

    if (model.contains("none")) {
      if (model.size() > 1) {
        throw new ConfigurationException(
            "propagators contains \"none\" along with other propagators");
      }
      return ContextPropagators.noop();
    }

    NamedSpiManager<TextMapPropagator> spiPropagatorsManager =
        spiHelper.loadConfigurable(
            ConfigurablePropagatorProvider.class,
            ConfigurablePropagatorProvider::getName,
            ConfigurablePropagatorProvider::getPropagator,
            DefaultConfigProperties.createForTest(Collections.emptyMap()));
    Set<TextMapPropagator> propagators = new LinkedHashSet<>();
    for (String propagator : model) {
      propagators.add(getPropagator(propagator, spiPropagatorsManager));
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
}
