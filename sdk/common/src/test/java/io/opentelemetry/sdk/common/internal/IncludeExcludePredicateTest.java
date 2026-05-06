/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
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
        Arguments.of(EXACT_INCLUDE, "Foo", false),
        Arguments.of(EXACT_INCLUDE, "FOO", false),
        // exclude only
        Arguments.of(EXACT_EXCLUDE, "foo", true),
        Arguments.of(EXACT_EXCLUDE, "bar", false),
        Arguments.of(EXACT_EXCLUDE, "baz", true),
        Arguments.of(EXACT_EXCLUDE, "Bar", true),
        Arguments.of(EXACT_EXCLUDE, "BAR", true),
        // include and exclude
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "foo", true),
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "Foo", false),
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "FOO", false),
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "bar", false),
        Arguments.of(EXACT_INCLUDE_AND_EXCLUDE, "baz", false),
        // multi
        Arguments.of(EXACT_MULTI, "foo", true),
        Arguments.of(EXACT_MULTI, "fooo", true),
        Arguments.of(EXACT_MULTI, "Foo", false),
        Arguments.of(EXACT_MULTI, "FOO", false),
        Arguments.of(EXACT_MULTI, "bar", false),
        Arguments.of(EXACT_MULTI, "barr", false),
        Arguments.of(EXACT_MULTI, "baz", false),
        // pattern matching
        // include only
        Arguments.of(PATTERN_INCLUDE, "foo", true),
        Arguments.of(PATTERN_INCLUDE, "fOo", true),
        Arguments.of(PATTERN_INCLUDE, "Foo", false),
        Arguments.of(PATTERN_INCLUDE, "bar", false),
        Arguments.of(PATTERN_INCLUDE, "baz", false),
        // exclude only
        Arguments.of(PATTERN_EXCLUDE, "foo", true),
        Arguments.of(PATTERN_EXCLUDE, "bar", false),
        Arguments.of(PATTERN_EXCLUDE, "bAr", false),
        Arguments.of(PATTERN_EXCLUDE, "Bar", true),
        Arguments.of(PATTERN_EXCLUDE, "BAR", true),
        Arguments.of(PATTERN_EXCLUDE, "baz", true),
        // include and exclude
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "foo", true),
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "fOo", true),
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "FOO", false),
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "bar", false),
        Arguments.of(PATTERN_INCLUDE_AND_EXCLUDE, "baz", false),
        // multi
        Arguments.of(PATTERN_MULTI, "foo", true),
        Arguments.of(PATTERN_MULTI, "fooo", true),
        Arguments.of(PATTERN_MULTI, "fOo", true),
        Arguments.of(PATTERN_MULTI, "FOO", false),
        Arguments.of(PATTERN_MULTI, "bar", false),
        Arguments.of(PATTERN_MULTI, "bAr", false),
        Arguments.of(PATTERN_MULTI, "barr", false),
        Arguments.of(PATTERN_MULTI, "bArr", false),
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

  @Test
  void emptyOrNullShouldIncludeAll() {
    shouldIncludeAll(null, null);
    shouldIncludeAll(Collections.emptyList(), null);
    shouldIncludeAll(null, Collections.emptyList());
    shouldIncludeAll(Collections.emptyList(), Collections.emptyList());
  }

  private static void shouldIncludeAll(
      @Nullable Collection<String> included, @Nullable Collection<String> excluded) {
    Arrays.asList("foo", "fooo", "fOo", "FOO", "bar", "bAr", "barr", "bArr", "baz")
        .forEach(
            s -> {
              Predicate<String> exactMatching =
                  IncludeExcludePredicate.createExactMatching(included, excluded);
              assertThat(exactMatching.test(s)).isTrue();
              assertThat(exactMatching.toString())
                  .isEqualTo("IncludeExcludePredicate{globMatchingEnabled=false}");
              Predicate<String> patternMatching =
                  IncludeExcludePredicate.createPatternMatching(included, excluded);
              assertThat(patternMatching.test(s)).isTrue();
              assertThat(patternMatching.toString())
                  .isEqualTo("IncludeExcludePredicate{globMatchingEnabled=true}");
            });
  }

  @Test
  void shouldExcludeWhenValueMatchesBothIncludeAndExclude() {
    String value = "a";
    Collection<String> exactMatchingArg = Collections.singletonList("a");

    Predicate<String> exactMatching =
        IncludeExcludePredicate.createExactMatching(exactMatchingArg, exactMatchingArg);
    assertThat(exactMatching.test(value)).isFalse();

    Collection<String> patternMatchingArg = Collections.singletonList("*");
    Predicate<String> patternMatching =
        IncludeExcludePredicate.createPatternMatching(patternMatchingArg, patternMatchingArg);
    assertThat(patternMatching.test(value)).isFalse();
  }
}
