/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * @see Labels
 * @see Attributes
 */
@SuppressWarnings("rawtypes")
@Immutable
public abstract class ImmutableKeyValuePairs<K, V> {

  protected List<Object> data() {
    return Collections.emptyList();
  }

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

  /**
   * Sorts and dedupes the key/value pairs in {@code data}. If {@code filterNullValues} is {@code
   * true}, {@code null} values will be removed.
   */
  @SuppressWarnings("unchecked")
  public static List<Object> sortAndFilter(Object[] data, boolean filterNullValues) {
    checkArgument(
        data.length % 2 == 0, "You must provide an even number of key/value pair arguments.");

    mergeSort(data);
    return dedupe(data, filterNullValues);
  }

  private static void mergeSort(Object[] data) {
    Object[] workArray = new Object[data.length];
    mergeSort(data, workArray, data.length);
  }

  // note: merge sort implementation cribbed from this wikipedia article:
  // https://en.wikipedia.org/wiki/Merge_sort (this is the top-down variant)
  private static <K extends Comparable<K>> void mergeSort(
      Object[] sourceArray, Object[] workArray, int n) {
    System.arraycopy(sourceArray, 0, workArray, 0, sourceArray.length);
    splitAndMerge(workArray, 0, n, sourceArray); // sort data from workArray[] into sourceArray[]
  }

  /**
   * Sort the given run of array targetArray[] using array workArray[] as a source. beginIndex is
   * inclusive; endIndex is exclusive (targetArray[endIndex] is not in the set).
   */
  private static <K extends Comparable<K>> void splitAndMerge(
      Object[] workArray, int beginIndex, int endIndex, Object[] targetArray) {
    if (endIndex - beginIndex <= 2) { // if single element in the run, it's sorted
      return;
    }
    // split the run longer than 1 item into halves
    int midpoint = ((endIndex + beginIndex) / 4) * 2; // note: due to it's being key/value pairs
    // recursively sort both runs from array targetArray[] into workArray[]
    splitAndMerge(targetArray, beginIndex, midpoint, workArray);
    splitAndMerge(targetArray, midpoint, endIndex, workArray);
    // merge the resulting runs from array workArray[] into targetArray[]
    merge(workArray, beginIndex, midpoint, endIndex, targetArray);
  }

  /**
   * Left source half is sourceArray[ beginIndex:middleIndex-1]. Right source half is sourceArray[
   * middleIndex:endIndex-1]. Result is targetArray[ beginIndex:endIndex-1].
   */
  @SuppressWarnings("unchecked")
  private static <K extends Comparable<K>> void merge(
      Object[] sourceArray, int beginIndex, int middleIndex, int endIndex, Object[] targetArray) {
    int leftKeyIndex = beginIndex;
    int rightKeyIndex = middleIndex;

    // While there are elements in the left or right runs, fill in the target array from left to
    // right
    for (int k = beginIndex; k < endIndex; k += 2) {
      // If left run head exists and is <= existing right run head.
      if (leftKeyIndex < middleIndex - 1
          && (rightKeyIndex >= endIndex - 1
              || compareToNullSafe((K) sourceArray[leftKeyIndex], (K) sourceArray[rightKeyIndex])
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

  private static <K extends Comparable<K>> int compareToNullSafe(
      @Nullable K key, @Nullable K pivotKey) {
    if (key == null) {
      return pivotKey == null ? 0 : -1;
    }
    if (pivotKey == null) {
      return 1;
    }
    return key.compareTo(pivotKey);
  }

  private static List<Object> dedupe(Object[] data, boolean filterNullValues) {
    List<Object> result = new ArrayList<>(data.length);
    Object previousKey = null;

    // iterate in reverse, to implement the "last one in wins" behavior.
    for (int i = data.length - 2; i >= 0; i -= 2) {
      Object key = data[i];
      Object value = data[i + 1];
      if (key == null) {
        continue;
      }
      if (key.equals(previousKey)) {
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
      sb.append(data.get(i)).append("=").append(data.get(i + 1)).append(", ");
    }
    // get rid of that last pesky comma
    if (sb.length() > 1) {
      sb.setLength(sb.length() - 2);
    }
    sb.append("}");
    return sb.toString();
  }
}
