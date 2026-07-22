/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReadOnlyArrayMapTest {

  @Test
  void equalsHashCode() {
    Map<String, String> one = ReadOnlyArrayMap.wrap(Arrays.asList("a", "b"));
    Map<String, String> two = ReadOnlyArrayMap.wrap(Arrays.asList("a", "b"));
    Map<String, String> three = ReadOnlyArrayMap.wrap(Arrays.asList("c", "d"));
    Map<String, String> empty = ReadOnlyArrayMap.wrap(Collections.emptyList());
    new EqualsTester()
        .addEqualityGroup(one, two)
        .addEqualityGroup(three)
        .addEqualityGroup(empty, empty)
        .testEquals();
  }

  @Test
  void containsValueNull() {
    Map<String, String> map = ReadOnlyArrayMap.wrap(Arrays.asList("a", null));

    assertThat(map.containsValue(null)).isTrue();
    assertThat(map.containsValue("b")).isFalse();
  }
}
