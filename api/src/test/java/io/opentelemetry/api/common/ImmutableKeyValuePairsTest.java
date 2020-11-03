/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImmutableKeyValuePairsTest {

  @Test
  void toStringIsCorrect() {
    assertThat(new TestPairs(Collections.emptyList()).toString()).isEqualTo("{}");
    assertThat(new TestPairs(Arrays.asList("one", 55)).toString()).isEqualTo("{one=55}");
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).toString())
        .isEqualTo("{one=55, two=b}");
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).toString())
        .isEqualTo(ImmutableMap.of("one", 55, "two", "b").toString());
  }

  static class TestPairs extends ImmutableKeyValuePairs<String, Object> {
    private final List<Object> data;

    TestPairs(List<Object> data) {
      this.data = data;
    }

    @Override
    List<Object> data() {
      return data;
    }
  }
}
