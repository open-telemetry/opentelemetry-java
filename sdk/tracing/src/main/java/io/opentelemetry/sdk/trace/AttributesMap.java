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
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A map with a fixed capacity that drops attributes when the map gets full.
 *
 * <p>Note: this doesn't implement the Map interface, but behaves very similarly to one.
 */
final class AttributesMap implements ReadableAttributes {
  private final Map<String, AttributeValue> data = new HashMap<>();

  private final long capacity;
  private int totalAddedValues = 0;

  AttributesMap(long capacity) {
    this.capacity = capacity;
  }

  public void put(String key, AttributeValue value) {
    totalAddedValues++;
    if (data.size() >= capacity && !data.containsKey(key)) {
      return;
    }
    data.put(key, value);
  }

  void remove(String key) {
    data.remove(key);
  }

  int getTotalAddedValues() {
    return totalAddedValues;
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public void forEach(KeyValueConsumer<String, AttributeValue> consumer) {
    for (Map.Entry<String, AttributeValue> entry : data.entrySet()) {
      consumer.consume(entry.getKey(), entry.getValue());
    }
  }

  @Nullable
  @Override
  public AttributeValue get(String key) {
    return data.get(key);
  }

  @Override
  public String toString() {
    return "AttributesMap{"
        + "data="
        + data
        + ", capacity="
        + capacity
        + ", totalAddedValues="
        + totalAddedValues
        + '}';
  }

  ReadableAttributes immutableCopy() {
    Attributes.Builder builder = Attributes.newBuilder();
    for (Map.Entry<String, AttributeValue> entry : data.entrySet()) {
      builder.setAttribute(entry.getKey(), entry.getValue());
    }
    return builder.build();
  }
}
