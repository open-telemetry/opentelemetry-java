/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IncludeExcludePredicateTest {

  private static final Predicate<String> EXACT_INCLUDE =
      IncludeExcludePredicate.createExactMatching(singletonList("foo"), null);
  private static final Predicate<String> EXACT_EXCLUDE =
      IncludeExcludePredicate.createExactMatching(null, singletonList("bar"));
  private static final Predicate<String> EXACT_INCLUDE_AND_EXCLUDE =
      IncludeExcludePredicate.createExactMatching(singletonList("foo"), singletonList("bar"));
  private static final Predicate<String> EXACT_MULTI =
      IncludeExcludePredicate.createExactMatching(asList("foo", "fooo"), asList("bar", "barr"));

  private static final Predicate<String> PATTERN_INCLUDE =
      IncludeExcludePredicate.createPatternMatching(singletonList("f?o"), null);
  private static final Predicate<String> PATTERN_EXCLUDE =
      IncludeExcludePredicate.createPatternMatching(null, singletonList("b?r"));
  private static final Predicate<String> PATTERN_INCLUDE_AND_EXCLUDE =
      IncludeExcludePredicate.createPatternMatching(singletonList("f?o"), singletonList("b?r"));
  private static final Predicate<String> PATTERN_MULTI =
      IncludeExcludePredicate.createPatternMatching(asList("f?o", "f?oo"), asList("b?r", "b?rr"));

  @ParameterizedTest
  @MethodSource("testArgs")
  void test(Predicate<String> predicate, String testCase, boolean expectedResult) {
    assertThat(predicate.test(testCase)).isEqualTo(expectedResult);
  }

  private static Stream<Arguments> testArgs() {
    return Stream.of(
        // exact matching
        // include only
        Arguments.of(EXACT_INCLUDE, "foo", true),
        Arguments.of(EXACT_INCLUDE, "bar", false),
        Arguments.of(EXACT_INCLUDE, "baz", false),
        // exclude only
        Arguments.of(EXACT_EXCLUDE, "foo", true),
        Arguments.of(EXACT_EXCLUDE, "bar", false),
        Arguments.of(EXACT_EXCLUDE, "baz", true),
        // include and exclude
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "foo", true),
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "bar", false),
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "baz", false),
        // multi
        Arguments.of(EXACT_MULTI, "foo", true),
        Arguments.of(EXACT_MULTI, "fooo", true),
        Arguments.of(EXACT_MULTI, "bar", false),
        Arguments.of(EXACT_MULTI, "barr", false),
        Arguments.of(EXACT_MULTI, "baz", false),
        // pattern matching
        // include only
        Arguments.of(PATTERN_INCLUDE, "foo", true),
        Arguments.of(PATTERN_INCLUDE, "bar", false),
        Arguments.of(PATTERN_INCLUDE, "baz", false),
        // exclude only
        Arguments.of(PATTERN_EXCLUDE, "foo", true),
        Arguments.of(PATTERN_EXCLUDE, "bar", false),
        Arguments.of(PATTERN_EXCLUDE, "baz", true),
        // include and exclude
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "foo", true),
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "bar", false),
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "baz", false),
        // multi
        Arguments.of(PATTERN_MULTI, "foo", true),
        Arguments.of(PATTERN_MULTI, "fooo", true),
        Arguments.of(PATTERN_MULTI, "bar", false),
        Arguments.of(PATTERN_MULTI, "barr", false),
        Arguments.of(PATTERN_MULTI, "baz", false));
  }

  @ParameterizedTest
  @MethodSource("stringRepresentationArgs")
  void stringRepresentation(Predicate<String> predicate, String exepectedString) {
    assertThat(predicate.toString()).isEqualTo(exepectedString);
  }

  private static Stream<Arguments> stringRepresentationArgs() {
    return Stream.of(
        Arguments.of(
            EXACT_INCLUDE, "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo]}"),
        Arguments.of(
            EXACT_EXCLUDE, "IncludeExcludePredicate{globMatchingEnabled=false, excluded=[bar]}"),
        Arguments.of(
            EXACT_INCLUDE_AND_EXCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo], excluded=[bar]}"),
        Arguments.of(
            EXACT_MULTI,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo, fooo], excluded=[bar, barr]}"),
        Arguments.of(
            PATTERN_INCLUDE, "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o]}"),
        Arguments.of(
            PATTERN_EXCLUDE, "IncludeExcludePredicate{globMatchingEnabled=true, excluded=[b?r]}"),
        Arguments.of(
            PATTERN_INCLUDE_AND_EXCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o], excluded=[b?r]}"),
        Arguments.of(
            PATTERN_MULTI,
            "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o, f?oo], excluded=[b?r, b?rr]}"));
  }
}
