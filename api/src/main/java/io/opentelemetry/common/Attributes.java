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

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for attributes. The type parameter denotes the type of the values of the
 * attributes.
 */
@Immutable
public abstract class Attributes<T> {
  private static final Attributes<Object> EMPTY =
      new Attributes<Object>() {
        @Override
        public void forEach(AttributeConsumer<Object> consumer) {
          // no-op
        }
      };

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  public abstract void forEach(AttributeConsumer<T> consumer);

  private static <T> Attributes<T> sortAndFilter(Object... data) {
    return sortAndFilter(/* checkType= */ false, null, data);
  }

  @SuppressWarnings("unchecked")
  private static <T> Attributes<T> sortAndFilter(boolean checkType, Class<T> type, Object[] data) {
    checkArgument(
        data.length % 2 == 0, "You must provide an even number of key/value pair arguments.");
    String errorMessage = "All values must be of type: " + type;

    // note: this is possibly not the most memory-efficient possible implementation, but it works
    // for starters.
    TreeMap<String, T> sorter = new TreeMap<>();
    for (int i = 0; i < data.length; i++) {
      String key = (String) data[i++];
      Object value = data[i];

      if (checkType) {
        checkNotNull(key, "You cannot provide null keys for creation of attributes.");
        checkArgument(type.isAssignableFrom(value.getClass()), errorMessage);
      }

      // todo: skip here, favoring the first, or use the TreeMap's built in replacement to favor the
      // last? Or, final option, disallow duplicate keys and throw an exception like guava's
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
    return new AutoValue_Attributes_ArrayBackedAttributes<>(sortedData);
  }

  /** An {@link Attributes} instance with no attributes. */
  @SuppressWarnings("unchecked")
  public static <T> Attributes<T> empty() {
    return (Attributes<T>) EMPTY;
  }

  /** An {@link Attributes} instance with a single key-value pair. */
  public static <T> Attributes<T> of(String key, T value) {
    return sortAndFilter(key, value);
  }

  /**
   * An {@link Attributes} instance with two key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static <T> Attributes<T> of(String key1, T value1, String key2, T value2) {
    return sortAndFilter(key1, value1, key2, value2);
  }

  /**
   * An {@link Attributes} instance with three key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static <T> Attributes<T> of(
      String key1, T value1, String key2, T value2, String key3, T value3) {
    return sortAndFilter(key1, value1, key2, value2, key3, value3);
  }

  /**
   * An {@link Attributes} instance with four key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static <T> Attributes<T> of(
      String key1, T value1, String key2, T value2, String key3, T value3, String key4, T value4) {
    return sortAndFilter(key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * An {@link Attributes} instance with five key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static <T> Attributes<T> of(
      String key1,
      T value1,
      String key2,
      T value2,
      String key3,
      T value3,
      String key4,
      T value4,
      String key5,
      T value5) {
    return sortAndFilter(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5);
  }

  /**
   * Create an {@link Attributes} instance with values of the given type.
   *
   * <p>The data MUST be in alternating key,value format, and the values must be of the specified
   * type.
   *
   * @param valueType The type of the values contained in these Attributes.
   * @param data The array of data, alternating key, value paris.
   * @return A new instance.
   * @throws IllegalArgumentException if any of the requirements are violated.
   */
  public static <T> Attributes<T> of(Class<T> valueType, Object... data) {
    return sortAndFilter(/* checkType= */ true, valueType, data);
  }

  /** Creates a new {@link Builder} instance for creating arbitrary {@link Attributes}. */
  public static <T> Builder<T> newBuilder() {
    return new Builder<>();
  }

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes<T> extends Attributes<T> {
    abstract List<Object> data();

    ArrayBackedAttributes() {}

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(AttributeConsumer<T> consumer) {
      for (int i = 0; i < data().size(); i++) {
        consumer.consume((String) data().get(i), (T) data().get(++i));
      }
    }
  }

  /**
   * Enables the creation of an {@link Attributes} instance with an arbitrary number of key-value
   * pairs.
   */
  public static class Builder<T> {
    private final List<Object> data = new ArrayList<>();

    /** javadoc me. */
    public Attributes<T> build() {
      return sortAndFilter(data.toArray());
    }

    /** javadoc me. */
    public Builder<T> addAttribute(String key, T value) {
      data.add(key);
      data.add(value);
      return this;
    }
  }

  /** Used for iterating over the key-value pairs contained by an {@link Attributes} instance. */
  public interface AttributeConsumer<T> {
    void consume(String key, T value);
  }
}
