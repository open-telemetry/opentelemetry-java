/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.Labels.ArrayBackedLabels.sortAndFilterToLabels;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.List;
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

  /** Iterates over all the key-value pairs of labels contained by this instance. */
  void forEach(BiConsumer<String, String> consumer);

  /** The number of key-value pairs of labels in this instance. */
  int size();

  /** Returns the value for the given {@code key}, or {@code null} if the key is not present. */
  @Nullable
  String get(String key);

  /** Returns whether this instance is empty (contains no labels). */
  boolean isEmpty();

  @AutoValue
  @Immutable
  abstract class ArrayBackedLabels extends ImmutableKeyValuePairs<String, String>
      implements Labels {
    private static final Labels EMPTY = Labels.builder().build();

    ArrayBackedLabels() {}

    @Override
    protected abstract List<Object> data();

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
      List<Object> data = data();
      for (int i = 0; i < data.size(); i += 2) {
        consumer.accept((String) data.get(i), (String) data.get(i + 1));
      }
    }

    static Labels sortAndFilterToLabels(Object... data) {
      return new AutoValue_Labels_ArrayBackedLabels(
          sortAndFilter(data, /* filterNullValues= */ false));
    }

    @Override
    public LabelsBuilder toBuilder() {
      return new ArrayBackedLabelsBuilder(data());
    }
  }

  /** Returns a {@link Labels} instance with no attributes. */
  static Labels empty() {
    return ArrayBackedLabels.EMPTY;
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

  static Labels of(String[] keyValueLabelPairs) {
    return sortAndFilterToLabels((Object[]) keyValueLabelPairs);
  }

  /**
   * Create a {@link ArrayBackedLabelsBuilder} pre-populated with the contents of this Labels
   * instance.
   */
  LabelsBuilder toBuilder();

  /**
   * Creates a new {@link ArrayBackedLabelsBuilder} instance for creating arbitrary {@link Labels}.
   */
  static LabelsBuilder builder() {
    return new ArrayBackedLabelsBuilder();
  }
}
