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
import java.util.Arrays;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for attributes. Holds keys, values and the types of the values, as there
 * is a limited set of types that are allowable.
 */
@Immutable
@AutoValue
public abstract class CleanAttributes extends HeterogenousImmutableKeyValuePairs
    implements CleanReadableAttributes {
  private static final CleanAttributes EMPTY = CleanAttributes.newBuilder().build();

  @Override
  abstract List<Object> data();

  /** Returns a {@link CleanAttributes} instance with no attributes. */
  public static CleanAttributes empty() {
    return EMPTY;
  }

  private static CleanAttributes sortAndFilterToAttributes(Object... data) {
    return new AutoValue_CleanAttributes(sortAndFilter(data));
  }

  /** Creates a new {@link Builder} instance for creating arbitrary {@link CleanAttributes}. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Enables the creation of an {@link CleanAttributes} instance with an arbitrary number of
   * key-value pairs.
   */
  public static class Builder {
    private final List<Object> data = new ArrayList<>();

    /** Create the {@link CleanAttributes} from this. */
    public CleanAttributes build() {
      return sortAndFilterToAttributes(data.toArray());
    }

    /**
     * Sets a String {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setString(String key, String value) {
      data.add(key);
      data.add(AttributeType.STRING);
      data.add(value);
      return this;
    }

    /**
     * Sets a long {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setLong(String key, long value) {
      data.add(key);
      data.add(AttributeType.LONG);
      data.add(value);
      return this;
    }

    /**
     * Sets a double {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setDouble(String key, double value) {
      data.add(key);
      data.add(AttributeType.DOUBLE);
      data.add(value);
      return this;
    }

    /**
     * Sets a boolean {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setBoolean(String key, boolean value) {
      data.add(key);
      data.add(AttributeType.BOOLEAN);
      data.add(value);
      return this;
    }

    /**
     * Sets a String array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setStringArray(String key, String... value) {
      data.add(key);
      data.add(AttributeType.STRING_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }

    /**
     * Sets a Long array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setLongArray(String key, Long... value) {
      data.add(key);
      data.add(AttributeType.LONG_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }

    /**
     * Sets a Double array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setDoubleArray(String key, Double... value) {
      data.add(key);
      data.add(AttributeType.DOUBLE_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }

    /**
     * Sets a Boolean array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setBooleanArray(String key, Boolean... value) {
      data.add(key);
      data.add(AttributeType.BOOLEAN_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }
  }
}
