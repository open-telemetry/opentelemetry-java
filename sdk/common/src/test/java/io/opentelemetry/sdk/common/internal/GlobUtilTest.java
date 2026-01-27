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
        Arguments.of("foo", "foo", true),
        Arguments.of("foo", "Foo", true),
        Arguments.of("foo", "bar", false),
        Arguments.of("fo?", "foo", true),
        Arguments.of("fo??", "fooo", true),
        Arguments.of("fo?", "fob", true),
        Arguments.of("fo?", "fooo", false),
        Arguments.of("*", "foo", true),
        Arguments.of("*", "bar", true),
        Arguments.of("*", "baz", true),
        Arguments.of("*", "foo.bar.baz", true),
        Arguments.of("*", null, true),
        Arguments.of("*", "", true),
        Arguments.of("fo*", "fo", true),
        Arguments.of("fo*", "foo", true),
        Arguments.of("fo*", "fooo", true),
        Arguments.of("fo*", "foo.bar.baz", true),
        Arguments.of("*bar", "sandbar", true),
        Arguments.of("fo*b*", "foobar", true),
        Arguments.of("fo*b*", "foob", true),
        Arguments.of("fo*b*", "foo bar", true),
        Arguments.of("fo? b??", "foo bar", true),
        Arguments.of("fo? b??", "fooo bar", false),
        Arguments.of("fo* ba?", "foo is not bar", true),
        Arguments.of("fo? b*", "fox beetles for lunch", true),
        Arguments.of("f()[]$^.{}|", "f()[]$^.{}|", true),
        Arguments.of("f()[]$^.{}|?", "f()[]$^.{}|o", true),
        Arguments.of("f()[]$^.{}|*", "f()[]$^.{}|ooo", true));
  }
}
