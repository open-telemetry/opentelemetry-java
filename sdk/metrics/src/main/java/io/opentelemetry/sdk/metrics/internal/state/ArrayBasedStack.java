package io.opentelemetry.sdk.metrics.internal.state;

import javax.annotation.Nullable;

/**
 * Array-based Stack of {@code T}
 *
 * Not thread-safe
 */
public class ArrayBasedStack<T> {
  private static final int DEFAULT_CAPACITY = 10;
  private T[] array;
  private int size;

  @SuppressWarnings("unchecked")
  public ArrayBasedStack() {
    array = (T[]) new Object[DEFAULT_CAPACITY];
    size = 0;
  }

  public void push(T element) {
    if (element == null) {
      throw new NullPointerException("Null is not permitted as element in the stack");
    }
    if (size == array.length) {
      resizeArray(array.length * 2);
    }
    array[size++] = element;
  }

  @Nullable
  public T pop() {
    if (isEmpty()) {
      return null; // Return null if the stack is empty
    }
    T element = array[size - 1];
    array[size - 1] = null; // Allow garbage collection
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
