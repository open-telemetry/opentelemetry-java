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
@SuppressWarnings("unchecked")
@Immutable
public abstract class CleanAttributes extends HeterogenousImmutableKeyValuePairs
    implements CleanReadableAttributes {
  private static final CleanAttributes EMPTY = CleanAttributes.newBuilder().build();

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes extends CleanAttributes {
    ArrayBackedAttributes() {}

    @Override
    abstract List<Object> data();
  }

  @Override
  public Boolean getBooleanValue(Object value) {
    return (Boolean) value;
  }

  @Override
  public String getStringValue(Object value) {
    return (String) value;
  }

  @Override
  public Double getDoubleValue(Object value) {
    return (Double) value;
  }

  @Override
  public Long getLongValue(Object value) {
    return (Long) value;
  }

  @Override
  public List<Boolean> getBooleanArrayValue(Object value) {
    return (List<Boolean>) value;
  }

  @Override
  public List<String> getStringArrayValue(Object value) {
    return (List<String>) value;
  }

  @Override
  public List<Double> getDoubleArrayValue(Object value) {
    return (List<Double>) value;
  }

  @Override
  public List<Long> getLongArrayValue(Object value) {
    return (List<Long>) value;
  }

  @Override
  public void forEach(AttributeConsumer consumer) {
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 3) {
      consumer.consume(
          (String) data.get(i), (AttributeValue.Type) data.get(i + 1), data.get(i + 2));
    }
  }

  /** Returns a {@link CleanAttributes} instance with no attributes. */
  public static CleanAttributes empty() {
    return EMPTY;
  }

  private static CleanAttributes sortAndFilterToAttributes(Object... data) {
    return new AutoValue_CleanAttributes_ArrayBackedAttributes(sortAndFilter(data));
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
    public Builder setAttribute(String key, String value) {
      data.add(key);
      data.add(AttributeValue.Type.STRING);
      data.add(value);
      return this;
    }

    /**
     * Sets a long {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, long value) {
      data.add(key);
      data.add(AttributeValue.Type.LONG);
      data.add(value);
      return this;
    }

    /**
     * Sets a double {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, double value) {
      data.add(key);
      data.add(AttributeValue.Type.DOUBLE);
      data.add(value);
      return this;
    }

    /**
     * Sets a boolean {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, boolean value) {
      data.add(key);
      data.add(AttributeValue.Type.BOOLEAN);
      data.add(value);
      return this;
    }

    /**
     * Sets a String array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, String... value) {
      data.add(key);
      data.add(AttributeValue.Type.STRING_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }

    /**
     * Sets a Long array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Long... value) {
      data.add(key);
      data.add(AttributeValue.Type.LONG_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }

    /**
     * Sets a Double array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Double... value) {
      data.add(key);
      data.add(AttributeValue.Type.DOUBLE_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }

    /**
     * Sets a Boolean array {@link AttributeValue} into this.
     *
     * @return this Builder
     */
    public Builder setAttribute(String key, Boolean... value) {
      data.add(key);
      data.add(AttributeValue.Type.BOOLEAN_ARRAY);
      data.add(Arrays.asList(value));
      return this;
    }
  }
}
