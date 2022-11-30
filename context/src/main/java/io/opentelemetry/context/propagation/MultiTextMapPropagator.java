/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

final class MultiTextMapPropagator implements TextMapPropagator {
  private final TextMapPropagator[] textMapPropagators;
  private final Collection<String> allFields;

  MultiTextMapPropagator(TextMapPropagator... textMapPropagators) {
    this(Arrays.asList(textMapPropagators));
  }

  MultiTextMapPropagator(List<TextMapPropagator> textMapPropagators) {
    this.textMapPropagators = new TextMapPropagator[textMapPropagators.size()];
    textMapPropagators.toArray(this.textMapPropagators);
    this.allFields = Collections.unmodifiableList(getAllFields(this.textMapPropagators));
  }

  @Override
  public Collection<String> fields() {
    return allFields;
  }

  private static List<String> getAllFields(TextMapPropagator[] textPropagators) {
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
    for (TextMapPropagator textPropagator : textMapPropagators) {
      context = textPropagator.extract(context, carrier, getter);
    }
    return context;
  }

  @Override
  public String toString() {
    return "MultiTextMapPropagator{textMapPropagators=" + Arrays.toString(textMapPropagators) + '}';
  }
}
