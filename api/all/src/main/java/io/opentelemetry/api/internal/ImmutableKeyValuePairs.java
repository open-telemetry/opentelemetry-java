/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable set of key-value pairs.
 *
 * <p>Key-value pairs are dropped for {@code null} or empty keys.
 *
 * <p>Note: for subclasses of this, null keys will be removed, but if your key has another concept
 * of being "empty", you'll need to remove them before calling {@link #sortAndFilter(Object[],
 * boolean)}, assuming you don't want the "empty" keys to be kept in your collection.
 *
 * @param <V> The type of the values contained in this.
 */
@Immutable
public abstract class ImmutableKeyValuePairs<K, V> {

  protected abstract List<Object> data();

  public int size() {
    return data().size() / 2;
  }

  public boolean isEmpty() {
    return data().isEmpty();
  }

  public Map<K, V> asMap() {
    return ReadOnlyArrayMap.wrap(data());
  }

  /** Returns the value for the given {@code key}, or {@code null} if the key is not present. */
  @Nullable
  @SuppressWarnings("unchecked")
  public V get(K key) {
    for (int i = 0; i < data().size(); i += 2) {
      if (key.equals(data().get(i))) {
        return (V) data().get(i + 1);
      }
    }
    return null;
  }

  /** Iterates over all the key-value pairs of labels contained by this instance. */
  @SuppressWarnings("unchecked")
  public void forEach(BiConsumer<K, V> consumer) {
    for (int i = 0; i < data().size(); i += 2) {
      consumer.accept((K) data().get(i), (V) data().get(i + 1));
    }
  }

  /**
   * Sorts and dedupes the key/value pairs in {@code data}. If {@code filterNullValues} is {@code
   * true}, {@code null} values will be removed. Keys must be {@link Comparable}.
   */
  protected static List<Object> sortAndFilter(Object[] data, boolean filterNullValues) {
    return sortAndFilter(data, filterNullValues, Comparator.naturalOrder());
  }

  /**
   * Sorts and dedupes the key/value pairs in {@code data}. If {@code filterNullValues} is {@code
   * true}, {@code null} values will be removed. Keys will be compared with the given {@link
   * Comparator}.
   */
  protected static List<Object> sortAndFilter(
      Object[] data, boolean filterNullValues, Comparator<?> keyComparator) {
    checkArgument(
        data.length % 2 == 0, "You must provide an even number of key/value pair arguments.");

    mergeSort(data, keyComparator);
    return dedupe(data, filterNullValues, keyComparator);
  }

  // note: merge sort implementation cribbed from this wikipedia article:
  // https://en.wikipedia.org/wiki/Merge_sort (this is the top-down variant)
  private static void mergeSort(Object[] data, Comparator<?> keyComparator) {
    Object[] workArray = new Object[data.length];
    System.arraycopy(data, 0, workArray, 0, data.length);
    splitAndMerge(
        workArray,
        0,
        data.length,
        data,
        keyComparator); // sort data from workArray[] into sourceArray[]
  }

  /**
   * Sort the given run of array targetArray[] using array workArray[] as a source. beginIndex is
   * inclusive; endIndex is exclusive (targetArray[endIndex] is not in the set).
   */
  private static void splitAndMerge(
      Object[] workArray,
      int beginIndex,
      int endIndex,
      Object[] targetArray,
      Comparator<?> keyComparator) {
    if (endIndex - beginIndex <= 2) { // if single element in the run, it's sorted
      return;
    }
    // split the run longer than 1 item into halves
    int midpoint = ((endIndex + beginIndex) / 4) * 2; // note: due to it's being key/value pairs
    // recursively sort both runs from array targetArray[] into workArray[]
    splitAndMerge(targetArray, beginIndex, midpoint, workArray, keyComparator);
    splitAndMerge(targetArray, midpoint, endIndex, workArray, keyComparator);
    // merge the resulting runs from array workArray[] into targetArray[]
    merge(workArray, beginIndex, midpoint, endIndex, targetArray, keyComparator);
  }

  /**
   * Left source half is sourceArray[ beginIndex:middleIndex-1]. Right source half is sourceArray[
   * middleIndex:endIndex-1]. Result is targetArray[ beginIndex:endIndex-1].
   */
  @SuppressWarnings("unchecked")
  private static <K> void merge(
      Object[] sourceArray,
      int beginIndex,
      int middleIndex,
      int endIndex,
      Object[] targetArray,
      Comparator<K> keyComparator) {
    int leftKeyIndex = beginIndex;
    int rightKeyIndex = middleIndex;

    // While there are elements in the left or right runs, fill in the target array from left to
    // right
    for (int k = beginIndex; k < endIndex; k += 2) {
      // If left run head exists and is <= existing right run head.
      if (leftKeyIndex < middleIndex - 1
          && (rightKeyIndex >= endIndex - 1
              || compareToNullSafe(
                      (K) sourceArray[leftKeyIndex], (K) sourceArray[rightKeyIndex], keyComparator)
                  <= 0)) {
        targetArray[k] = sourceArray[leftKeyIndex];
        targetArray[k + 1] = sourceArray[leftKeyIndex + 1];
        leftKeyIndex = leftKeyIndex + 2;
      } else {
        targetArray[k] = sourceArray[rightKeyIndex];
        targetArray[k + 1] = sourceArray[rightKeyIndex + 1];
        rightKeyIndex = rightKeyIndex + 2;
      }
    }
  }

  private static <K> int compareToNullSafe(
      @Nullable K key, @Nullable K pivotKey, Comparator<K> keyComparator) {
    if (key == null) {
      return pivotKey == null ? 0 : -1;
    }
    if (pivotKey == null) {
      return 1;
    }
    return keyComparator.compare(key, pivotKey);
  }

  @SuppressWarnings("unchecked")
  private static <K> List<Object> dedupe(
      Object[] data, boolean filterNullValues, Comparator<K> keyComparator) {
    List<Object> result = new ArrayList<>(data.length);
    Object previousKey = null;

    // iterate in reverse, to implement the "last one in wins" behavior.
    for (int i = data.length - 2; i >= 0; i -= 2) {
      Object key = data[i];
      Object value = data[i + 1];
      if (key == null) {
        continue;
      }
      if (previousKey != null && keyComparator.compare((K) key, (K) previousKey) == 0) {
        continue;
      }
      previousKey = key;
      if (filterNullValues && value == null) {
        continue;
      }
      // add them in reverse order, because we'll reverse the list before returning,
      // to preserve insertion order.
      result.add(value);
      result.add(key);
    }
    Collections.reverse(result);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("{");
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      // Quote string values
      Object value = data.get(i + 1);
      String valueStr = value instanceof String ? '"' + (String) value + '"' : value.toString();
      sb.append(data.get(i)).append("=").append(valueStr).append(", ");
    }
    // get rid of that last pesky comma
    if (sb.length() > 1) {
      sb.setLength(sb.length() - 2);
    }
    sb.append("}");
    return sb.toString();
  }
}
