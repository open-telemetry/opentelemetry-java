/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import javax.annotation.Nullable;

/**
 * Array-based Stack.
 *
 * <p>Not thread-safe
 */
public class ArrayBasedStack<T> {
  static final int DEFAULT_CAPACITY = 10;

  // Using native array instead of ArrayList since I plan to add eviction
  // if the initial portion of the stack is not used for several cycles of collection
  private T[] array;

  private int size;

  @SuppressWarnings("unchecked")
  public ArrayBasedStack() {
    array = (T[]) new Object[DEFAULT_CAPACITY];
    size = 0;
  }

  /**
   * Add {@code element} to the top of the stack (LIFO).
   *
   * @param element The element to add
   * @throws NullPointerException if {@code element} is null
   */
  public void push(T element) {
    if (element == null) {
      throw new NullPointerException("Null is not permitted as element in the stack");
    }
    if (size == array.length) {
      resizeArray(array.length * 2);
    }
    array[size++] = element;
  }

  /**
   * Removes and returns an element from the top of the stack (LIFO).
   *
   * @return the top most element in the stack (last one added)
   */
  @Nullable
  public T pop() {
    if (isEmpty()) {
      return null;
    }
    T element = array[size - 1];
    array[size - 1] = null;
    size--;
    return element;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  @SuppressWarnings("unchecked")
  private void resizeArray(int newCapacity) {
    T[] newArray = (T[]) new Object[newCapacity];
    System.arraycopy(array, 0, newArray, 0, size);
    array = newArray;
  }
}
