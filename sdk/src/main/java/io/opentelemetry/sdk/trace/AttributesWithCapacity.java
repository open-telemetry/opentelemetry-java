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
import java.util.LinkedHashMap;
import java.util.Map;

// A map implementation with a fixed capacity that drops events when the map gets full. Eviction
// is based on the access order.
final class AttributesWithCapacity extends LinkedHashMap<String, AttributeValue> {

  private final long capacity;
  private int totalRecordedAttributes = 0;
  // Here because -Werror complains about this: [serial] serializable class AttributesWithCapacity
  // has no definition of serialVersionUID. This class shouldn't be serialized.
  private static final long serialVersionUID = 42L;

  AttributesWithCapacity(long capacity) {
    // Capacity of the map is capacity + 1 to avoid resizing because removeEldestEntry is invoked
    // by put and putAll after inserting a new entry into the map. The loadFactor is set to 1
    // to avoid resizing because. The accessOrder is set to true.
    super((int) capacity + 1, 1, /*accessOrder=*/ true);
    this.capacity = capacity;
  }

  // Users must call this method instead of put to keep count of the total number of entries
  // inserted.
  void putAttribute(String key, AttributeValue value) {
    totalRecordedAttributes += 1;
    put(key, value);
  }

  void putAllAttributes(Map<String, AttributeValue> m) {
    for (Map.Entry<String, AttributeValue> entry : m.entrySet()) {
      putAttribute(entry.getKey(), entry.getValue());
    }
  }

  int getNumberOfDroppedAttributes() {
    return totalRecordedAttributes - size();
  }

  // It is called after each put or putAll call in order to determine if the eldest inserted
  // entry should be removed or not.
  @Override
  protected boolean removeEldestEntry(Map.Entry<String, AttributeValue> eldest) {
    return size() > this.capacity;
  }
}
