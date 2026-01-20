/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for extended attributes.
 *
 * <p>"extended" refers an extended set of allowed value types compared to standard {@link
 * Attributes}. Notably, {@link ExtendedAttributes} values can be of type {@link
 * ExtendedAttributeType#VALUE}, allowing attributes backed by {@link
 * io.opentelemetry.api.common.Value}.
 *
 * <p>Where standard {@link Attributes} are accepted everyone that OpenTelemetry represents key /
 * value pairs, {@link ExtendedAttributes} are only accepted in select places, such as log records
 * (e.g. {@link ExtendedLogRecordBuilder#setAttribute(ExtendedAttributeKey, Object)}).
 *
 * <p>The keys are {@link ExtendedAttributeKey}s and the values are Object instances that match the
 * type of the provided key.
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
 * here via the factory methods and the {@link ExtendedAttributesBuilder}.
 *
 * <p>Convenience methods are provided for translating to / from {@link Attributes}:
 *
 * <ul>
 *   <li>{@link #asAttributes()} converts from {@link ExtendedAttributes} to {@link Attributes}
 *   <li>{@link ExtendedAttributesBuilder#putAll(Attributes)} converts from {@link Attributes} to
 *       {@link ExtendedAttributes}
 *   <li>{@link #get(AttributeKey)} supports reading values using standard {@link AttributeKey}
 * </ul>
 *
 * @deprecated Use {@link io.opentelemetry.api.common.Attributes} instead. Complex attributes are
 *     now supported directly in the standard API via {@link
 *     io.opentelemetry.api.common.AttributeKey#valueKey(String)} and {@link
 *     io.opentelemetry.api.common.AttributesBuilder#put(io.opentelemetry.api.common.AttributeKey,
 *     Object)}.
 */
@Deprecated
@Immutable
public interface ExtendedAttributes {

  /** Returns the value for the given {@link AttributeKey}, or {@code null} if not found. */
  @Nullable
  default <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    return get(ExtendedAttributeKey.fromAttributeKey(key));
  }

  /**
   * Returns the value for the given {@link ExtendedAttributeKey}, or {@code null} if not found.
   *
   * <p>Note: this method will automatically return the corresponding {@link
   * io.opentelemetry.api.common.Value} instance when passed a key of type {@link
   * ExtendedAttributeType#VALUE} and a simple attribute is found. This is the inverse of {@link
   * ExtendedAttributesBuilder#put(ExtendedAttributeKey, Object)} when the key is {@link
   * ExtendedAttributeType#VALUE}.
   *
   * <ul>
   *   <li>If {@code put(ExtendedAttributeKey.stringKey("key"), "a")} was called, then {@code
   *       get(ExtendedAttributeKey.valueKey("key"))} returns {@code Value.of("a")}.
   *   <li>If {@code put(ExtendedAttributeKey.longKey("key"), 1L)} was called, then {@code
   *       get(ExtendedAttributeKey.valueKey("key"))} returns {@code Value.of(1L)}.
   *   <li>If {@code put(ExtendedAttributeKey.doubleKey("key"), 1.0)} was called, then {@code
   *       get(ExtendedAttributeKey.valueKey("key"))} returns {@code Value.of(1.0)}.
   *   <li>If {@code put(ExtendedAttributeKey.booleanKey("key"), true)} was called, then {@code
   *       get(ExtendedAttributeKey.valueKey("key"))} returns {@code Value.of(true)}.
   *   <li>If {@code put(ExtendedAttributeKey.stringArrayKey("key"), Arrays.asList("a", "b"))} was
   *       called, then {@code get(ExtendedAttributeKey.valueKey("key"))} returns {@code
   *       Value.of(Value.of("a"), Value.of("b"))}.
   *   <li>If {@code put(ExtendedAttributeKey.longArrayKey("key"), Arrays.asList(1L, 2L))} was
   *       called, then {@code get(ExtendedAttributeKey.valueKey("key"))} returns {@code
   *       Value.of(Value.of(1L), Value.of(2L))}.
   *   <li>If {@code put(ExtendedAttributeKey.doubleArrayKey("key"), Arrays.asList(1.0, 2.0))} was
   *       called, then {@code get(ExtendedAttributeKey.valueKey("key"))} returns {@code
   *       Value.of(Value.of(1.0), Value.of(2.0))}.
   *   <li>If {@code put(ExtendedAttributeKey.booleanArrayKey("key"), Arrays.asList(true, false))}
   *       was called, then {@code get(ExtendedAttributeKey.valueKey("key"))} returns {@code
   *       Value.of(Value.of(true), Value.of(false))}.
   * </ul>
   *
   * <p>Further, if {@code put(ExtendedAttributeKey.valueKey("key"), Value.of(emptyList()))} was
   * called, then
   *
   * <ul>
   *   <li>{@code get(ExtendedAttributeKey.stringArrayKey("key"))}
   *   <li>{@code get(ExtendedAttributeKey.longArrayKey("key"))}
   *   <li>{@code get(ExtendedAttributeKey.booleanArrayKey("key"))}
   *   <li>{@code get(ExtendedAttributeKey.doubleArrayKey("key"))}
   * </ul>
   *
   * <p>all return an empty list (as opposed to {@code null}).
   */
  @Nullable
  <T> T get(ExtendedAttributeKey<T> key);

  /**
   * Iterates over all the key-value pairs of attributes contained by this instance.
   *
   * <p>Note: {@link ExtendedAttributeType#VALUE} attributes will be represented as simple
   * attributes if possible. See {@link ExtendedAttributesBuilder#put(ExtendedAttributeKey, Object)}
   * for more details.
   */
  void forEach(BiConsumer<? super ExtendedAttributeKey<?>, ? super Object> consumer);

  /** The number of attributes contained in this. */
  int size();

  /** Whether there are any attributes contained in this. */
  boolean isEmpty();

  /**
   * Returns a read-only view of this {@link ExtendedAttributes} as a {@link Map}.
   *
   * <p>Note: {@link ExtendedAttributeType#VALUE} attributes will be represented as simple
   * attributes in this map if possible. See {@link
   * ExtendedAttributesBuilder#put(ExtendedAttributeKey, Object)} for more details.
   */
  Map<ExtendedAttributeKey<?>, Object> asMap();

  /**
   * Return a view of this extended attributes with entries limited to those representable as
   * standard attributes.
   */
  Attributes asAttributes();

  /** Returns a {@link ExtendedAttributes} instance with no attributes. */
  static ExtendedAttributes empty() {
    return ArrayBackedExtendedAttributes.EMPTY;
  }

  /**
   * Returns a new {@link ExtendedAttributesBuilder} instance for creating arbitrary {@link
   * ExtendedAttributes}.
   *
   * @return a new {@link ExtendedAttributesBuilder} instance
   */
  static ExtendedAttributesBuilder builder() {
    return new ArrayBackedExtendedAttributesBuilder();
  }

  /**
   * Returns a new {@link ExtendedAttributesBuilder} instance populated with the data of this {@link
   * ExtendedAttributes}.
   */
  ExtendedAttributesBuilder toBuilder();
}
