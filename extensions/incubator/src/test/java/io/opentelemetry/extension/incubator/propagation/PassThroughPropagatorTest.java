/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.propagation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class PassThroughPropagatorTest {
  private static final TextMapPropagator propagator =
      PassThroughPropagator.create("animal", "food");

  private static final TextMapGetter<Map<String, String>> getter =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @Test
  void propagates() {
    Map<String, String> incoming = new HashMap<>();
    incoming.put("animal", "cat");
    incoming.put("food", "pizza");
    incoming.put("country", "japan");

    Context context = propagator.extract(Context.root(), incoming, getter);

    Map<String, String> outgoing = new HashMap<>();
    propagator.inject(context, outgoing, Map::put);
    assertThat(outgoing).containsOnly(entry("animal", "cat"), entry("food", "pizza"));
  }

  @Test
  void noFields() {
    TextMapPropagator propagator = PassThroughPropagator.create();
    Map<String, String> incoming = new HashMap<>();
    incoming.put("animal", "cat");
    incoming.put("food", "pizza");
    incoming.put("country", "japan");

    Context context = propagator.extract(Context.root(), incoming, getter);

    Map<String, String> outgoing = new HashMap<>();
    propagator.inject(context, outgoing, Map::put);
    assertThat(outgoing).isEmpty();
  }

  @Test
  void emptyMap() {
    Map<String, String> incoming = new HashMap<>();

    Context context = propagator.extract(Context.root(), incoming, getter);

    Map<String, String> outgoing = new HashMap<>();
    propagator.inject(context, outgoing, Map::put);
    assertThat(outgoing).isEmpty();
  }

  @Test
  void nullFields() {
    assertThatThrownBy(() -> PassThroughPropagator.create((String[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("fields");
    assertThatThrownBy(() -> PassThroughPropagator.create((Iterable<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("fields");
    assertThatThrownBy(() -> PassThroughPropagator.create("cat", null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("field");
  }
}
