/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * A resizable list for storing primitive `long` values.
 *
 * <p>This class implements a dynamically resizable list specifically for primitive long values. The
 * values are stored in a chain of arrays (named sub-array), so it can grow efficiently, by adding
 * more sub-arrays per its defined size. The backing array also helps avoid auto-boxing and helps
 * provide access to values as primitives without boxing.
 *
 * <p>The list is designed to minimize memory allocations, by:
 *
 * <ol>
 *   <li>Adding sub-arrays and not creating new arrays and copying.
 *   <li>When the size is changing to a smaller size, arrays are not removed.
 * </ol>
 *
 * <p><b>Supported {@code List<Long>} methods:</b>
 *
 * <ul>
 *   <li>{@link #get(int)} - Retrieves the element at the specified position in this list as a
 *       {@code Long} object.
 *   <li>{@link #set(int, Long)} - Replaces the element at the specified position in this list with
 *       the specified {@code Long} object.
 *   <li>{@link #size()} - Returns the number of elements in this list.
 * </ul>
 *
 * <p><b>Additional utility methods:</b>
 *
 * <ul>
 *   <li>{@link #getLong(int)} - Retrieves the element at the specified position in this list as a
 *       primitive long.
 *   <li>{@link #setLong(int, long)} - Replaces the element at the specified position in this list
 *       with the specified primitive long element.
 *   <li>{@link #resizeAndClear(int)} - Resizes the list to the specified size, resetting all
 *       elements to zero.
 * </ul>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public class DynamicPrimitiveLongList extends AbstractList<Long> {

  private static final int DEFAULT_SUBARRAY_CAPACITY = 10;
  private final int subarrayCapacity;
  private long[][] arrays;
  private int size;
  private int arrayCount;

  public static DynamicPrimitiveLongList of(long... values) {
    DynamicPrimitiveLongList list = new DynamicPrimitiveLongList();
    list.resizeAndClear(values.length);
    for (int i = 0; i < values.length; i++) {
      list.setLong(i, values[i]);
    }
    return list;
  }

  public static DynamicPrimitiveLongList ofSubArrayCapacity(int subarrayCapacity) {
    return new DynamicPrimitiveLongList(subarrayCapacity);
  }

  public static DynamicPrimitiveLongList empty() {
    return new DynamicPrimitiveLongList();
  }

  DynamicPrimitiveLongList() {
    this(DEFAULT_SUBARRAY_CAPACITY);
  }

  DynamicPrimitiveLongList(int subarrayCapacity) {
    if (subarrayCapacity <= 0) {
      throw new IllegalArgumentException("Subarray capacity must be positive");
    }
    this.subarrayCapacity = subarrayCapacity;
    arrays = new long[0][subarrayCapacity];
    arrayCount = 0;
    size = 0;
  }

  @Override
  public Long get(int index) {
    return getLong(index);
  }

  public long getLong(int index) {
    rangeCheck(index);
    return arrays[index / subarrayCapacity][index % subarrayCapacity];
  }

  @Override
  public Long set(int index, Long element) {
    return setLong(index, element);
  }

  public long setLong(int index, long element) {
    rangeCheck(index);
    long oldValue = arrays[index / subarrayCapacity][index % subarrayCapacity];
    arrays[index / subarrayCapacity][index % subarrayCapacity] = element;
    return oldValue;
  }

  @Override
  public int size() {
    return size;
  }

  public void resizeAndClear(int newSize) {
    if (newSize < 0) {
      throw new IllegalArgumentException("New size must be non-negative");
    }
    ensureCapacity(newSize);
    size = newSize;
    for (int i = 0; i < newSize; i++) {
      setLong(i, 0);
    }
  }

  private void ensureCapacity(int minCapacity) {
    // A faster way to do ceil(minCapacity/subArrayCapacity)
    int requiredArrays = (minCapacity + subarrayCapacity - 1) / subarrayCapacity;

    if (requiredArrays > arrayCount) {
      arrays = Arrays.copyOf(arrays, /* newLength= */ requiredArrays);
      for (int i = arrayCount; i < requiredArrays; i++) {
        arrays[i] = new long[subarrayCapacity];
      }
      arrayCount = requiredArrays;
    }
  }

  private void rangeCheck(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
  }

  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Size: " + size;
  }
}
