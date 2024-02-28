/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
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

final class TextMapPropagatorFactory implements Factory<List<String>, TextMapPropagator> {

  private static final TextMapPropagatorFactory INSTANCE = new TextMapPropagatorFactory();

  private TextMapPropagatorFactory() {}

  static TextMapPropagatorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public TextMapPropagator create(
      @Nullable List<String> model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null || model.isEmpty()) {
      model = Arrays.asList("tracecontext", "baggage");
    }

    if (model.contains("none")) {
      if (model.size() > 1) {
        throw new ConfigurationException(
            "propagators contains \"none\" along with other propagators");
      }
      return TextMapPropagator.noop();
    }

    NamedSpiManager<TextMapPropagator> spiPropagatorsManager =
        spiHelper.loadConfigurable(
            ConfigurablePropagatorProvider.class,
            ConfigurablePropagatorProvider::getName,
            ConfigurablePropagatorProvider::getPropagator,
            DefaultConfigProperties.createFromMap(Collections.emptyMap()));
    Set<TextMapPropagator> propagators = new LinkedHashSet<>();
    for (String propagator : model) {
      propagators.add(getPropagator(propagator, spiPropagatorsManager));
    }

    return TextMapPropagator.composite(propagators);
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
    throw new ConfigurationException("Unrecognized propagator: " + name);
  }
}
