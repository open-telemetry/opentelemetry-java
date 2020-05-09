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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.truth.Truth;
import io.opentelemetry.context.Context;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for {@link DefaultContextPropagators}. */
// @RunWith(JUnit4.class)
public class DefaultPropagatorsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void addHttpTextFormatNull() {
    thrown.expect(NullPointerException.class);
    DefaultContextPropagators.builder().addHttpTextFormat(null);
  }

  @Test
  public void testInject() {
    CustomHttpTextFormat propagator1 = new CustomHttpTextFormat("prop1");
    CustomHttpTextFormat propagator2 = new CustomHttpTextFormat("prop2");
    ContextPropagators propagators =
        DefaultContextPropagators.builder()
            .addHttpTextFormat(propagator1)
            .addHttpTextFormat(propagator2)
            .build();

    Context context = Context.EMPTY;
    context = context.put(propagator1.getKey(), "value1");
    context = context.put(propagator2.getKey(), "value2");

    Map<String, String> map = new HashMap<>();
    propagators.getHttpTextFormat().inject(context, map, MapSetter.INSTANCE);
    assertThat(map.get(propagator1.getKeyName())).isEqualTo("value1");
    assertThat(map.get(propagator2.getKeyName())).isEqualTo("value2");
  }

  @Test
  public void testExtract() {
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
        propagators.getHttpTextFormat().extract(Context.EMPTY, map, MapGetter.INSTANCE);
    assertThat(context.get(propagator1.getKey())).isEqualTo("value1");
    assertThat(context.get(propagator2.getKey())).isEqualTo("value2");
    assertThat(context.get(propagator3.getKey())).isNull(); // Handle missing value.
  }

  @Test
  public void noopPropagator() {
    ContextPropagators propagators = DefaultContextPropagators.builder().build();

    Context context = Context.EMPTY;
    Map<String, String> map = new HashMap<>();
    propagators.getHttpTextFormat().inject(context, map, MapSetter.INSTANCE);
    assertThat(map).isEmpty();

    Truth.assertThat(propagators.getHttpTextFormat().extract(context, map, MapGetter.INSTANCE))
        .isSameInstanceAs(context);
  }

  class CustomHttpTextFormat implements HttpTextFormat {
    private final String name;
    private final Context.Key<String> key;

    public CustomHttpTextFormat(String name) {
      this.name = name;
      this.key = new Context.Key<>(name);
    }

    public Context.Key<String> getKey() {
      return key;
    }

    public String getKeyName() {
      return name;
    }

    @Override
    public List<String> fields() {
      return Collections.<String>singletonList(name);
    }

    @Override
    public <C> void inject(Context context, C carrier, Setter<C> setter) {
      Object payload = context.get(key);
      if (payload != null) {
        setter.set(carrier, name, payload.toString());
      }
    }

    @Override
    public <C> Context extract(Context context, C carrier, Getter<C> getter) {
      String payload = getter.get(carrier, name);
      if (payload != null) {
        context = context.put(key, payload);
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
