/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReadOnlyArrayMapTest {

  @Test
  void equalsHashCode() {
    Map<String, String> one = ReadOnlyArrayMap.wrap(Arrays.asList("a", "b"));
    Map<String, String> two = ReadOnlyArrayMap.wrap(Arrays.asList("a", "b"));
    Map<String, String> three = ReadOnlyArrayMap.wrap(Arrays.asList("c", "d"));

    new EqualsTester().addEqualityGroup(one, two).addEqualityGroup(three).testEquals();
  }
}
