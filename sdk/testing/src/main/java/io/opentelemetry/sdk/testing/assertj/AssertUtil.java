/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;

final class AssertUtil {

  /**
   * Converts from consumers of our assertions provided by the user to consumers of the data for use
   * with satisfiesExactlyInAnyOrder.
   */
  static <T, U extends AbstractAssert<U, T>> Consumer<T>[] toConsumers(
      Iterable<? extends Consumer<U>> assertions, Function<T, U> assertionFactory) {
    Stream.Builder<Consumer<T>> builder = Stream.builder();
    for (Consumer<U> assertion : assertions) {
      builder.add(item -> assertion.accept(assertionFactory.apply(item)));
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    Consumer<T>[] consumers = builder.build().toArray(Consumer[]::new);
    return consumers;
  }

  static void assertAttributes(Attributes actual, Iterable<AttributeAssertion> assertions) {
    assertAttributes(actual, assertions, "attribute keys");
  }

  static void assertAttributes(
      Attributes actual, Iterable<AttributeAssertion> assertions, String name) {
    Set<AttributeKey<?>> actualKeys = actual.asMap().keySet();
    Set<AttributeKey<?>> checkedKeys = new HashSet<>();
    for (AttributeAssertion attributeAssertion : assertions) {
      AttributeKey<?> key = attributeAssertion.getKey();
      Object value = actual.get(key);
      if (value != null) {
        checkedKeys.add(key);
      }
      AbstractAssert<?, ?> assertion = AttributeAssertion.attributeValueAssertion(key, value);
      attributeAssertion.getAssertion().accept(assertion);
    }

    assertThat(actualKeys).as(name).containsAll(checkedKeys);
  }

  static void assertAttributesExactly(Attributes actual, Iterable<AttributeAssertion> assertions) {
    assertAttributesExactly(actual, assertions, "attribute keys");
  }

  static void assertAttributesExactly(
      Attributes actual, Iterable<AttributeAssertion> assertions, String name) {
    Set<AttributeKey<?>> actualKeys = actual.asMap().keySet();
    Set<AttributeKey<?>> checkedKeys = new HashSet<>();
    for (AttributeAssertion attributeAssertion : assertions) {
      AttributeKey<?> key = attributeAssertion.getKey();
      Object value = actual.get(key);
      if (value != null) {
        checkedKeys.add(key);
      }
      AbstractAssert<?, ?> assertion = AttributeAssertion.attributeValueAssertion(key, value);
      attributeAssertion.getAssertion().accept(assertion);
    }

    assertThat(actualKeys).as(name).containsExactlyInAnyOrderElementsOf(checkedKeys);
  }

  /**
   * Compares {@link Attributes} as maps since currently attributes cannot be compared across
   * implementations.
   */
  static boolean attributesAreEqual(Attributes actual, Attributes other) {
    return actual.asMap().equals(other.asMap());
  }

  private AssertUtil() {}
}
