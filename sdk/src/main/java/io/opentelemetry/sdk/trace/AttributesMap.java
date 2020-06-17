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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.ReadableAttributes;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A map implementation with a fixed capacity that drops attributes when the map gets full.
 *
 * <p>Some APIs may have slightly different behaviors, like `put` which returns null if out of
 * capacity.
 */
final class AttributesMap extends HashMap<String, AttributeValue> implements ReadableAttributes {

  private final long capacity;
  private int totalAddedValues = 0;
  // Here because -Werror complains about this: [serial] serializable class AttributesWithCapacity
  // has no definition of serialVersionUID. This class shouldn't be serialized.
  private static final long serialVersionUID = 42L;

  AttributesMap(long capacity) {
    this.capacity = capacity;
  }

  @Nullable
  @Override
  public AttributeValue put(String key, AttributeValue value) {
    totalAddedValues++;
    if (size() >= capacity && !containsKey(key)) {
      return null;
    }
    return super.put(key, value);
  }

  @Override
  public void putAll(Map<? extends String, ? extends AttributeValue> values) {
    for (Map.Entry<? extends String, ? extends AttributeValue> entry : values.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Nullable
  @Override
  public AttributeValue remove(Object key) {
    return super.remove(key);
  }

  // Added as public to override the newly added methods in java8, so when we do the switch and
  // start using them we remember to fix them and count the number of attributes added.

  @SuppressWarnings("MissingOverride")
  public AttributeValue putIfAbsent(String key, AttributeValue value) {
    throw new UnsupportedOperationException("Do not call methods on the map");
  }

  @SuppressWarnings("MissingOverride")
  public AttributeValue replace(String key, AttributeValue value) {
    throw new UnsupportedOperationException("Do not call methods on the map");
  }

  @SuppressWarnings("MissingOverride")
  public boolean replace(String key, AttributeValue oldValue, AttributeValue newValue) {
    throw new UnsupportedOperationException("Do not call methods on the map");
  }

  int getTotalAddedValues() {
    return totalAddedValues;
  }

  @Override
  public void forEach(KeyValueConsumer<AttributeValue> consumer) {
    for (Entry<String, AttributeValue> entry : entrySet()) {
      consumer.consume(entry.getKey(), entry.getValue());
    }
  }

  @Nullable
  @Override
  public AttributeValue get(String key) {
    return super.get(key);
  }
}
