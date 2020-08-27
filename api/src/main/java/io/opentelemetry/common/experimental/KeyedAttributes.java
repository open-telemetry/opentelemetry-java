/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.common.experimental;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** javadoc me. */
public class KeyedAttributes implements ReadableKeyedAttributes {

  // todo replace with the array-backed impl
  private final ConcurrentMap<Key, Object> values;

  private KeyedAttributes(ConcurrentMap<Key, Object> values) {
    this.values = values;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void forEach(AttributeConsumer attributeConsumer) {
    Set<Map.Entry<Key, Object>> entries = values.entrySet();
    for (Map.Entry<Key, Object> entry : entries) {
      if (entry.getKey() instanceof StringKey) {
        attributeConsumer.consume((StringKey) entry.getKey(), (String) entry.getValue());
      } else if (entry.getKey() instanceof BooleanKey) {
        attributeConsumer.consume((BooleanKey) entry.getKey(), (boolean) entry.getValue());
      } else if (entry.getKey() instanceof StringArrayKey) {
        attributeConsumer.consume((StringArrayKey) entry.getKey(), (List<String>) entry.getValue());
      }
    }
  }

  public static Builder newBuilder() {
    return new BuilderImpl();
  }

  public static StringKey stringKey(String key) {
    return new StringKey(key);
  }

  public static BooleanKey booleanKey(String key) {
    return new BooleanKey(key);
  }

  public static StringArrayKey stringArrayKey(String key) {
    return new StringArrayKey(key);
  }

  interface Key {
    String get();
  }

  interface Builder {
    Builder set(StringKey key, String value);

    Builder set(StringArrayKey key, String... value);

    Builder set(BooleanKey key, boolean value);

    KeyedAttributes build();
  }

  private abstract static class KeyImpl implements Key {
    private final String key;

    protected KeyImpl(String key) {
      this.key = key;
    }

    @Override
    public final String get() {
      return key;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{'" + key + '\'' + '}';
    }
  }

  public static class StringKey extends KeyImpl {

    private StringKey(String key) {
      super(key);
    }
  }

  public static class StringArrayKey extends KeyImpl {

    private StringArrayKey(String key) {
      super(key);
    }
  }

  public static class BooleanKey extends KeyImpl {

    private BooleanKey(String key) {
      super(key);
    }
  }

  private static class BuilderImpl implements Builder {
    // todo: replace with object array thing
    private final Map<Key, Object> values = new HashMap<>();

    @Override
    public Builder set(StringKey key, String value) {
      values.put(key, value);
      return this;
    }

    @Override
    public Builder set(StringArrayKey key, String... value) {
      values.put(key, Arrays.asList(value));
      return this;
    }

    @Override
    public Builder set(BooleanKey key, boolean value) {
      values.put(key, value);
      return this;
    }

    @Override
    public KeyedAttributes build() {
      return new KeyedAttributes(new ConcurrentHashMap<>(values));
    }
  }
}
