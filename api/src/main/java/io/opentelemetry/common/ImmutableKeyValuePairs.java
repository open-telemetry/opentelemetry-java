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
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable set of key-value pairs. Can be iterated over using the {@link
 * #forEach(KeyValueConsumer)} method.
 *
 * @param <T> The type of the values contained in this.
 * @see Labels
 * @see Attributes
 */
@Immutable
public interface ImmutableKeyValuePairs<T> {
  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(KeyValueConsumer<T> consumer);

  /**
   * Used for iterating over the key-value pairs contained by an {@link ImmutableKeyValuePairs}
   * instance.
   */
  interface KeyValueConsumer<T> {
    void consume(String key, T value);
  }

  class Helper {
    private Helper() {}

    @SuppressWarnings("unchecked")
    static <T> List<Object> sortAndFilter(Object[] data) {
      checkArgument(
          data.length % 2 == 0, "You must provide an even number of key/value pair arguments.");

      // note: this is possibly not the most memory-efficient possible implementation, but it works
      // for starters.
      TreeMap<String, T> sorter = new TreeMap<>();
      for (int i = 0; i < data.length; i++) {
        String key = (String) data[i++];
        Object value = data[i];

        checkNotNull(key, "You cannot provide null keys for creation of attributes.");

        // todo: skip here, favoring the first, or use the TreeMap's built in replacement to favor
        // the last? Or, final option, disallow duplicate keys and throw an exception like guava's
        // ImmutableMap.
        if (!sorter.containsKey(key)) {
          sorter.put(key, (T) value);
        }
      }
      List<Object> sortedData = new ArrayList<>(sorter.size() * 2);
      for (Entry<String, T> entry : sorter.entrySet()) {
        sortedData.add(entry.getKey());
        sortedData.add(entry.getValue());
      }
      return sortedData;
    }
  }
}
