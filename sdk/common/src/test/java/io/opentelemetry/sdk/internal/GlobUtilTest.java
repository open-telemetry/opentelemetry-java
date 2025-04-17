/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static io.opentelemetry.sdk.internal.GlobUtil.createGlobPatternPredicate;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GlobUtilTest {

  @Test
  void matchesName() {
    assertThat(createGlobPatternPredicate("foo").test("foo")).isTrue();
    assertThat(createGlobPatternPredicate("foo").test("Foo")).isTrue();
    assertThat(createGlobPatternPredicate("foo").test("bar")).isFalse();
    assertThat(createGlobPatternPredicate("fo?").test("foo")).isTrue();
    assertThat(createGlobPatternPredicate("fo??").test("fooo")).isTrue();
    assertThat(createGlobPatternPredicate("fo?").test("fob")).isTrue();
    assertThat(createGlobPatternPredicate("fo?").test("fooo")).isFalse();
    assertThat(createGlobPatternPredicate("*").test("foo")).isTrue();
    assertThat(createGlobPatternPredicate("*").test("bar")).isTrue();
    assertThat(createGlobPatternPredicate("*").test("baz")).isTrue();
    assertThat(createGlobPatternPredicate("*").test("foo.bar.baz")).isTrue();
    assertThat(createGlobPatternPredicate("*").test(null)).isTrue();
    assertThat(createGlobPatternPredicate("*").test("")).isTrue();
    assertThat(createGlobPatternPredicate("fo*").test("fo")).isTrue();
    assertThat(createGlobPatternPredicate("fo*").test("foo")).isTrue();
    assertThat(createGlobPatternPredicate("fo*").test("fooo")).isTrue();
    assertThat(createGlobPatternPredicate("fo*").test("foo.bar.baz")).isTrue();
    assertThat(createGlobPatternPredicate("*bar").test("sandbar")).isTrue();
    assertThat(createGlobPatternPredicate("fo*b*").test("foobar")).isTrue();
    assertThat(createGlobPatternPredicate("fo*b*").test("foob")).isTrue();
    assertThat(createGlobPatternPredicate("fo*b*").test("foo bar")).isTrue();
    assertThat(createGlobPatternPredicate("fo? b??").test("foo bar")).isTrue();
    assertThat(createGlobPatternPredicate("fo? b??").test("fooo bar")).isFalse();
    assertThat(createGlobPatternPredicate("fo* ba?").test("foo is not bar")).isTrue();
    assertThat(createGlobPatternPredicate("fo? b*").test("fox beetles for lunch")).isTrue();
    assertThat(createGlobPatternPredicate("f()[]$^.{}|").test("f()[]$^.{}|")).isTrue();
    assertThat(createGlobPatternPredicate("f()[]$^.{}|?").test("f()[]$^.{}|o")).isTrue();
    assertThat(createGlobPatternPredicate("f()[]$^.{}|*").test("f()[]$^.{}|ooo")).isTrue();
  }
}
