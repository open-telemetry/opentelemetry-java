/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.AwsXrayPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracerPropagator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class PropagatorConfiguration {

  static ContextPropagators configurePropagators(ConfigProperties config) {
    List<TextMapPropagator> propagators = new ArrayList<>();
    for (String propagatorName : config.getCommaSeparatedValues("otel.propagators")) {
      propagatorName = propagatorName.toLowerCase(Locale.ROOT);
      propagators.add(PropagatorConfiguration.getPropagator(propagatorName));
    }
    return ContextPropagators.create(TextMapPropagator.composite(propagators));
  }

  private static TextMapPropagator getPropagator(String name) {
    switch (name) {
      case "tracecontext":
        return W3CTraceContextPropagator.getInstance();
      case "baggage":
        return W3CBaggagePropagator.getInstance();
      case "b3":
        return B3Propagator.getInstance();
      case "b3multi":
        return B3Propagator.builder().injectMultipleHeaders().build();
      case "jaeger":
        return JaegerPropagator.getInstance();
      case "ottracer":
        return OtTracerPropagator.getInstance();
      case "xray":
        return AwsXrayPropagator.getInstance();
      default:
        throw new IllegalStateException(
            "Unrecognized value for otel.propagators coniguration: " + name);
    }
  }

  private PropagatorConfiguration() {}
}
