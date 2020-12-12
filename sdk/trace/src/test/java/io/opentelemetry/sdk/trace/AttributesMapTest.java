/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class AttributesMapTest {
  @Test
  void attributesAreInOrder() {
    AttributesMap attributesMap = new AttributesMap(5);
    attributesMap.put(longKey("one"), 1L);
    attributesMap.put(longKey("three"), 3L);
    attributesMap.put(longKey("two"), 2L);
    attributesMap.put(longKey("four"), 4L);
    attributesMap.put(longKey("five"), 5L);

    List<String> expectedKeyOrder = Arrays.asList("one", "three", "two", "four", "five");
    List<Long> expectedValueOrder = Arrays.asList(1L, 3L, 2L, 4L, 5L);

    assertOrdering(attributesMap, expectedKeyOrder, expectedValueOrder);
    assertOrdering(attributesMap.immutableCopy(), expectedKeyOrder, expectedValueOrder);
  }

  @Test
  void asMap() {
    AttributesMap attributesMap = new AttributesMap(2);
    attributesMap.put(longKey("one"), 1L);
    attributesMap.put(longKey("two"), 2L);

    assertThat(attributesMap.asMap())
        .containsExactly(entry(longKey("one"), 1L), entry(longKey("two"), 2L));
  }

  private void assertOrdering(
      Attributes attributes, List<String> expectedKeyOrder, List<Long> expectedValueOrder) {
    attributes.forEach(
        new BiConsumer<AttributeKey<?>, Object>() {
          private int counter = 0;

          @Override
          public void accept(AttributeKey<?> key, Object value) {
            String k = key.getKey();
            Long val = (Long) value;
            assertThat(val).isEqualTo(expectedValueOrder.get(counter));
            assertThat(k).isEqualTo(expectedKeyOrder.get(counter++));
          }
        });
  }
}
