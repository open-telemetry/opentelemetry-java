/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

final class MultiTextMapPropagator implements TextMapPropagator {
  private final TextMapPropagator[] textPropagators;
  private final Collection<String> allFields;

  MultiTextMapPropagator(List<TextMapPropagator> textPropagators) {
    this.textPropagators = new TextMapPropagator[textPropagators.size()];
    textPropagators.toArray(this.textPropagators);
    this.allFields = Collections.unmodifiableList(getAllFields(this.textPropagators));
  }

  @Override
  public Collection<String> fields() {
    return allFields;
  }

  private static List<String> getAllFields(TextMapPropagator[] textPropagators) {
    Set<String> fields = new LinkedHashSet<>();
    for (int i = 0; i < textPropagators.length; i++) {
      fields.addAll(textPropagators[i].fields());
    }

    return new ArrayList<>(fields);
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, Setter<C> setter) {
    for (int i = 0; i < textPropagators.length; i++) {
      textPropagators[i].inject(context, carrier, setter);
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
    for (int i = 0; i < textPropagators.length; i++) {
      context = textPropagators[i].extract(context, carrier, getter);
    }
    return context;
  }
}
