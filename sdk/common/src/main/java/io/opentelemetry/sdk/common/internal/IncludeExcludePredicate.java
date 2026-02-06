/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static io.opentelemetry.sdk.common.internal.GlobUtil.createGlobPatternPredicate;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * A predicate that evaluates if a string matches a configurable set of {@code included} and {@code
 * excluded} string.
 *
 * <p>Supports optional glob pattern matching. See {@link GlobUtil}.
 *
 * <p>String equality is evaluated using {@link String#equalsIgnoreCase(String)}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class IncludeExcludePredicate implements Predicate<String> {

  private final boolean globMatchingEnabled;
  @Nullable private final Set<String> included;
  @Nullable private final Set<String> excluded;
  private final Predicate<String> predicate;

  private IncludeExcludePredicate(
      @Nullable Collection<String> included,
      @Nullable Collection<String> excluded,
      boolean globMatchingEnabled) {
    this.globMatchingEnabled = globMatchingEnabled;
    this.included = included == null ? null : new LinkedHashSet<>(included);
    this.excluded = excluded == null ? null : new LinkedHashSet<>(excluded);
    if (this.included != null && this.excluded != null) {
      this.predicate =
          includedPredicate(this.included, globMatchingEnabled)
              .and(excludedPredicate(this.excluded, globMatchingEnabled));
    } else if (this.included == null && this.excluded != null) {
      this.predicate = excludedPredicate(this.excluded, globMatchingEnabled);
    } else if (this.excluded == null && this.included != null) {
      this.predicate = includedPredicate(this.included, globMatchingEnabled);
    } else {
      throw new IllegalArgumentException(
          "At least one of includedPatterns or excludedPatterns must not be null");
    }
  }

  /**
   * Create a (case-insensitive) exact matching include exclude predicate.
   *
   * @throws IllegalArgumentException if {@code included} AND {@code excluded} are null.
   */
  public static Predicate<String> createExactMatching(
      @Nullable Collection<String> included, @Nullable Collection<String> excluded) {
    return new IncludeExcludePredicate(included, excluded, /* globMatchingEnabled= */ false);
  }

  /**
   * Create a pattern matching include exclude predicate.
   *
   * <p>See {@link GlobUtil} for pattern matching details.
   *
   * @throws IllegalArgumentException if {@code included} AND {@code excluded} are null.
   */
  public static Predicate<String> createPatternMatching(
      @Nullable Collection<String> included, @Nullable Collection<String> excluded) {
    return new IncludeExcludePredicate(included, excluded, /* globMatchingEnabled= */ true);
  }

  @Override
  public boolean test(String s) {
    return predicate.test(s);
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "IncludeExcludePredicate{", "}");
    joiner.add("globMatchingEnabled=" + globMatchingEnabled);
    if (included != null) {
      joiner.add("included=" + included.stream().collect(joining(", ", "[", "]")));
    }
    if (excluded != null) {
      joiner.add("excluded=" + excluded.stream().collect(joining(", ", "[", "]")));
    }
    return joiner.toString();
  }

  private static Predicate<String> includedPredicate(
      Set<String> included, boolean globMatchingEnabled) {
    Predicate<String> result = attributeKey -> false;
    for (String include : included) {
      if (globMatchingEnabled) {
        result = result.or(createGlobPatternPredicate(include));
      } else {
        result = result.or(include::equalsIgnoreCase);
      }
    }
    return result;
  }

  private static Predicate<String> excludedPredicate(
      Set<String> excluded, boolean globMatchingEnabled) {
    Predicate<String> result = attributeKey -> true;
    for (String exclude : excluded) {
      if (globMatchingEnabled) {
        result = result.and(createGlobPatternPredicate(exclude).negate());
      } else {
        result = result.and(s -> !exclude.equalsIgnoreCase(s));
      }
    }
    return result;
  }
}
