/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.ArrayBackedComplexAttribute.sortAndFilterToAttributes;

import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for a complex attribute.
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
 * here via the factory methods and the {@link ComplexAttributeBuilder}.
 */
@SuppressWarnings("rawtypes")
@Immutable
public interface ComplexAttribute {

  /** Returns the value for the given {@link AttributeKey}, or {@code null} if not found. */
  @Nullable
  <T> T get(AttributeKey<T> key);

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer);

  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /** Returns a read-only view of this {@link ComplexAttribute} as a {@link Map}. */
  Map<AttributeKey<?>, Object> asMap();

  /** Returns a {@link ComplexAttribute} instance with no attributes. */
  static ComplexAttribute empty() {
    return ArrayBackedComplexAttribute.EMPTY;
  }

  /** Returns a {@link ComplexAttribute} instance with a single key-value pair. */
  static <T> ComplexAttribute of(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return empty();
    }
    return new ArrayBackedComplexAttribute(new Object[] {key, value});
  }

  /**
   * Returns a {@link ComplexAttribute} instance with two key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static <T, U> ComplexAttribute of(
      AttributeKey<T> key1, T value1, AttributeKey<U> key2, U value2) {
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
      return new ArrayBackedComplexAttribute(new Object[] {key2, value2, key1, value1});
    }
    return new ArrayBackedComplexAttribute(new Object[] {key1, value1, key2, value2});
  }

  /**
   * Returns a {@link ComplexAttribute} instance with three key-value pairs. Order of the keys is
   * not preserved. Duplicate keys will be removed.
   */
  static <T, U, V> ComplexAttribute of(
      AttributeKey<T> key1,
      T value1,
      AttributeKey<U> key2,
      U value2,
      AttributeKey<V> key3,
      V value3) {
    return sortAndFilterToAttributes(key1, value1, key2, value2, key3, value3);
  }

  /**
   * Returns a {@link ComplexAttribute} instance with four key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static <T, U, V, W> ComplexAttribute of(
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
   * Returns a {@link ComplexAttribute} instance with five key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  @SuppressWarnings("TooManyParameters")
  static <T, U, V, W, X> ComplexAttribute of(
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
   * Returns a {@link ComplexAttribute} instance with the given key-value pairs. Order of the keys
   * is not preserved. Duplicate keys will be removed.
   */
  @SuppressWarnings("TooManyParameters")
  static <T, U, V, W, X, Y> ComplexAttribute of(
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

  /**
   * Returns a new {@link ComplexAttributeBuilder} instance for creating arbitrary {@link
   * ComplexAttribute}.
   */
  static ComplexAttributeBuilder builder() {
    return new ArrayBackedComplexAttributeBuilder();
  }

  /**
   * Returns a new {@link ComplexAttributeBuilder} instance populated with the data of this {@link
   * ComplexAttribute}.
   */
  ComplexAttributeBuilder toBuilder();
}
