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
        Arguments.argumentSet("exact include foo", EXACT_INCLUDE, "foo", true),
        Arguments.argumentSet("exact include bar no match", EXACT_INCLUDE, "bar", false),
        Arguments.argumentSet("exact include baz no match", EXACT_INCLUDE, "baz", false),
        Arguments.argumentSet("exact include Foo no match", EXACT_INCLUDE, "Foo", false),
        Arguments.argumentSet("exact include FOO no match", EXACT_INCLUDE, "FOO", false),
        Arguments.argumentSet("exact exclude foo", EXACT_EXCLUDE, "foo", true),
        Arguments.argumentSet("exact exclude bar no match", EXACT_EXCLUDE, "bar", false),
        Arguments.argumentSet("exact exclude baz", EXACT_EXCLUDE, "baz", true),
        Arguments.argumentSet("exact exclude Bar", EXACT_EXCLUDE, "Bar", true),
        Arguments.argumentSet("exact exclude BAR", EXACT_EXCLUDE, "BAR", true),
        Arguments.argumentSet("exact include+exclude foo", EXACT_INCLUDE_AND_EXCLUDE, "foo", true),
        Arguments.argumentSet(
            "exact include+exclude Foo no match", EXACT_INCLUDE_AND_EXCLUDE, "Foo", false),
        Arguments.argumentSet(
            "exact include+exclude FOO no match", EXACT_INCLUDE_AND_EXCLUDE, "FOO", false),
        Arguments.argumentSet(
            "exact include+exclude bar no match", EXACT_INCLUDE_AND_EXCLUDE, "bar", false),
        Arguments.argumentSet(
            "exact include+exclude baz no match", EXACT_INCLUDE_AND_EXCLUDE, "baz", false),
        Arguments.argumentSet("exact multi foo", EXACT_MULTI, "foo", true),
        Arguments.argumentSet("exact multi fooo", EXACT_MULTI, "fooo", true),
        Arguments.argumentSet("exact multi Foo no match", EXACT_MULTI, "Foo", false),
        Arguments.argumentSet("exact multi FOO no match", EXACT_MULTI, "FOO", false),
        Arguments.argumentSet("exact multi bar no match", EXACT_MULTI, "bar", false),
        Arguments.argumentSet("exact multi barr no match", EXACT_MULTI, "barr", false),
        Arguments.argumentSet("exact multi baz no match", EXACT_MULTI, "baz", false),
        Arguments.argumentSet("pattern include foo", PATTERN_INCLUDE, "foo", true),
        Arguments.argumentSet("pattern include fOo", PATTERN_INCLUDE, "fOo", true),
        Arguments.argumentSet("pattern include Foo no match", PATTERN_INCLUDE, "Foo", false),
        Arguments.argumentSet("pattern include bar no match", PATTERN_INCLUDE, "bar", false),
        Arguments.argumentSet("pattern include baz no match", PATTERN_INCLUDE, "baz", false),
        Arguments.argumentSet("pattern exclude foo", PATTERN_EXCLUDE, "foo", true),
        Arguments.argumentSet("pattern exclude bar no match", PATTERN_EXCLUDE, "bar", false),
        Arguments.argumentSet("pattern exclude bAr no match", PATTERN_EXCLUDE, "bAr", false),
        Arguments.argumentSet("pattern exclude Bar", PATTERN_EXCLUDE, "Bar", true),
        Arguments.argumentSet("pattern exclude BAR", PATTERN_EXCLUDE, "BAR", true),
        Arguments.argumentSet("pattern exclude baz", PATTERN_EXCLUDE, "baz", true),
        Arguments.argumentSet(
            "pattern include+exclude foo", PATTERN_INCLUDE_AND_EXCLUDE, "foo", true),
        Arguments.argumentSet(
            "pattern include+exclude fOo", PATTERN_INCLUDE_AND_EXCLUDE, "fOo", true),
        Arguments.argumentSet(
            "pattern include+exclude FOO no match", PATTERN_INCLUDE_AND_EXCLUDE, "FOO", false),
        Arguments.argumentSet(
            "pattern include+exclude bar no match", PATTERN_INCLUDE_AND_EXCLUDE, "bar", false),
        Arguments.argumentSet(
            "pattern include+exclude baz no match", PATTERN_INCLUDE_AND_EXCLUDE, "baz", false),
        Arguments.argumentSet("pattern multi foo", PATTERN_MULTI, "foo", true),
        Arguments.argumentSet("pattern multi fooo", PATTERN_MULTI, "fooo", true),
        Arguments.argumentSet("pattern multi fOo", PATTERN_MULTI, "fOo", true),
        Arguments.argumentSet("pattern multi FOO no match", PATTERN_MULTI, "FOO", false),
        Arguments.argumentSet("pattern multi bar no match", PATTERN_MULTI, "bar", false),
        Arguments.argumentSet("pattern multi bAr no match", PATTERN_MULTI, "bAr", false),
        Arguments.argumentSet("pattern multi barr no match", PATTERN_MULTI, "barr", false),
        Arguments.argumentSet("pattern multi bArr no match", PATTERN_MULTI, "bArr", false),
        Arguments.argumentSet("pattern multi baz no match", PATTERN_MULTI, "baz", false));
  }

  @ParameterizedTest
  @MethodSource("stringRepresentationArgs")
  void stringRepresentation(Predicate<String> predicate, String exepectedString) {
    assertThat(predicate.toString()).isEqualTo(exepectedString);
  }

  private static Stream<Arguments> stringRepresentationArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "exact include",
            EXACT_INCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo]}"),
        Arguments.argumentSet(
            "exact exclude",
            EXACT_EXCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=false, excluded=[bar]}"),
        Arguments.argumentSet(
            "exact include and exclude",
            EXACT_INCLUDE_AND_EXCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo], excluded=[bar]}"),
        Arguments.argumentSet(
            "exact multi",
            EXACT_MULTI,
            "IncludeExcludePredicate{globMatchingEnabled=false, included=[foo, fooo], excluded=[bar, barr]}"),
        Arguments.argumentSet(
            "pattern include",
            PATTERN_INCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o]}"),
        Arguments.argumentSet(
            "pattern exclude",
            PATTERN_EXCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=true, excluded=[b?r]}"),
        Arguments.argumentSet(
            "pattern include and exclude",
            PATTERN_INCLUDE_AND_EXCLUDE,
            "IncludeExcludePredicate{globMatchingEnabled=true, included=[f?o], excluded=[b?r]}"),
        Arguments.argumentSet(
            "pattern multi",
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
