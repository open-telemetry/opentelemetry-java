/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class CompositeAndPredicateTest {

  @Test
  void create_Empty() {
    assertThat(CompositeAndPredicate.create(Collections.emptyList()))
        .isSameAs(StringPredicates.ALL);
  }

  @Test
  void create_Single() {
    Predicate<String> pred = s -> true;
    assertThat(CompositeAndPredicate.create(Collections.singletonList(pred))).isSameAs(pred);
  }

  @Test
  void test_LogicalAnd() {
    Predicate<String> p1 = s -> s.startsWith("a");
    Predicate<String> p2 = s -> s.endsWith("z");
    Predicate<String> composite = CompositeAndPredicate.create(Arrays.asList(p1, p2));

    assertThat(composite.test("abz")).isTrue();
    assertThat(composite.test("ab")).isFalse();
    assertThat(composite.test("bz")).isFalse();
  }

  @Test
  void equalsAndHashCode() {
    Predicate<String> p1 = StringPredicates.exact("a");
    Predicate<String> p2 = StringPredicates.exact("b");
    Predicate<String> p3 = StringPredicates.exact("c");

    Predicate<String> c1 = CompositeAndPredicate.create(Arrays.asList(p1, p2));
    Predicate<String> c2 = CompositeAndPredicate.create(Arrays.asList(p1, p2));
    Predicate<String> c3 = CompositeAndPredicate.create(Arrays.asList(p1, p3));

    assertThat(c1).isEqualTo(c2).hasSameHashCodeAs(c2).isNotEqualTo(c3);

    Predicate<String> nested = CompositeAndPredicate.create(Arrays.asList(c1, p3));
    Predicate<String> flat = CompositeAndPredicate.create(Arrays.asList(p1, p2, p3));
    assertThat(nested).isEqualTo(flat).hasSameHashCodeAs(flat);
  }
}
