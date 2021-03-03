/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.common;

import static io.opentelemetry.api.metrics.common.ArrayBackedLabels.sortAndFilterToLabels;

import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container for labels, which are key-value pairs of {@link String}s.
 *
 * <p>Implementations of this interface *must* be immutable and have well-defined value-based
 * equals/hashCode implementations. If an implementation does not strictly conform to these
 * requirements, behavior of the OpenTelemetry APIs and default SDK cannot be guaranteed.
 *
 * <p>For this reason, it is strongly suggested that you use the implementation that is provided
 * here via the factory methods and the {@link ArrayBackedLabelsBuilder}.
 */
@Immutable
public interface Labels {

  /** Returns a {@link Labels} instance with no attributes. */
  static Labels empty() {
    return ArrayBackedLabels.empty();
  }

  /** Creates a new {@link LabelsBuilder} instance for creating arbitrary {@link Labels}. */
  static LabelsBuilder builder() {
    return new ArrayBackedLabelsBuilder();
  }

  /** Returns a {@link Labels} instance with a single key-value pair. */
  static Labels of(String key, String value) {
    return sortAndFilterToLabels(key, value);
  }

  /**
   * Returns a {@link Labels} instance with two key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  static Labels of(String key1, String value1, String key2, String value2) {
    return sortAndFilterToLabels(key1, value1, key2, value2);
  }

  /**
   * Returns a {@link Labels} instance with three key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static Labels of(
      String key1, String value1, String key2, String value2, String key3, String value3) {
    return sortAndFilterToLabels(key1, value1, key2, value2, key3, value3);
  }

  /**
   * Returns a {@link Labels} instance with four key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static Labels of(
      String key1,
      String value1,
      String key2,
      String value2,
      String key3,
      String value3,
      String key4,
      String value4) {
    return sortAndFilterToLabels(key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * Returns a {@link Labels} instance with five key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  static Labels of(
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
    return sortAndFilterToLabels(
        key1, value1,
        key2, value2,
        key3, value3,
        key4, value4,
        key5, value5);
  }

  /** Returns a {@link Labels} instance with the provided {@code keyValueLabelPairs}. */
  static Labels of(String... keyValueLabelPairs) {
    return sortAndFilterToLabels((Object[]) keyValueLabelPairs);
  }

  /** Iterates over all the key-value pairs of labels contained by this instance. */
  void forEach(BiConsumer<? super String, ? super String> consumer);

  /** The number of key-value pairs of labels in this instance. */
  int size();

  /** Returns the value for the given {@code key}, or {@code null} if the key is not present. */
  @Nullable
  String get(String key);

  /** Returns whether this instance is empty (contains no labels). */
  boolean isEmpty();

  /** Returns a read-only view of these {@link Labels} as a {@link Map}. */
  Map<String, String> asMap();

  /** Create a {@link LabelsBuilder} pre-populated with the contents of this Labels instance. */
  LabelsBuilder toBuilder();
}
