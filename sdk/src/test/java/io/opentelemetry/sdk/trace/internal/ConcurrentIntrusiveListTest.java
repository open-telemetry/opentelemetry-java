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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.internal.ConcurrentIntrusiveList.Element;
import java.util.ArrayList;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ConcurrentIntrusiveList}. */
@RunWith(JUnit4.class)
public class ConcurrentIntrusiveListTest {
  private static final int CAPACITY = 5;
  private final ConcurrentIntrusiveList<FakeElement> intrusiveList =
      new ConcurrentIntrusiveList<>(CAPACITY);

  @Test
  public void emptyList() {
    assertThat(intrusiveList.size()).isEqualTo(0);
    assertThat(intrusiveList.getAll().isEmpty()).isTrue();
  }

  @Test
  public void addRemoveAdd_SameElement() {
    FakeElement element = new FakeElement();
    assertThat(intrusiveList.addElement(element)).isTrue();
    assertThat(intrusiveList.size()).isEqualTo(1);
    assertThat(intrusiveList.removeElement(element)).isTrue();
    assertThat(intrusiveList.size()).isEqualTo(0);
    assertThat(intrusiveList.addElement(element)).isTrue();
    assertThat(intrusiveList.size()).isEqualTo(1);
  }

  @Test
  public void addAndRemoveElements() {
    FakeElement element1 = new FakeElement();
    FakeElement element2 = new FakeElement();
    FakeElement element3 = new FakeElement();
    intrusiveList.addElement(element1);
    intrusiveList.addElement(element2);
    intrusiveList.addElement(element3);
    assertThat(intrusiveList.size()).isEqualTo(3);
    assertThat(intrusiveList.getAll()).containsExactly(element3, element2, element1).inOrder();
    // Remove element from the middle of the list.
    intrusiveList.removeElement(element2);
    assertThat(intrusiveList.size()).isEqualTo(2);
    assertThat(intrusiveList.getAll()).containsExactly(element3, element1).inOrder();
    // Remove element from the tail of the list.
    intrusiveList.removeElement(element1);
    assertThat(intrusiveList.size()).isEqualTo(1);
    assertThat(intrusiveList.getAll().contains(element3)).isTrue();
    intrusiveList.addElement(element1);
    assertThat(intrusiveList.size()).isEqualTo(2);
    assertThat(intrusiveList.getAll()).containsExactly(element1, element3).inOrder();
    // Remove element from the head of the list when there are other elements after.
    intrusiveList.removeElement(element1);
    assertThat(intrusiveList.size()).isEqualTo(1);
    assertThat(intrusiveList.getAll().contains(element3)).isTrue();
    // Remove element from the head of the list when no more other elements in the list.
    intrusiveList.removeElement(element3);
    assertThat(intrusiveList.size()).isEqualTo(0);
    assertThat(intrusiveList.getAll().isEmpty()).isTrue();
  }

  @Test
  public void clear() {
    FakeElement element1 = new FakeElement();
    FakeElement element2 = new FakeElement();
    FakeElement element3 = new FakeElement();
    intrusiveList.addElement(element1);
    intrusiveList.addElement(element2);
    intrusiveList.addElement(element3);
    assertThat(intrusiveList.size()).isEqualTo(3);
    intrusiveList.clear();
    // Check that elements are no longer in the list.
    assertThat(intrusiveList.removeElement(element1)).isFalse();
    assertThat(intrusiveList.removeElement(element2)).isFalse();
    assertThat(intrusiveList.removeElement(element3)).isFalse();
  }

  @Test
  public void addMoreThanCapacity() {
    ArrayList<FakeElement> elements = new ArrayList<>(2 * CAPACITY);
    for (int i = 0; i < CAPACITY; i++) {
      FakeElement element = new FakeElement();
      elements.add(element);
      assertThat(intrusiveList.addElement(element)).isTrue();
    }

    assertThat(intrusiveList.size()).isEqualTo(CAPACITY);

    // Try to add more elements. All will fail.
    for (int i = 0; i < CAPACITY; i++) {
      assertThat(intrusiveList.addElement(new FakeElement())).isFalse();
      assertThat(intrusiveList.size()).isEqualTo(CAPACITY);
    }

    // Check that the first CAPACITY elements are not in the list.
    for (int i = 0; i < CAPACITY; i++) {
      assertThat(intrusiveList.removeElement(elements.get(i))).isTrue();
    }
  }

  @Test
  public void addMoreThanCapacity_ThenRemoveAndAdd() {
    ArrayList<FakeElement> elements = new ArrayList<>(2 * CAPACITY);
    for (int i = 0; i < CAPACITY; i++) {
      FakeElement element = new FakeElement();
      elements.add(element);
      assertThat(intrusiveList.addElement(element)).isTrue();
    }

    assertThat(intrusiveList.removeElement(elements.get(CAPACITY / 2))).isTrue();

    // Now we can add another element
    assertThat(intrusiveList.addElement(new FakeElement())).isTrue();
  }

  @Test
  public void addAlreadyAddedElement() {
    FakeElement element = new FakeElement();
    assertThat(intrusiveList.addElement(element)).isTrue();
    assertThat(intrusiveList.addElement(element)).isFalse();
  }

  @Test
  public void removeNotAddedElement() {
    assertThat(intrusiveList.removeElement(new FakeElement())).isFalse();
  }

  private static final class FakeElement implements Element<FakeElement> {
    @Nullable private FakeElement next = null;
    @Nullable private FakeElement prev = null;

    @Override
    public FakeElement getNext() {
      return next;
    }

    @Override
    public void setNext(FakeElement element) {
      next = element;
    }

    @Override
    public FakeElement getPrev() {
      return prev;
    }

    @Override
    public void setPrev(FakeElement element) {
      prev = element;
    }
  }
}
