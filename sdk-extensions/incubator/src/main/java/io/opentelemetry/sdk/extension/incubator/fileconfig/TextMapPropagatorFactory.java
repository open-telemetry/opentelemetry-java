/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;
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
      TextMapPropagatorModel model, DeclarativeConfigContext context) {
    Map.Entry<String, DeclarativeConfigProperties> propagatorKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "propagator");
    return getPropagator(context, propagatorKeyValue.getKey(), propagatorKeyValue.getValue());
  }

  static TextMapPropagatorAndName getPropagator(
      DeclarativeConfigContext context, String name, DeclarativeConfigProperties configProperties) {
    TextMapPropagator textMapPropagator;
    if (name.equals("tracecontext")) {
      textMapPropagator = W3CTraceContextPropagator.getInstance();
    } else if (name.equals("baggage")) {
      textMapPropagator = W3CBaggagePropagator.getInstance();
    } else {
      textMapPropagator = context.loadComponent(TextMapPropagator.class, name, configProperties);
    }
    return TextMapPropagatorAndName.create(textMapPropagator, name);
  }
}
