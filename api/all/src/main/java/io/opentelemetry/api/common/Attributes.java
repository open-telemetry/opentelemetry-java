/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.ArrayBackedAttributes.sortAndFilterToAttributes;

import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
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
 *
 * <p>Implementations of this interface *must* be immutable and have well-defined value-based
 * equals/hashCode implementations. If an implementation does not strictly conform to these
 * requirements, behavior of the OpenTelemetry APIs and default SDK cannot be guaranteed.
 *
 * <p>For this reason, it is strongly suggested that you use the implementation that is provided
 * here via the factory methods and the {@link AttributesBuilder}.
 */
@SuppressWarnings("rawtypes")
@Immutable
public interface Attributes {

  /**
   * Returns the value for the given {@link AttributeKey}, or {@code null} if not found.
   *
   * <p>Note: this method will automatically return the corresponding {@link
   * io.opentelemetry.api.common.Value} instance when passed a key of type {@link
   * AttributeType#VALUE} and a simple attribute is found. This is the inverse of {@link
   * AttributesBuilder#put(AttributeKey, Object)} when the key is {@link AttributeType#VALUE}.
   *
   * <ul>
   *   <li>If {@code put(AttributeKey.stringKey("key"), "a")} was called, then {@code
   *       get(AttributeKey.valueKey("key"))} returns {@code Value.of("a")}.
   *   <li>If {@code put(AttributeKey.longKey("key"), 1L)} was called, then {@code
   *       get(AttributeKey.valueKey("key"))} returns {@code Value.of(1L)}.
   *   <li>If {@code put(AttributeKey.doubleKey("key"), 1.0)} was called, then {@code
   *       get(AttributeKey.valueKey("key"))} returns {@code Value.of(1.0)}.
   *   <li>If {@code put(AttributeKey.booleanKey("key"), true)} was called, then {@code
   *       get(AttributeKey.valueKey("key"))} returns {@code Value.of(true)}.
   *   <li>If {@code put(AttributeKey.stringArrayKey("key"), Arrays.asList("a", "b"))} was called,
   *       then {@code get(AttributeKey.valueKey("key"))} returns {@code Value.of(Value.of("a"),
   *       Value.of("b"))}.
   *   <li>If {@code put(AttributeKey.longArrayKey("key"), Arrays.asList(1L, 2L))} was called, then
   *       {@code get(AttributeKey.valueKey("key"))} returns {@code Value.of(Value.of(1L),
   *       Value.of(2L))}.
   *   <li>If {@code put(AttributeKey.doubleArrayKey("key"), Arrays.asList(1.0, 2.0))} was called,
   *       then {@code get(AttributeKey.valueKey("key"))} returns {@code Value.of(Value.of(1.0),
   *       Value.of(2.0))}.
   *   <li>If {@code put(AttributeKey.booleanArrayKey("key"), Arrays.asList(true, false))} was
   *       called, then {@code get(AttributeKey.valueKey("key"))} returns {@code
   *       Value.of(Value.of(true), Value.of(false))}.
   * </ul>
   *
   * <p>Further, if {@code put(AttributeKey.valueKey("key"), Value.of(emptyList()))} was called,
   * then
   *
   * <ul>
   *   <li>{@code get(AttributeKey.stringArrayKey("key"))}
   *   <li>{@code get(AttributeKey.longArrayKey("key"))}
   *   <li>{@code get(AttributeKey.booleanArrayKey("key"))}
   *   <li>{@code get(AttributeKey.doubleArrayKey("key"))}
   * </ul>
   *
   * <p>all return an empty list (as opposed to {@code null}).
   */
  @Nullable
  <T> T get(AttributeKey<T> key);

  /**
   * Iterates over all the key-value pairs of attributes contained by this instance.
   *
   * <p>Note: {@link AttributeType#VALUE} attributes will be represented as simple attributes if
   * possible. See {@link AttributesBuilder#put(AttributeKey, Object)} for more details.
   */
  void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer);

  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /**
   * Returns a read-only view of this {@link Attributes} as a {@link Map}.
   *
   * <p>Note: {@link AttributeType#VALUE} attributes will be represented as simple attributes in
   * this map if possible. See {@link AttributesBuilder#put(AttributeKey, Object)} for more details.
   */
  Map<AttributeKey<?>, Object> asMap();

  /** Returns a {@link Attributes} instance with no attributes. */
  static Attributes empty() {
    return ArrayBackedAttributes.EMPTY;
  }

  /** Returns a {@link Attributes} instance with a single key-value pair. */
  static <T> Attributes of(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return empty();
    }
    return new ArrayBackedAttributes(new Object[] {key, value});
  }

  /**
   * Returns a {@link Attributes} instance with two key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static <T, U> Attributes of(AttributeKey<T> key1, T value1, AttributeKey<U> key2, U value2) {
    if (key1 == null || key1.getKey().isEmpty() || value1 == null) {
      return of(key2, value2);
    }
    if (key2 == null || key2.getKey().isEmpty() || value2 == null) {
      return of(key1, value1);
    }
    if (key1.getKey().equals(key2.getKey())) {
      // last one in wins
      return of(key2, value2);
    }
    if (key1.getKey().compareTo(key2.getKey()) > 0) {
      return new ArrayBackedAttributes(new Object[] {key2, value2, key1, value1});
    }
    return new ArrayBackedAttributes(new Object[] {key1, value1, key2, value2});
  }

  /**
   * Returns a {@link Attributes} instance with three key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static <T, U, V> Attributes of(
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
  static <T, U, V, W> Attributes of(
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
  @SuppressWarnings("TooManyParameters")
  static <T, U, V, W, X> Attributes of(
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
  @SuppressWarnings("TooManyParameters")
  static <T, U, V, W, X, Y> Attributes of(
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

  /** Returns a new {@link AttributesBuilder} instance for creating arbitrary {@link Attributes}. */
  static AttributesBuilder builder() {
    return new ArrayBackedAttributesBuilder();
  }

  /**
   * Returns a new {@link AttributesBuilder} instance populated with the data of this {@link
   * Attributes}.
   */
  AttributesBuilder toBuilder();
}
