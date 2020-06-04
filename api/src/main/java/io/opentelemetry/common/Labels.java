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

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/** An immutable container for labels, which are pairs of {@link String}. */
@Immutable
public abstract class Labels implements ImmutableKeyValuePairs<String> {
  private static final Labels EMPTY =
      new Labels() {
        @Override
        public void forEach(KeyValueConsumer<String> consumer) {
          // no-op
        }
      };

  /** An {@link Labels} instance with no attributes. */
  public static Labels empty() {
    return EMPTY;
  }

  /** An {@link Labels} instance with a single key-value pair. */
  public static Labels of(String key, String value) {
    return sortAndFilter(key, value);
  }

  /**
   * An {@link Labels} instance with two key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Labels of(String key1, String value1, String key2, String value2) {
    return sortAndFilter(key1, value1, key2, value2);
  }

  /**
   * An {@link Labels} instance with three key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Labels of(
      String key1, String value1, String key2, String value2, String key3, String value3) {
    return sortAndFilter(key1, value1, key2, value2, key3, value3);
  }

  /**
   * An {@link Labels} instance with four key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Labels of(
      String key1,
      String value1,
      String key2,
      String value2,
      String key3,
      String value3,
      String key4,
      String value4) {
    return sortAndFilter(key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * An {@link Labels} instance with five key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Labels of(
      String key1,
      String value1,
      String key2,
      String value2,
      String key3,
      String value3,
      String key4,
      String value4,
      String key5,
      String value5) {
    return sortAndFilter(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5);
  }

  /** Creates a new {@link Builder} instance for creating arbitrary {@link Labels}. */
  public static Builder newBuilder() {
    return new Builder();
  }

  private static Labels sortAndFilter(Object... data) {
    return new AutoValue_Labels_ArrayBackedLabels(Helper.sortAndFilter(data));
  }

  @AutoValue
  @Immutable
  abstract static class ArrayBackedLabels extends Labels {
    abstract List<Object> data();

    ArrayBackedLabels() {}

    @Override
    public void forEach(KeyValueConsumer<String> consumer) {
      for (int i = 0; i < data().size(); i += 2) {
        consumer.consume((String) data().get(i), (String) data().get(i + 1));
      }
    }
  }

  /**
   * Enables the creation of an {@link Labels} instance with an arbitrary number of key-value pairs.
   */
  public static class Builder {
    private final List<Object> data = new ArrayList<>();

    /** Create the {@link Labels} from this. */
    public Labels build() {
      return sortAndFilter(data.toArray());
    }

    /**
     * Add a single label to this Builder.
     *
     * @return this Builder
     */
    public Builder setLabel(String key, String value) {
      data.add(key);
      data.add(value);
      return this;
    }
  }
}
