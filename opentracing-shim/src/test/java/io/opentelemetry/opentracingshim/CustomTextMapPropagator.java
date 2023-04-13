/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

class CustomTextMapPropagator implements TextMapPropagator {
  static final String DEBUG_HEADER = "debug-id";
  private boolean extracted;
  private boolean injected;

  @Override
  public Collection<String> fields() {
    return Collections.emptyList();
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    injected = true;
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    extracted = true;
    return contextFromDebugHeader(context, carrier, getter);
  }

  public boolean isExtracted() {
    return extracted;
  }

  public boolean isInjected() {
    return injected;
  }

  private static <C> Context contextFromDebugHeader(
      Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    String value = getter.get(carrier, DEBUG_HEADER);
    if (value == null || value.isEmpty()) {
      return context;
    }
    SpanContext invalidSpanContextWithFlags =
        SpanContext.create(
            TraceId.getInvalid(),
            SpanId.getInvalid(),
            TraceFlags.getSampled(),
            TraceState.getDefault());
    return context.with(PropagatedOpenTelemetrySpan.create(invalidSpanContextWithFlags));
  }
}
