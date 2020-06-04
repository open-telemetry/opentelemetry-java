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

import static io.opentelemetry.common.AttributeValue.arrayAttributeValue;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;
import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for attributes. The type parameter denotes the type of the values of the
 * attributes.
 */
@Immutable
public abstract class Attributes implements ImmutableKeyValuePairs<AttributeValue> {
  private static final Attributes EMPTY =
      new Attributes() {
        @Override
        public void forEach(KeyValueConsumer<AttributeValue> consumer) {
          // no-op
        }
      };

  private static Attributes sortAndFilter(Object... data) {
    return new AutoValue_Attributes_ArrayBackedAttributes(Helper.sortAndFilter(data));
  }

  /** An {@link Attributes} instance with no attributes. */
  public static Attributes empty() {
    return EMPTY;
  }

  /** An {@link Attributes} instance with a single key-value pair. */
  public static Attributes of(String key, AttributeValue value) {
    return sortAndFilter(key, value);
  }

  /**
   * An {@link Attributes} instance with two key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Attributes of(
      String key1, AttributeValue value1, String key2, AttributeValue value2) {
    return sortAndFilter(key1, value1, key2, value2);
  }

  /**
   * An {@link Attributes} instance with three key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3) {
    return sortAndFilter(key1, value1, key2, value2, key3, value3);
  }

  /**
   * An {@link Attributes} instance with four key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3,
      String key4,
      AttributeValue value4) {
    return sortAndFilter(key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * An {@link Attributes} instance with five key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3,
      String key4,
      AttributeValue value4,
      String key5,
      AttributeValue value5) {
    return sortAndFilter(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5);
  }

  /** Creates a new {@link Builder} instance for creating arbitrary {@link Attributes}. */
  public static Builder newBuilder() {
    return new Builder();
  }

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes extends Attributes {
    abstract List<Object> data();

    ArrayBackedAttributes() {}

    @Override
    public void forEach(KeyValueConsumer<AttributeValue> consumer) {
      for (int i = 0; i < data().size(); i++) {
        consumer.consume((String) data().get(i), (AttributeValue) data().get(++i));
      }
    }
  }

  /**
   * Enables the creation of an {@link Attributes} instance with an arbitrary number of key-value
   * pairs.
   */
  public static class Builder {
    private final List<Object> data = new ArrayList<>();

    /** Create the {@link Attributes} from this. */
    public Attributes build() {
      return sortAndFilter(data.toArray());
    }

    /**
     * Add a bare {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, AttributeValue value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /**
     * Add a String {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, String value) {
      data.add(key);
      data.add(stringAttributeValue(value));
      return this;
    }

    /**
     * Add a long {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, long value) {
      data.add(key);
      data.add(longAttributeValue(value));
      return this;
    }

    /**
     * Add a double {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, double value) {
      data.add(key);
      data.add(doubleAttributeValue(value));
      return this;
    }

    /**
     * Add a boolean {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, boolean value) {
      data.add(key);
      data.add(booleanAttributeValue(value));
      return this;
    }

    /**
     * Add a String array {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, String... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }

    /**
     * Add a Long array {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Long... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }

    /**
     * Add a Double array {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Double... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }

    /**
     * Add a Boolean array {@link AttributeValue} to this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Boolean... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }
  }
}
