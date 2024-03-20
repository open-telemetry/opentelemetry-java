/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static io.opentelemetry.sdk.internal.GlobUtil.toGlobPatternPredicate;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GlobUtilTest {

  @Test
  void matchesName() {
    assertThat(toGlobPatternPredicate("foo").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("foo").test("Foo")).isTrue();
    assertThat(toGlobPatternPredicate("foo").test("bar")).isFalse();
    assertThat(toGlobPatternPredicate("fo?").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("fo??").test("fooo")).isTrue();
    assertThat(toGlobPatternPredicate("fo?").test("fob")).isTrue();
    assertThat(toGlobPatternPredicate("fo?").test("fooo")).isFalse();
    assertThat(toGlobPatternPredicate("*").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("*").test("bar")).isTrue();
    assertThat(toGlobPatternPredicate("*").test("baz")).isTrue();
    assertThat(toGlobPatternPredicate("*").test("foo.bar.baz")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("fo")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("foo")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("fooo")).isTrue();
    assertThat(toGlobPatternPredicate("fo*").test("foo.bar.baz")).isTrue();
    assertThat(toGlobPatternPredicate("f()[]$^.{}|").test("f()[]$^.{}|")).isTrue();
    assertThat(toGlobPatternPredicate("f()[]$^.{}|?").test("f()[]$^.{}|o")).isTrue();
    assertThat(toGlobPatternPredicate("f()[]$^.{}|*").test("f()[]$^.{}|ooo")).isTrue();
  }
}
