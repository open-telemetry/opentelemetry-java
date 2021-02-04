/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImmutableKeyValuePairsTest {

  @Test
  void get() {
    assertThat(new TestPairs(Collections.emptyList()).get("one")).isNull();
    assertThat(new TestPairs(Arrays.asList("one", 55)).get("one")).isEqualTo(55);
    assertThat(new TestPairs(Arrays.asList("one", 55)).get("two")).isNull();
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).get("one")).isEqualTo(55);
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).get("two")).isEqualTo("b");
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).get("three")).isNull();
  }

  @Test
  void size() {
    assertThat(new TestPairs(Collections.emptyList()).size()).isEqualTo(0);
    assertThat(new TestPairs(Arrays.asList("one", 55)).size()).isEqualTo(1);
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).size()).isEqualTo(2);
  }

  @Test
  void isEmpty() {
    assertThat(new TestPairs(Collections.emptyList()).isEmpty()).isTrue();
    assertThat(new TestPairs(Arrays.asList("one", 55)).isEmpty()).isFalse();
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).isEmpty()).isFalse();
  }

  @Test
  void toStringIsHumanReadable() {
    assertThat(new TestPairs(Collections.emptyList()).toString()).isEqualTo("{}");
    assertThat(new TestPairs(Arrays.asList("one", 55)).toString()).isEqualTo("{one=55}");
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).toString())
        .isEqualTo("{one=55, two=\"b\"}");
  }

  static class TestPairs extends ImmutableKeyValuePairs<String, Object> {
    TestPairs(List<Object> data) {
      super(data);
    }
  }
}
