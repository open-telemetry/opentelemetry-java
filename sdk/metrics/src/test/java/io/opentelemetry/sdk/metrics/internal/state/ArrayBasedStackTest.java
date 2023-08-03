package io.opentelemetry.sdk.metrics.internal.state;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
    for(int i = 0; i < ArrayBasedStack.DEFAULT_CAPACITY + 5; i++) {
      stack.push(i);
    }
    assertThat(stack.size()).isEqualTo(ArrayBasedStack.DEFAULT_CAPACITY + 5);
    for(int i = ArrayBasedStack.DEFAULT_CAPACITY + 4; i >= 0; i--) {
      assertThat(stack.pop()).isEqualTo(i);
    }
  }

  @Test
  void testPopOnEmptyStack() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    assertThat(stack.pop()).isNull();
  }

  @Test
  void testPushNullElement() {
    ArrayBasedStack<Integer> stack = new ArrayBasedStack<>();
    assertThatThrownBy(() -> stack.push(null)).isInstanceOf(NullPointerException.class);
  }
}
