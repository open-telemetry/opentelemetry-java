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

  private static final Predicate<String> exactInclude =
      IncludeExcludePredicate.createExactMatching(singletonList("foo"), null);
  private static final Predicate<String> exactExclude =
      IncludeExcludePredicate.createExactMatching(null, singletonList("bar"));
  private static final Predicate<String> exactIncludeAndExclude =
      IncludeExcludePredicate.createExactMatching(singletonList("foo"), singletonList("bar"));
  private static final Predicate<String> exactMulti =
      IncludeExcludePredicate.createExactMatching(asList("foo", "fooo"), asList("bar", "barr"));

  private static final Predicate<String> patternInclude =
      IncludeExcludePredicate.createPatternMatching(singletonList("f?o"), null);
  private static final Predicate<String> patternExclude =
      IncludeExcludePredicate.createPatternMatching(null, singletonList("b?r"));
  private static final Predicate<String> patternIncludeAndExclude =
      IncludeExcludePredicate.createPatternMatching(singletonList("f?o"), singletonList("b?r"));
  private static final Predicate<String> patternMulti =
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
        Arguments.of(exactInclude, "foo", true),
        Arguments.of(exactInclude, "bar", false),
        Arguments.of(exactInclude, "baz", false),
        // exclude only
        Arguments.of(exactExclude, "foo", true),
        Arguments.of(exactExclude, "bar", false),
        Arguments.of(exactExclude, "baz", true),
        // include and exclude
        Arguments.of(exactIncludeAndExclude, "foo", true),
        Arguments.of(exactIncludeAndExclude, "bar", false),
        Arguments.of(exactIncludeAndExclude, "baz", false),
        // multi
        Arguments.of(exactMulti, "foo", true),
        Arguments.of(exactMulti, "fooo", true),
        Arguments.of(exactMulti, "bar", false),
        Arguments.of(exactMulti, "barr", false),
        Arguments.of(exactMulti, "baz", false),
        // pattern matching
        // include only
        Arguments.of(patternInclude, "foo", true),
        Arguments.of(patternInclude, "bar", false),
        Arguments.of(patternInclude, "baz", false),
        // exclude only
        Arguments.of(patternExclude, "foo", true),
        Arguments.of(patternExclude, "bar", false),
        Arguments.of(patternExclude, "baz", true),
        // include and exclude
        Arguments.of(patternIncludeAndExclude, "foo", true),
        Arguments.of(patternIncludeAndExclude, "bar", false),
        Arguments.of(patternIncludeAndExclude, "baz", false),
        // multi
        Arguments.of(patternMulti, "foo", true),
        Arguments.of(patternMulti, "fooo", true),
        Arguments.of(patternMulti, "bar", false),
        Arguments.of(patternMulti, "barr", false),
        Arguments.of(patternMulti, "baz", false));
  }

  @ParameterizedTest
  @MethodSource("stringRepresentationArgs")
  void stringRepresentation(Predicate<String> predicate, String exepectedString) {
    assertThat(predicate.toString()).isEqualTo(exepectedString);
  }

  private static Stream<Arguments> stringRepresentationArgs() {
    return Stream.of(
        Arguments.of(
            exactInclude, "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo]}"),
        Arguments.of(
            exactExclude, "IncludeExcludePredicate{globMatchingEnabled=false, excluded=[bar]}"),
        Arguments.of(
            exactIncludeAndExclude,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo], excluded=[bar]}"),
        Arguments.of(
            exactMulti,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo, fooo], excluded=[bar, barr]}"),
        Arguments.of(
            patternInclude, "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o]}"),
        Arguments.of(
            patternExclude, "IncludeExcludePredicate{globMatchingEnabled=true, excluded=[b?r]}"),
        Arguments.of(
            patternIncludeAndExclude,
            "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o], excluded=[b?r]}"),
        Arguments.of(
            patternMulti,
            "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o, f?oo], excluded=[b?r, b?rr]}"));
  }
}
