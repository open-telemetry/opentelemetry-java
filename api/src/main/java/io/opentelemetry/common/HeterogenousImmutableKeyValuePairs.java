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

package io.opentelemetry.common;

import static io.opentelemetry.internal.Utils.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable set of key-value pairs. Keys are only {@link String} typed. Can be iterated over
 * using the {@link #forEach(AttributeConsumer)} method. The type of the value is stored along with
 * the key, for reference when reading the data back out.
 *
 * <p>Key-value pairs are dropped for {@code null} or empty keys.
 *
 * @see CleanAttributes
 */
@Immutable
abstract class HeterogenousImmutableKeyValuePairs implements CleanReadableAttributes {

  List<Object> data() {
    return Collections.emptyList();
  }

  @Override
  public int size() {
    return data().size() / 3;
  }

  @Override
  public boolean isEmpty() {
    return data().isEmpty();
  }

  @Override
  public void forEach(AttributeConsumer consumer) {
    for (int i = 0; i < data().size(); i += 3) {
      consumer.consume(
          (String) data().get(i), (AttributeType) data().get(i + 1), data().get(i + 2));
    }
  }

  @Override
  @Nullable
  public Object get(String key) {
    for (int i = 0; i < data().size(); i += 3) {
      if (key.equals(data().get(i))) {
        return data().get(i + 2);
      }
    }
    return null;
  }

  static List<Object> sortAndFilter(Object[] data) {
    checkArgument(data.length % 3 == 0, "You must provide key/type/value triples.");

    quickSort(data, 0, data.length - 3);
    return dedupe(data);
  }

  private static void quickSort(Object[] data, int leftIndex, int rightIndex) {
    if (leftIndex >= rightIndex) {
      return;
    }

    String pivotKey = data[rightIndex] == null ? "" : (String) data[rightIndex];
    int counter = leftIndex;

    for (int i = leftIndex; i <= rightIndex; i += 3) {
      String key = data[i] == null ? "" : (String) data[i];
      if (key.compareTo(pivotKey) <= 0) {
        swap(data, counter, i);
        counter += 3;
      }
    }

    quickSort(data, leftIndex, counter - 6);
    quickSort(data, counter, rightIndex);
  }

  private static List<Object> dedupe(Object[] data) {
    List<Object> result = new ArrayList<>(data.length);
    Object previousKey = null;

    for (int i = 0; i < data.length; i += 3) {
      Object key = data[i];
      Object type = data[i + 1];
      Object value = data[i + 2];
      if (key == null || "".equals(key)) {
        continue;
      }
      if (key.equals(previousKey)) {
        continue;
      }
      previousKey = key;
      result.add(key);
      result.add(type);
      result.add(value);
    }
    return result;
  }

  private static void swap(Object[] data, int a, int b) {
    Object keyA = data[a];
    Object typeA = data[a + 1];
    Object valueA = data[a + 2];
    data[a] = data[b];
    data[a + 1] = data[b + 1];
    data[a + 2] = data[b + 2];

    data[b] = keyA;
    data[b + 1] = typeA;
    data[b + 2] = valueA;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("{");
    forEach(
        new AttributeConsumer() {
          @Override
          public void consume(String key, AttributeType type, Object value) {
            sb.append(key)
                .append("=")
                .append("(")
                .append(type)
                .append(")")
                .append(value)
                .append(", ");
          }
        });
    // get rid of that last pesky comma
    if (sb.length() > 1) {
      sb.setLength(sb.length() - 2);
    }
    sb.append("}");
    return sb.toString();
  }
}
