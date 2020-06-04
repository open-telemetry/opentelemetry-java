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
import static io.opentelemetry.internal.Utils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable set of key-value pairs. Keys are only {@link String} typed. Can be iterated over
 * using the {@link #forEach(KeyValueConsumer)} method.
 *
 * @param <V> The type of the values contained in this.
 * @see Labels
 * @see Attributes
 */
@Immutable
public interface ImmutableKeyValuePairs<V> {
  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(KeyValueConsumer<V> consumer);

  /**
   * Used for iterating over the key-value pairs contained by an {@link ImmutableKeyValuePairs}
   * instance.
   */
  interface KeyValueConsumer<T> {
    void consume(String key, T value);
  }

  class Helper {
    private Helper() {}

    static <T> List<Object> sortAndFilter(Object[] data) {
      checkArgument(
          data.length % 2 == 0, "You must provide an even number of key/value pair arguments.");

      quickSort(data, 0, data.length - 2);
      return Arrays.asList(dedupe(data));
    }

    private static void quickSort(Object[] data, int leftIndex, int rightIndex) {
      if (leftIndex >= rightIndex) {
        return;
      }

      String pivotKey = (String) data[rightIndex];
      int counter = leftIndex;

      for (int i = leftIndex; i <= rightIndex; i += 2) {
        if (((String) data[i]).compareTo(pivotKey) <= 0) {
          swap(data, counter, i);
          counter += 2;
        }
      }

      quickSort(data, leftIndex, counter - 4);
      quickSort(data, counter, rightIndex);
    }

    private static Object[] dedupe(Object[] data) {
      List<Object> result = new ArrayList<>(data.length);
      Object previousKey = null;

      for (int i = 0; i < data.length; i += 2) {
        Object key = data[i];
        Object value = data[i + 1];
        checkNotNull(key, "You cannot provide null keys for creation of attributes.");
        if (key.equals(previousKey)) {
          continue;
        }
        previousKey = key;
        result.add(key);
        result.add(value);
      }
      return result.toArray();
    }

    private static void swap(Object[] data, int a, int b) {
      Object keyA = data[a];
      Object valueA = data[a + 1];
      data[a] = data[b];
      data[a + 1] = data[b + 1];

      data[b] = keyA;
      data[b + 1] = valueA;
    }
  }
}
