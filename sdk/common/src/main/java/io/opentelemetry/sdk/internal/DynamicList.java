/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.AbstractList;
import java.util.Arrays;
import javax.annotation.Nullable;

public class DynamicList<T> extends AbstractList<T> {
  private static final int DEFAULT_SUBARRAY_CAPACITY = 10;
  private final int subarrayCapacity;
  private Object[][] arrays;
  private int size;
  private int arrayCount;

  @SafeVarargs
  public static <T> DynamicList<T> of(T... values) {
    DynamicList<T> list = new DynamicList<>();
    list.resizeAndClear(values.length);
    for (int i = 0; i < values.length; i++) {
      list.set(i, values[i]);
    }
    return list;
  }

  public static <T> DynamicList<T> empty() {
    return new DynamicList<>();
  }

  public DynamicList() {
    this(DEFAULT_SUBARRAY_CAPACITY);
  }

  public DynamicList(int subarrayCapacity) {
    if (subarrayCapacity <= 0) {
      throw new IllegalArgumentException("Subarray capacity must be positive");
    }
    this.subarrayCapacity = subarrayCapacity;
    arrays = new Object[0][subarrayCapacity];
    arrayCount = 0;
    size = 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get(int index) {
    rangeCheck(index);
    return (T) arrays[index / subarrayCapacity][index % subarrayCapacity];
  }

  @Override
  public T set(int index, @Nullable T element) {
    rangeCheck(index);
    T oldValue = get(index);
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
      set(i, null);
    }
  }

  private void ensureCapacity(int minCapacity) {
    int requiredArrays = (minCapacity + subarrayCapacity - 1) / subarrayCapacity;
    if (requiredArrays > arrayCount) {
      arrays = Arrays.copyOf(arrays, requiredArrays);
      for (int i = arrayCount; i < requiredArrays; i++) {
        arrays[i] = new Object[subarrayCapacity];
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
