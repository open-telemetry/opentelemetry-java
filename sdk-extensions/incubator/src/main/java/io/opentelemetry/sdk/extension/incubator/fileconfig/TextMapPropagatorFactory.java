/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;

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
    ConfigKeyValue propagatorKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "propagator");
    return getPropagator(context, propagatorKeyValue);
  }

  static TextMapPropagatorAndName getPropagator(
      DeclarativeConfigContext context, ConfigKeyValue configKeyValue) {
    String name = configKeyValue.getKey();

    TextMapPropagator textMapPropagator;
    if (name.equals("tracecontext")) {
      textMapPropagator = W3CTraceContextPropagator.getInstance();
    } else if (name.equals("baggage")) {
      textMapPropagator = W3CBaggagePropagator.getInstance();
    } else {
      textMapPropagator = context.loadComponent(TextMapPropagator.class, configKeyValue);
    }
    return TextMapPropagatorAndName.create(textMapPropagator, name);
  }
}
