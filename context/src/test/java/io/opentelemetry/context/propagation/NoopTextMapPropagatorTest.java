/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class NoopTextMapPropagatorTest {

  private static final ContextKey<String> KEY = ContextKey.named("key");

  @Test
  void noopFields() {
    assertThat(TextMapPropagator.noop().fields()).isEmpty();
  }

  @Test
  void extract_contextUnchanged() {
    Context input = Context.current();
    Context result =
        TextMapPropagator.noop().extract(input, new HashMap<>(), MapTextMapGetter.INSTANCE);
    assertThat(result).isSameAs(input);
  }

  @Test
  void extract_nullContext() {
    assertThat(
            TextMapPropagator.noop()
                .extract(null, Collections.emptyMap(), MapTextMapGetter.INSTANCE))
        .isSameAs(Context.groot());
  }

  @Test
  void extract_nullGetter() {
    Context context = Context.current().with(KEY, "treasure");
    assertThat(TextMapPropagator.noop().extract(context, Collections.emptyMap(), null))
        .isSameAs(context);
  }

  @Test
  void inject_nullContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    TextMapPropagator.noop().inject(null, carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_nullSetter() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context = Context.current().with(KEY, "treasure");
    TextMapPropagator.noop().inject(context, carrier, null);
    assertThat(carrier).isEmpty();
  }

  enum MapTextMapGetter implements TextMapGetter<Map<? extends Object, ? extends Object>> {
    INSTANCE;

    @Override
    public Iterable<String> keys(Map<?, ?> carrier) {
      return null;
    }

    @Nullable
    @Override
    public String get(@Nullable Map<?, ?> carrier, String key) {
      return null;
    }
  }
}
