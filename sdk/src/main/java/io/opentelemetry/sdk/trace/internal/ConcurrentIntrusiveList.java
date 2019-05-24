/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.internal;

import static com.google.common.base.Preconditions.checkArgument;

import io.opentelemetry.sdk.trace.internal.ConcurrentIntrusiveList.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An {@code ConcurrentIntrusiveList<T>} is a doubly-linked list where the link pointers are
 * embedded in the elements. This makes insertion and removal into a known position constant time.
 *
 * <p>Elements must derive from the {@code Element<T extends Element<T>>} interface:
 *
 * <pre><code>
 * class MyClass implements {@code Element<MyClass>} {
 *   private MyClass next = null;
 *   private MyClass prev = null;
 *
 *  {@literal @}Override
 *   MyClass getNext() {
 *     return next;
 *   }
 *
 *  {@literal @}Override
 *   void setNext(MyClass element) {
 *     next = element;
 *   }
 *
 *  {@literal @}Override
 *   MyClass getPrev() {
 *     return prev;
 *   }
 *
 *  {@literal @}Override
 *   void setPrev(MyClass element) {
 *     prev = element;
 *   }
 * }
 * </code></pre>
 */
@ThreadSafe
public final class ConcurrentIntrusiveList<T extends Element<T>> {
  private final int capacity;
  private int size = 0;
  @Nullable private T head = null;

  /**
   * Constructs a new {@code ConcurrentIntrusiveList}.
   *
   * @param capacity must be greater than {@code 0}.
   */
  public ConcurrentIntrusiveList(int capacity) {
    checkArgument(capacity > 0, "Capacity needs to be greater than 0.");
    this.capacity = capacity;
  }

  /**
   * Adds the given {@code element} to the list. If the number of elements will be larger than the
   * capacity then the oldest element in the list will be removed.
   *
   * @param element the element to add.
   * @return {@code false} if the element is already in the list or if adding this element will
   *     exceed capacity.
   */
  public synchronized boolean addElement(T element) {
    if (element.getNext() != null
        || element.getPrev() != null
        || element == head
        || size >= capacity) {
      // Element already in a list.
      return false;
    }
    size++;
    if (head == null) {
      head = element;
    } else {
      head.setPrev(element);
      element.setNext(head);
      head = element;
    }
    return true;
  }

  /**
   * Removes the given {@code element} from the list.
   *
   * @param element the element to remove.
   */
  public synchronized boolean removeElement(T element) {
    if (element.getNext() == null && element.getPrev() == null && element != head) {
      // Element not in the list.
      return false;
    }
    size--;
    T prev = element.getPrev();
    T next = element.getNext();
    if (prev == null) {
      // This is the first element
      head = next;
      if (head != null) {
        // If more than one element in the list.
        head.setPrev(null);
        element.setNext(null);
      }
    } else if (next == null) {
      // This is the last element, and there is at least another element because
      // element.getPrev() != null.
      prev.setNext(null);
      element.setPrev(null);
    } else {
      prev.setNext(element.getNext());
      next.setPrev(element.getPrev());
      element.setNext(null);
      element.setPrev(null);
    }
    return true;
  }

  /**
   * Returns the number of elements in this list.
   *
   * @return the number of elements in this list.
   */
  public synchronized int size() {
    return size;
  }

  /** Clears all the elements from the list. */
  public synchronized void clear() {
    while (true) {
      T currentHead = head;
      if (currentHead == null) {
        // No more elements in the list.
        return;
      }
      // This will move the head.
      removeElement(currentHead);
    }
  }

  /**
   * Returns all the elements from this list.
   *
   * @return all the elements from this list.
   */
  public synchronized Collection<T> getAll() {
    List<T> all = new ArrayList<T>(size);
    for (T e = head; e != null; e = e.getNext()) {
      all.add(e);
    }
    return all;
  }

  /**
   * This is an interface that must be implemented by any element that uses {@link
   * ConcurrentIntrusiveList}.
   *
   * @param <T> the element that will be used for the list.
   */
  public interface Element<T extends Element<T>> {

    /**
     * Returns a reference to the next element in the list.
     *
     * @return a reference to the next element in the list.
     */
    @Nullable
    T getNext();

    /**
     * Sets the reference to the next element in the list.
     *
     * @param element the reference to the next element in the list.
     */
    void setNext(@Nullable T element);

    /**
     * Returns a reference to the previous element in the list.
     *
     * @return a reference to the previous element in the list.
     */
    @Nullable
    T getPrev();

    /**
     * Sets the reference to the previous element in the list.
     *
     * @param element the reference to the previous element in the list.
     */
    void setPrev(@Nullable T element);
  }
}
