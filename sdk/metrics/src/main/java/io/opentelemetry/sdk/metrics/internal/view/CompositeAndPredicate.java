/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable composite predicate that evaluates the logical AND of a list of predicates,
 * providing value-based equality and hash code over its constituent predicates.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class CompositeAndPredicate implements Predicate<String> {

  private final List<Predicate<String>> predicates;

  /** Creates a composite AND predicate from the provided predicates. */
  public static Predicate<String> create(List<Predicate<String>> predicates) {
    if (predicates.isEmpty()) {
      return StringPredicates.ALL;
    }
    if (predicates.size() == 1) {
      return predicates.get(0);
    }
    List<Predicate<String>> flattened = new ArrayList<>(predicates.size());
    for (Predicate<String> predicate : predicates) {
      if (predicate instanceof CompositeAndPredicate) {
        flattened.addAll(((CompositeAndPredicate) predicate).predicates);
      } else {
        flattened.add(predicate);
      }
    }
    return new CompositeAndPredicate(Collections.unmodifiableList(flattened));
  }

  private CompositeAndPredicate(List<Predicate<String>> predicates) {
    this.predicates = predicates;
  }

  @Override
  public boolean test(String s) {
    for (Predicate<String> predicate : predicates) {
      if (!predicate.test(s)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CompositeAndPredicate)) {
      return false;
    }
    CompositeAndPredicate that = (CompositeAndPredicate) o;
    return Objects.equals(this.predicates, that.predicates);
  }

  @Override
  public int hashCode() {
    return predicates.hashCode();
  }

  @Override
  public String toString() {
    return "CompositeAndPredicate{" + predicates + "}";
  }
}
