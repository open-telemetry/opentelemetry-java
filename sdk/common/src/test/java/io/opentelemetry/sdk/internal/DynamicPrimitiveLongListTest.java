/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class DynamicPrimitiveLongListTest {

  @Test
  public void subArrayCapacityMustBePositive() {
    assertThatThrownBy(() -> {
          int subArrayCapacity = 0;
          new DynamicPrimitiveLongList(subArrayCapacity);
        }
    ).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> {
          int subArrayCapacity = -2;
          new DynamicPrimitiveLongList(subArrayCapacity);
        }
    ).isInstanceOf(IllegalArgumentException.class);

  }

  @Test
  public void newListIsEmpty() {
    DynamicPrimitiveLongList list = new DynamicPrimitiveLongList();
    assertThat(list).isEmpty();
    assertThatThrownBy(() -> list.getLong(0)).isInstanceOf(IndexOutOfBoundsException.class);
  }


  @Test
  public void resizeListAndSetElement() {
    DynamicPrimitiveLongList list = new DynamicPrimitiveLongList();
    list.resize(5);
    list.setLong(3, 10L);

    for (int i = 0; i < 5; i++) {
      if (i == 3) {
        assertThat(list.getLong(i)).isEqualTo(10L);
      } else {
        assertThat(list.getLong(i)).isEqualTo(0L);
      }
    }
  }

  @Test
  public void resizeAndFillThenResizeSmallerAndCheck() {
    DynamicPrimitiveLongList list = new DynamicPrimitiveLongList();
    list.resize(6);

    for (int i = 0; i < 6; i++) {
      list.setLong(i, i + 1);
    }

    list.resize(3);

    for (int i = 0; i < 3; i++) {
      assertThat(list.getLong(i)).isEqualTo(0L);
    }

    assertThatThrownBy(() -> list.getLong(4)).isInstanceOf(IndexOutOfBoundsException.class);

    for (int i = 0; i < 3; i++) {
      list.setLong(i, i + 10);
      assertThat(list.getLong(i)).isEqualTo(i + 10);
    }
  }

  @Test
  public void resizeToNegativeNumber() {
    assertThatThrownBy( () -> DynamicPrimitiveLongList.of(0, 10, 20).resize(-2))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void resizeAndFillThenResizeLargerAndCheck() {
    DynamicPrimitiveLongList list = new DynamicPrimitiveLongList();
    list.resize(6);

    for (int i = 0; i < 6; i++) {
      list.setLong(i, i + 1);
    }

    list.resize(8);

    for (int i = 0; i < 8; i++) {
      assertThat(list.getLong(i)).isEqualTo(0L);
    }

    assertThatThrownBy(() -> list.getLong(8)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void of() {
    DynamicPrimitiveLongList list = DynamicPrimitiveLongList.of(1, 4, 5, 6);
    assertThat(list.getLong(0)).isEqualTo(1);
    assertThat(list.getLong(1)).isEqualTo(4);
    assertThat(list.getLong(2)).isEqualTo(5);
    assertThat(list.getLong(3)).isEqualTo(6);

    list = DynamicPrimitiveLongList.of();
    assertThat(list).isEmpty();
  }

  @Test
  public void set() {
    DynamicPrimitiveLongList list = DynamicPrimitiveLongList.of(0, 10, 20);
    assertThat(list.get(1)).isEqualTo(10L);

    list.set(1, 100L);
    assertThat(list.get(1)).isEqualTo(100L);
  }
}
