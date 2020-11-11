/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.concurrent.Immutable;

/** An immutable container for labels, which are pairs of {@link String}. */
@Immutable
public abstract class Labels extends ImmutableKeyValuePairs<String, String> {

  private static final Labels EMPTY = Labels.builder().build();

  public abstract void forEach(BiConsumer<String, String> consumer);

  @AutoValue
  @Immutable
  abstract static class ArrayBackedLabels extends Labels {
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
  }

  /** Returns a {@link Labels} instance with no attributes. */
  public static Labels empty() {
    return EMPTY;
  }

  /** Returns a {@link Labels} instance with a single key-value pair. */
  public static Labels of(String key, String value) {
    return sortAndFilterToLabels(key, value);
  }

  /**
   * Returns a {@link Labels} instance with two key-value pairs. Order of the keys is not preserved.
   * Duplicate keys will be removed.
   */
  public static Labels of(String key1, String value1, String key2, String value2) {
    return sortAndFilterToLabels(key1, value1, key2, value2);
  }

  /**
   * Returns a {@link Labels} instance with three key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static Labels of(
      String key1, String value1, String key2, String value2, String key3, String value3) {
    return sortAndFilterToLabels(key1, value1, key2, value2, key3, value3);
  }

  /**
   * Returns a {@link Labels} instance with four key-value pairs. Order of the keys is not
   * preserved. Duplicate keys will be removed.
   */
  public static Labels of(
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
  public static Labels of(
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

  public static Labels of(String[] keyValueLabelPairs) {
    return sortAndFilterToLabels((Object[]) keyValueLabelPairs);
  }

  static Labels sortAndFilterToLabels(Object... data) {
    return new AutoValue_Labels_ArrayBackedLabels(
        sortAndFilter(data, /* filterNullValues= */ false));
  }

  /** Create a {@link LabelsBuilder} pre-populated with the contents of this Labels instance. */
  public LabelsBuilder toBuilder() {
    return new LabelsBuilder(data());
  }

  /** Creates a new {@link LabelsBuilder} instance for creating arbitrary {@link Labels}. */
  public static LabelsBuilder builder() {
    return new LabelsBuilder();
  }
}
