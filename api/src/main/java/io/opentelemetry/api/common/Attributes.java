/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for attributes.
 *
 * <p>The keys are {@link AttributeKey}s and the values are Object instances that match the type of
 * the provided key.
 *
 * <p>Null keys will be silently dropped.
 *
 * <p>Note: The behavior of null-valued attributes is undefined, and hence strongly discouraged.
 */
@SuppressWarnings("rawtypes")
@Immutable
public abstract class Attributes extends ImmutableKeyValuePairs<AttributeKey, Object>
    implements ReadableAttributes {
  private static final Attributes EMPTY = Attributes.builder().build();

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes extends Attributes {
    ArrayBackedAttributes() {}

    @Override
    protected abstract List<Object> data();

    @Override
    public Builder toBuilder() {
      return new Builder(new ArrayList<>(data()));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(AttributeKey<T> key) {
    return (T) super.get(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void forEach(AttributeConsumer consumer) {
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      consumer.accept((AttributeKey) data.get(i), data.get(i + 1));
    }
  }

  /** Returns a {@link Attributes} instance with no attributes. */
  public static Attributes empty() {
    return EMPTY;
  }

  /** Returns a {@link Attributes} instance with a single key-value pair. */
  public static <T> Attributes of(AttributeKey<T> key, T value) {
    return sortAndFilterToAttributes(key, value);
  }

  /**
   * Returns a {@link Attributes} instance with two key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static <T, U> Attributes of(
      AttributeKey<T> key1, T value1, AttributeKey<U> key2, U value2) {
    return sortAndFilterToAttributes(key1, value1, key2, value2);
  }

  /**
   * Returns a {@link Attributes} instance with three key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static <T, U, V> Attributes of(
      AttributeKey<T> key1,
      T value1,
      AttributeKey<U> key2,
      U value2,
      AttributeKey<V> key3,
      V value3) {
    return sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3);
  }

  /**
   * Returns a {@link Attributes} instance with four key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static <T, U, V, W> Attributes of(
      AttributeKey<T> key1,
      T value1,
      AttributeKey<U> key2,
      U value2,
      AttributeKey<V> key3,
      V value3,
      AttributeKey<W> key4,
      W value4) {
    return sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * Returns a {@link Attributes} instance with five key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static <T, U, V, W, X> Attributes of(
      AttributeKey<T> key1,
      T value1,
      AttributeKey<U> key2,
      U value2,
      AttributeKey<V> key3,
      V value3,
      AttributeKey<W> key4,
      W value4,
      AttributeKey<X> key5,
      X value5) {
    return sortAndFilterToAttributes(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5);
  }

  /**
   * Returns a {@link Attributes} instance with the given key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static <T, U, V, W, X, Y> Attributes of(
      AttributeKey<T> key1,
      T value1,
      AttributeKey<U> key2,
      U value2,
      AttributeKey<V> key3,
      V value3,
      AttributeKey<W> key4,
      W value4,
      AttributeKey<X> key5,
      X value5,
      AttributeKey<Y> key6,
      Y value6) {
    return sortAndFilterToAttributes(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5,
        key6, value6);
  }

  private static Attributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      AttributeKey<?> key = (AttributeKey<?>) data[i];
      if (key != null && (key.getKey() == null || "".equals(key.getKey()))) {
        data[i] = null;
      }
    }
    return new AutoValue_Attributes_ArrayBackedAttributes(
        sortAndFilter(data, /* filterNullValues= */ true));
  }

  /** Returns a new {@link Builder} instance for creating arbitrary {@link Attributes}. */
  public static Builder builder() {
    return new Builder();
  }

  /** Returns a new {@link Builder} instance from ReadableAttributes. */
  public static Builder builder(ReadableAttributes attributes) {
    final Builder builder = new Builder();
    attributes.forEach(builder::put);
    return builder;
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

    /** Puts a {@link AttributeKey} with associated value into this. */
    public <T> Builder put(AttributeKey<Long> key, int value) {
      return put(key, (long) value);
    }

    /** Puts a {@link AttributeKey} with associated value into this. */
    public <T> Builder put(AttributeKey<T> key, T value) {
      if (key == null || key.getKey() == null || key.getKey().length() == 0 || value == null) {
        return this;
      }
      data.add(key);
      data.add(value);
      return this;
    }

    /**
     * Puts a String attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, String value) {
      return put(stringKey(key), value);
    }

    /**
     * Puts a long attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, long value) {
      return put(longKey(key), value);
    }

    /**
     * Puts a double attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, double value) {
      return put(doubleKey(key), value);
    }

    /**
     * Puts a boolean attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, boolean value) {
      return put(booleanKey(key), value);
    }

    /**
     * Puts a String array attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, String... value) {
      return put(stringArrayKey(key), value == null ? null : Arrays.asList(value));
    }

    /**
     * Puts a Long array attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, Long... value) {
      return put(longArrayKey(key), value == null ? null : Arrays.asList(value));
    }

    /**
     * Puts a Double array attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, Double... value) {
      return put(doubleArrayKey(key), value == null ? null : Arrays.asList(value));
    }

    /**
     * Puts a Boolean array attribute into this.
     *
     * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @return this Builder
     */
    public Builder put(String key, Boolean... value) {
      return put(booleanArrayKey(key), value == null ? null : Arrays.asList(value));
    }

    /**
     * Puts all the provided attributes into this Builder.
     *
     * @return this Builder
     */
    public Builder putAll(Attributes attributes) {
      data.addAll(attributes.data());
      return this;
    }
  }
}
