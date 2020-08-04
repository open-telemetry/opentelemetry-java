/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.context.propagation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Context;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultContextPropagators}. */
class DefaultPropagatorsTest {

  @Test
  void addHttpTextFormatNull() {
    assertThrows(
        NullPointerException.class,
        () -> DefaultContextPropagators.builder().addHttpTextFormat(null));
  }

  @Test
  void testInject() {
    CustomHttpTextFormat propagator1 = new CustomHttpTextFormat("prop1");
    CustomHttpTextFormat propagator2 = new CustomHttpTextFormat("prop2");
    ContextPropagators propagators =
        DefaultContextPropagators.builder()
            .addHttpTextFormat(propagator1)
            .addHttpTextFormat(propagator2)
            .build();

    Context context = Context.current();
    context = context.withValue(propagator1.getKey(), "value1");
    context = context.withValue(propagator2.getKey(), "value2");

    Map<String, String> map = new HashMap<>();
    propagators.getHttpTextFormat().inject(context, map, MapSetter.INSTANCE);
    assertThat(map.get(propagator1.getKeyName())).isEqualTo("value1");
    assertThat(map.get(propagator2.getKeyName())).isEqualTo("value2");
  }

  @Test
  void testExtract() {
    CustomHttpTextFormat propagator1 = new CustomHttpTextFormat("prop1");
    CustomHttpTextFormat propagator2 = new CustomHttpTextFormat("prop2");
    CustomHttpTextFormat propagator3 = new CustomHttpTextFormat("prop3");
    ContextPropagators propagators =
        DefaultContextPropagators.builder()
            .addHttpTextFormat(propagator1)
            .addHttpTextFormat(propagator2)
            .build();

    // Put values for propagators 1 and 2 only.
    Map<String, String> map = new HashMap<>();
    map.put(propagator1.getKeyName(), "value1");
    map.put(propagator2.getKeyName(), "value2");

    Context context =
        propagators.getHttpTextFormat().extract(Context.current(), map, MapGetter.INSTANCE);
    assertThat(propagator1.getKey().get(context)).isEqualTo("value1");
    assertThat(propagator2.getKey().get(context)).isEqualTo("value2");
    assertThat(propagator3.getKey().get(context)).isNull(); // Handle missing value.
  }

  @Test
  void noopPropagator() {
    ContextPropagators propagators = DefaultContextPropagators.builder().build();

    Context context = Context.current();
    Map<String, String> map = new HashMap<>();
    propagators.getHttpTextFormat().inject(context, map, MapSetter.INSTANCE);
    assertThat(map).isEmpty();

    assertThat(propagators.getHttpTextFormat().extract(context, map, MapGetter.INSTANCE))
        .isSameAs(context);
  }

  private static class CustomHttpTextFormat implements HttpTextFormat {
    private final String name;
    private final Context.Key<String> key;

    CustomHttpTextFormat(String name) {
      this.name = name;
      this.key = Context.key(name);
    }

    Context.Key<String> getKey() {
      return key;
    }

    String getKeyName() {
      return name;
    }

    @Override
    public List<String> fields() {
      return Collections.singletonList(name);
    }

    @Override
    public <C> void inject(Context context, C carrier, Setter<C> setter) {
      Object payload = key.get(context);
      if (payload != null) {
        setter.set(carrier, name, payload.toString());
      }
    }

    @Override
    public <C> Context extract(Context context, C carrier, Getter<C> getter) {
      String payload = getter.get(carrier, name);
      if (payload != null) {
        context = context.withValue(key, payload);
      }

      return context;
    }
  }

  private static final class MapSetter implements HttpTextFormat.Setter<Map<String, String>> {
    private static final MapSetter INSTANCE = new MapSetter();

    @Override
    public void set(Map<String, String> map, String key, String value) {
      map.put(key, value);
    }

    private MapSetter() {}
  }

  private static final class MapGetter implements HttpTextFormat.Getter<Map<String, String>> {
    private static final MapGetter INSTANCE = new MapGetter();

    @Override
    public String get(Map<String, String> map, String key) {
      return map.get(key);
    }

    private MapGetter() {}
  }
}
