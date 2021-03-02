/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReadOnlyArrayMapTest {

  @Test
  void equalsHashCode() {
    Map<String, String> one = ReadOnlyArrayMap.wrap(Arrays.asList("a", "b"));
    Map<String, String> two = ReadOnlyArrayMap.wrap(Arrays.asList("a", "b"));
    Map<String, String> three = ReadOnlyArrayMap.wrap(Arrays.asList("c", "d"));

    assertThat(one).isEqualTo(two);
    assertThat(one).isEqualTo(one);
    assertThat(two).isEqualTo(one);
    assertThat(two).isEqualTo(two);
    assertThat(one).isNotEqualTo(three);
    assertThat(two).isNotEqualTo(three);

    assertThat(one.hashCode()).isEqualTo(two.hashCode());
    assertThat(one.hashCode()).isNotEqualTo(three.hashCode());
  }
}