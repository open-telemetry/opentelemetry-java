/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class TextMapPropagatorFactory
    implements Factory<TextMapPropagatorModel, TextMapPropagatorAndName> {

  private static final TextMapPropagatorFactory INSTANCE = new TextMapPropagatorFactory();

  private TextMapPropagatorFactory() {}

  static TextMapPropagatorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public TextMapPropagatorAndName create(
      TextMapPropagatorModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model.getTracecontext() != null) {
      return getPropagator(spiHelper, "tracecontext");
    }
    if (model.getBaggage() != null) {
      return getPropagator(spiHelper, "baggage");
    }
    if (model.getB3() != null) {
      return getPropagator(spiHelper, "b3");
    }
    if (model.getB3multi() != null) {
      return getPropagator(spiHelper, "b3multi");
    }
    if (model.getJaeger() != null) {
      return getPropagator(spiHelper, "jaeger");
    }
    if (model.getOttrace() != null) {
      return getPropagator(spiHelper, "ottrace");
    }
    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new DeclarativeConfigException(
            "Invalid configuration - multiple propgators set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> propagatorKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Missing propagator. This is a programming error."));
      TextMapPropagator propagator =
          FileConfigUtil.loadComponent(
              spiHelper,
              TextMapPropagator.class,
              propagatorKeyValue.getKey(),
              propagatorKeyValue.getValue());
      return TextMapPropagatorAndName.create(propagator, propagatorKeyValue.getKey());
    } else {
      throw new DeclarativeConfigException("propagator must be set");
    }
  }

  static TextMapPropagatorAndName getPropagator(SpiHelper spiHelper, String name) {
    TextMapPropagator textMapPropagator;
    if (name.equals("tracecontext")) {
      textMapPropagator = W3CTraceContextPropagator.getInstance();
    } else if (name.equals("baggage")) {
      textMapPropagator = W3CBaggagePropagator.getInstance();
    } else {
      textMapPropagator =
          FileConfigUtil.loadComponent(
              spiHelper, TextMapPropagator.class, name, Collections.emptyMap());
    }
    return TextMapPropagatorAndName.create(textMapPropagator, name);
  }
}
