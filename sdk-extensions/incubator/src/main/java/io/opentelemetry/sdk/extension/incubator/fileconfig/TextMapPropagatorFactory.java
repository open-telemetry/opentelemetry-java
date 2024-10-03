/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class TextMapPropagatorFactory implements Factory<List<String>, TextMapPropagator> {

  private static final TextMapPropagatorFactory INSTANCE = new TextMapPropagatorFactory();

  private TextMapPropagatorFactory() {}

  static TextMapPropagatorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public TextMapPropagator create(
      List<String> model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model.isEmpty()) {
      model = Arrays.asList("tracecontext", "baggage");
    }

    if (model.contains("none")) {
      if (model.size() > 1) {
        throw new DeclarativeConfigException(
            "propagators contains \"none\" along with other propagators");
      }
      return TextMapPropagator.noop();
    }

    List<TextMapPropagator> propagators = new ArrayList<>();
    for (String propagator : model) {
      propagators.add(getPropagator(spiHelper, propagator));
    }

    return TextMapPropagator.composite(propagators);
  }

  private static TextMapPropagator getPropagator(SpiHelper spiHelper, String name) {
    if (name.equals("tracecontext")) {
      return W3CTraceContextPropagator.getInstance();
    }
    if (name.equals("baggage")) {
      return W3CBaggagePropagator.getInstance();
    }

    return FileConfigUtil.loadComponent(
        spiHelper, TextMapPropagator.class, name, Collections.emptyMap());
  }
}
