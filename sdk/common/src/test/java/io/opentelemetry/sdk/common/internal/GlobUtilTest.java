/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GlobUtilTest {

  @ParameterizedTest
  @MethodSource("globPatternPredicateArgs")
  void matchesName(String globPattern, String testString, boolean isMatchExpected) {
    assertThat(GlobUtil.createGlobPatternPredicate(globPattern).test(testString))
        .isEqualTo(isMatchExpected);
  }

  private static Stream<Arguments> globPatternPredicateArgs() {
    return Stream.of(
        Arguments.argumentSet("foo matches foo", "foo", "foo", true),
        Arguments.argumentSet("foo no match Foo", "foo", "Foo", false),
        Arguments.argumentSet("foo no match bar", "foo", "bar", false),
        Arguments.argumentSet("fo? matches foo", "fo?", "foo", true),
        Arguments.argumentSet("fo?? matches fooo", "fo??", "fooo", true),
        Arguments.argumentSet("fo? matches fob", "fo?", "fob", true),
        Arguments.argumentSet("fo? no match fooo", "fo?", "fooo", false),
        Arguments.argumentSet("* matches foo", "*", "foo", true),
        Arguments.argumentSet("* matches bar", "*", "bar", true),
        Arguments.argumentSet("* matches baz", "*", "baz", true),
        Arguments.argumentSet("* matches foo.bar.baz", "*", "foo.bar.baz", true),
        Arguments.argumentSet("* matches null", "*", null, true),
        Arguments.argumentSet("* matches empty", "*", "", true),
        Arguments.argumentSet("fo* matches fo", "fo*", "fo", true),
        Arguments.argumentSet("fo* matches foo", "fo*", "foo", true),
        Arguments.argumentSet("fo* matches foO", "fo*", "foO", true),
        Arguments.argumentSet("fo* no match FOO", "fo*", "FOO", false),
        Arguments.argumentSet("fo* matches fooo", "fo*", "fooo", true),
        Arguments.argumentSet("fo* matches foo.bar.baz", "fo*", "foo.bar.baz", true),
        Arguments.argumentSet("*bar matches sandbar", "*bar", "sandbar", true),
        Arguments.argumentSet("fo*b* matches foobar", "fo*b*", "foobar", true),
        Arguments.argumentSet("fo*b* matches foob", "fo*b*", "foob", true),
        Arguments.argumentSet("fo*b* matches foo bar", "fo*b*", "foo bar", true),
        Arguments.argumentSet("fo? b?? matches foo bar", "fo? b??", "foo bar", true),
        Arguments.argumentSet("fo? b?? matches foO bAR", "fo? b??", "foO bAR", true),
        Arguments.argumentSet("fo? b?? no match FOO BAR", "fo? b??", "FOO BAR", false),
        Arguments.argumentSet("fo? b?? no match fooo bar", "fo? b??", "fooo bar", false),
        Arguments.argumentSet("fo* ba? matches foo is not bar", "fo* ba?", "foo is not bar", true),
        Arguments.argumentSet(
            "fo? b* matches fox beetles for lunch", "fo? b*", "fox beetles for lunch", true),
        Arguments.argumentSet("special chars literal", "f()[]$^.{}|", "f()[]$^.{}|", true),
        Arguments.argumentSet("special chars with ?", "f()[]$^.{}|?", "f()[]$^.{}|o", true),
        Arguments.argumentSet("special chars with *", "f()[]$^.{}|*", "f()[]$^.{}|ooo", true));
  }
}
