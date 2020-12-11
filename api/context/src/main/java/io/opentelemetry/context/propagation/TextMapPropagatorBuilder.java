/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TextMapPropagatorBuilder {
  final List<TextMapPropagator> delegates;
  Collection<String> allFields;
  boolean stopExtractAfterFirst = false;
  boolean iterateBackwards = false;

  TextMapPropagatorBuilder(Iterable<TextMapPropagator> delegates) {
    this.delegates = new ArrayList<>();
    for (TextMapPropagator propagator : delegates) {
      requireNonNull(propagator, "propagator");
      this.delegates.add(propagator);
    }
  }

  public TextMapPropagatorBuilder stopExtractAfterFirst() {
    this.stopExtractAfterFirst = true;
    return this;
  }

  public TextMapPropagatorBuilder iterateBackwards() {
    this.iterateBackwards = true;
    return this;
  }

  TextMapPropagator build() {
    if (delegates.isEmpty()) {
      return TextMapPropagator.noop();
    }
    if (delegates.size() == 1) {
      return delegates.get(0);
    }
    this.allFields = getAllFieldsFromDelegates();
    return new MultiTextMapPropagator(this);
  }

  private List<String> getAllFieldsFromDelegates() {
    Set<String> fields = new LinkedHashSet<>();
    for (TextMapPropagator textPropagator : delegates) {
      fields.addAll(textPropagator.fields());
    }
    return new ArrayList<>(fields);
  }
}
