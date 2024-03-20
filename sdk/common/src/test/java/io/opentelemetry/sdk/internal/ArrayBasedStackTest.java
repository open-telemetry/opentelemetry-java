/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ArrayBasedStackTest {

  @Test
  void testPushAndPop() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    stack.push(1);
    stack.push(2);
    assertThat(stack.pop()).isEqualTo(2);
    assertThat(stack.pop()).isEqualTo(1);
  }

  @Test
  void testIsEmpty() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    assertThat(stack.isEmpty()).isTrue();
    stack.push(1);
    assertThat(stack.isEmpty()).isFalse();
  }

  @Test
  void testSize() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    assertThat(stack.size()).isEqualTo(0);
    stack.push(1);
    assertThat(stack.size()).isEqualTo(1);
  }

  @Test
  void testPushBeyondInitialCapacity() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    for (int i = 0; i < ArrayBasedStack.DEFAULT_CAPACITY + 5; i++) {
      stack.push(i);
    }
    assertThat(stack.size()).isEqualTo(ArrayBasedStack.DEFAULT_CAPACITY + 5);
    for (int i = ArrayBasedStack.DEFAULT_CAPACITY + 4; i >= 0; i--) {
      assertThat(stack.pop()).isEqualTo(i);
    }
  }

  @Test
  void testPopOnEmptyStack() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    assertThat(stack.pop()).isNull();
  }
}
