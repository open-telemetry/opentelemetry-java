/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Comparator;
import org.junit.jupiter.api.Test;

class ImmutableKeyValuePairsTest {

  @Test
  void get() {
    assertThat(new TestPairs(new Object[0]).get("one")).isNull();
    assertThat(new TestPairs(new Object[] {"one", 55}).get("one")).isEqualTo(55);
    assertThat(new TestPairs(new Object[] {"one", 55}).get("two")).isNull();
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b"}).get("one")).isEqualTo(55);
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b"}).get("two")).isEqualTo("b");
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b"}).get("three")).isNull();
    assertThat(new TestPairs(new Object[] {"one", 55, "one", null, "one", 66}).get("one"))
        .isEqualTo(66);
    assertThat(
            new TestPairs(new Object[] {"one", 55, "one", null, "one", 66, "one", null}).get("two"))
        .isNull();
    assertThat(
            new TestPairs(new Object[] {"one", 55, "two", "b", "one", null, "one", 66}).get("one"))
        .isEqualTo(66);
    assertThat(
            new TestPairs(new Object[] {"one", 55, "two", 66, "two", null, "two", 77}).get("two"))
        .isEqualTo(77);
  }

  @Test
  void size() {
    assertThat(new TestPairs(new Object[0]).size()).isEqualTo(0);
    assertThat(new TestPairs(new Object[] {"one", 55}).size()).isEqualTo(1);
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b"}).size()).isEqualTo(2);
    assertThat(new TestPairs(new Object[] {"one", 55, "one", null, "one", 66}).size()).isEqualTo(1);
    assertThat(new TestPairs(new Object[] {"one", 55, "one", null, "one", 66, "one", null}).size())
        .isEqualTo(0);
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b", "one", null, "one", 66}).size())
        .isEqualTo(2);
    assertThat(new TestPairs(new Object[] {"one", 55, "two", 66, "two", null}).size()).isEqualTo(1);
    assertThat(new TestPairs(new Object[] {"one", 55, "two", 66, "two", null, "two", 77}).size())
        .isEqualTo(2);
  }

  @Test
  void isEmpty() {
    assertThat(new TestPairs(new Object[0]).isEmpty()).isTrue();
    assertThat(new TestPairs(new Object[] {"one", 55}).isEmpty()).isFalse();
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b"}).isEmpty()).isFalse();
  }

  @Test
  void toStringIsHumanReadable() {
    assertThat(new TestPairs(new Object[0]).toString()).isEqualTo("{}");
    assertThat(new TestPairs(new Object[] {"one", 55}).toString()).isEqualTo("{one=55}");
    assertThat(new TestPairs(new Object[] {"one", 55, "two", "b"}).toString())
        .isEqualTo("{one=55, two=\"b\"}");
  }

  @Test
  void doesNotCrash() {
    TestPairs pairs = new TestPairs(new Object[0]);
    assertThat(pairs.get(null)).isNull();
    assertThatCode(() -> pairs.forEach(null)).doesNotThrowAnyException();
  }

  static class TestPairs extends ImmutableKeyValuePairs<String, Object> {
    TestPairs(Object[] data) {
      super(data, Comparator.naturalOrder());
    }
  }
}
