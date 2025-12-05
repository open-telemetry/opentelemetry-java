/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorPropertyModel;
import java.util.Collections;
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
    if (model.getTracecontext() != null) {
      return getPropagator(context, "tracecontext");
    }
    if (model.getBaggage() != null) {
      return getPropagator(context, "baggage");
    }
    if (model.getB3() != null) {
      return getPropagator(context, "b3");
    }
    if (model.getB3multi() != null) {
      return getPropagator(context, "b3multi");
    }
    if (model.getJaeger() != null) {
      return getPropagator(context, "jaeger");
    }
    if (model.getOttrace() != null) {
      return getPropagator(context, "ottrace");
    }

    Map.Entry<String, TextMapPropagatorPropertyModel> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "propagator");
    TextMapPropagator propagator =
        context.loadComponent(TextMapPropagator.class, keyValue.getKey(), keyValue.getValue());
    return TextMapPropagatorAndName.create(propagator, keyValue.getKey());
  }

  static TextMapPropagatorAndName getPropagator(DeclarativeConfigContext context, String name) {
    TextMapPropagator textMapPropagator;
    if (name.equals("tracecontext")) {
      textMapPropagator = W3CTraceContextPropagator.getInstance();
    } else if (name.equals("baggage")) {
      textMapPropagator = W3CBaggagePropagator.getInstance();
    } else {
      textMapPropagator =
          context.loadComponent(TextMapPropagator.class, name, Collections.emptyMap());
    }
    return TextMapPropagatorAndName.create(textMapPropagator, name);
  }
}
