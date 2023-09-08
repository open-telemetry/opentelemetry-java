/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class SpanLinkingMultiTextMapPropagator implements TextMapPropagator {

  public static final ContextKey<Collection<SpanContext>> SPAN_LINKS =
      ContextKey.named("opentelemetry-propagators-span-links");

  private final List<TextMapPropagator> textMapPropagators;
  private final List<String> allFields;

  SpanLinkingMultiTextMapPropagator(List<TextMapPropagator> textMapPropagators) {
    this.textMapPropagators = textMapPropagators;
    this.allFields = Collections.unmodifiableList(getAllFields(this.textMapPropagators));
  }

  @Override
  public Collection<String> fields() {
    return allFields;
  }

  private static List<String> getAllFields(List<TextMapPropagator> textPropagators) {
    Set<String> fields = new LinkedHashSet<>();
    for (TextMapPropagator textPropagator : textPropagators) {
      fields.addAll(textPropagator.fields());
    }

    return new ArrayList<>(fields);
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null || setter == null) {
      return;
    }
    for (TextMapPropagator textPropagator : textMapPropagators) {
      textPropagator.inject(context, carrier, setter);
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      return Context.root();
    }
    if (getter == null) {
      return context;
    }

    SpanContext parentSpanContext = null;
    List<SpanContext> spanLinks = null;

    for (TextMapPropagator textPropagator : textMapPropagators) {
      SpanContext result =
          Span.fromContext(textPropagator.extract(Context.root(), carrier, getter))
              .getSpanContext();
      if (result.isValid()) {
        if (parentSpanContext == null) {
          parentSpanContext = result;
        } else {
          if (spanLinks == null) {
            spanLinks = new ArrayList<>();
          }
          spanLinks.add(result);
        }
      }
    }

    if (spanLinks != null) {
      context = context.with(SPAN_LINKS, Collections.unmodifiableList(spanLinks));
    }
    if (parentSpanContext != null) {
      context = Span.wrap(parentSpanContext).storeInContext(context);
    }

    return context;
  }
}
