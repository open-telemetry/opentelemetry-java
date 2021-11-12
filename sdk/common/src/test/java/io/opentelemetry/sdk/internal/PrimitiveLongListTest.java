/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrimitiveLongListTest {

  @Test
  void wrap() {
    long[] array = new long[] {1, 2};
    List<Long> wrapped = PrimitiveLongList.wrap(array);
    // Standard List operations
    assertThat(wrapped).containsExactly(1L, 2L);
    assertThat(wrapped).hasSize(2);
    assertThatThrownBy(() -> wrapped.get(3))
        .isInstanceOf(IndexOutOfBoundsException.class)
        .hasMessageContaining("3");

    assertThat(PrimitiveLongList.toArray(wrapped)).isSameAs(array).containsExactly(1L, 2L);
  }

  @Test
  void notWrapped() {
    List<Long> list = Arrays.asList(1L, 2L);
    // Standard List operations
    assertThat(list).containsExactly(1L, 2L);
    assertThat(list).hasSize(2);
    assertThatThrownBy(() -> list.get(3))
        .isInstanceOf(IndexOutOfBoundsException.class)
        .hasMessageContaining("3");

    assertThat(PrimitiveLongList.toArray(list)).containsExactly(1L, 2L);
  }
}
