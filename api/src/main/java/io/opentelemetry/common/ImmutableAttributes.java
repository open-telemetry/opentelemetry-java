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
public abstract class ImmutableAttributes extends ImmutableKeyValuePairs<AttributeValue>
    implements Attributes {
  private static final ImmutableAttributes EMPTY = ImmutableAttributes.newBuilder().build();

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes extends ImmutableAttributes {
    ArrayBackedAttributes() {}

    @Override
    abstract List<Object> data();
  }

  /** Returns a {@link ImmutableAttributes} instance with no attributes. */
  public static ImmutableAttributes empty() {
    return EMPTY;
  }

  /** Returns a {@link ImmutableAttributes} instance with a single key-value pair. */
  public static ImmutableAttributes of(String key, AttributeValue value) {
    return sortAndFilterToAttributes(key, value);
  }

  /**
   * Returns a {@link ImmutableAttributes} instance with two key-value pairs. Order of the keys is
   * not preserved. Duplicate keys will be removed.
   */
  public static ImmutableAttributes of(
      String key1, AttributeValue value1, String key2, AttributeValue value2) {
    return sortAndFilterToAttributes(key1, value1, key2, value2);
  }

  /**
   * Returns a {@link ImmutableAttributes} instance with three key-value pairs. Order of the keys is
   * not preserved. Duplicate keys will be removed.
   */
  public static ImmutableAttributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3) {
    return sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3);
  }

  /**
   * Returns a {@link ImmutableAttributes} instance with four key-value pairs. Order of the keys is
   * not preserved. Duplicate keys will be removed.
   */
  public static ImmutableAttributes of(
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
   * Returns a {@link ImmutableAttributes} instance with five key-value pairs. Order of the keys is
   * not preserved. Duplicate keys will be removed.
   */
  public static ImmutableAttributes of(
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

  private static ImmutableAttributes sortAndFilterToAttributes(Object... data) {
    return new AutoValue_ImmutableAttributes_ArrayBackedAttributes(sortAndFilter(data));
  }

  /** Creates a new {@link Builder} instance for creating arbitrary {@link ImmutableAttributes}. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Enables the creation of an {@link ImmutableAttributes} instance with an arbitrary number of
   * key-value pairs.
   */
  public static class Builder {
    private final List<Object> data = new ArrayList<>();

    /** Create the {@link ImmutableAttributes} from this. */
    public ImmutableAttributes build() {
      return sortAndFilterToAttributes(data.toArray());
    }

    /**
     * Sets a bare {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, AttributeValue value) {
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
      data.add(key);
      data.add(stringAttributeValue(value));
      return this;
    }

    /**
     * Sets a long {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, long value) {
      data.add(key);
      data.add(longAttributeValue(value));
      return this;
    }

    /**
     * Sets a double {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, double value) {
      data.add(key);
      data.add(doubleAttributeValue(value));
      return this;
    }

    /**
     * Sets a boolean {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, boolean value) {
      data.add(key);
      data.add(booleanAttributeValue(value));
      return this;
    }

    /**
     * Sets a String array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, String... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }

    /**
     * Sets a Long array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Long... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }

    /**
     * Sets a Double array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Double... value) {
      data.add(key);
      data.add(arrayAttributeValue(value));
      return this;
    }

    /**
     * Sets a Boolean array {@link AttributeValue} into this.
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
