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
 * An immutable container for attributes.
 *
 * <p>The keys are {@link String}s and the values are {@link AttributeValue} instances.
 */
@Immutable
public abstract class Attributes extends ImmutableKeyValuePairs<AttributeValue>
    implements ReadableAttributes {
  private static final Attributes EMPTY = Attributes.newBuilder().build();

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes extends Attributes {
    ArrayBackedAttributes() {}

    @Override
    abstract List<Object> data();

    @Override
    public Builder toBuilder() {
      return new Builder(new ArrayList<>(data()));
    }
  }

  /** Returns a {@link Attributes} instance with no attributes. */
  public static Attributes empty() {
    return EMPTY;
  }

  /** Returns a {@link Attributes} instance with a single key-value pair. */
  public static Attributes of(String key, AttributeValue value) {
    return sortAndFilterToAttributes(key, value);
  }

  /**
   * Returns a {@link Attributes} instance with two key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static Attributes of(
      String key1, AttributeValue value1, String key2, AttributeValue value2) {
    return sortAndFilterToAttributes(key1, value1, key2, value2);
  }

  /**
   * Returns a {@link Attributes} instance with three key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3) {
    return sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3);
  }

  /**
   * Returns a {@link Attributes} instance with four key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
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
    return sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * Returns a {@link Attributes} instance with five key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
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
    return sortAndFilterToAttributes(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5);
  }

  private static Attributes sortAndFilterToAttributes(Object... data) {
    return new AutoValue_Attributes_ArrayBackedAttributes(sortAndFilter(data));
  }

  /** Returns a new {@link Builder} instance for creating arbitrary {@link Attributes}. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Returns a new {@link Builder} instance populated with the data of this {@link Attributes}. */
  public abstract Builder toBuilder();

  /**
   * Enables the creation of an {@link Attributes} instance with an arbitrary number of key-value
   * pairs.
   */
  public static class Builder {
    private final List<Object> data;

    private Builder() {
      data = new ArrayList<>();
    }

    private Builder(List<Object> data) {
      this.data = data;
    }

    /** Create the {@link Attributes} from this. */
    public Attributes build() {
      return sortAndFilterToAttributes(data.toArray());
    }

    private boolean doNotAdd(String key, AttributeValue value) {
      if (key == null || key.length() == 0) {
        return true;
      }
      if (value == null || value.isEmpty()) {
        int index = data.indexOf(key);
        if (index == -1) {
          return true;
        }
        // Remove key/value pair
        data.remove(index);
        data.remove(index);
        return true;
      }
      return false;
    }

    /**
     * Sets a bare {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, AttributeValue value) {
      if (doNotAdd(key, value)) {
        return this;
      }
      data.add(key);
      data.add(value);
      return this;
    }

    /**
     * Sets a String {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, String value) {
      AttributeValue v = stringAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a long {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, long value) {
      AttributeValue v = longAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a double {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, double value) {
      AttributeValue v = doubleAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a boolean {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, boolean value) {
      AttributeValue v = booleanAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a String array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, String... value) {
      AttributeValue v = arrayAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a Long array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Long... value) {
      AttributeValue v = arrayAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a Double array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Double... value) {
      AttributeValue v = arrayAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }

    /**
     * Sets a Boolean array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Boolean... value) {
      AttributeValue v = arrayAttributeValue(value);
      if (doNotAdd(key, v)) {
        return this;
      }
      data.add(key);
      data.add(v);
      return this;
    }
  }
}
