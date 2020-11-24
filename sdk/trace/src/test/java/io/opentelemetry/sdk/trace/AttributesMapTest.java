/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.ReadableAttributes;
import java.util.Arrays;
import java.util.List;
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

  private void assertOrdering(
      ReadableAttributes attributes, List<String> expectedKeyOrder, List<Long> expectedValueOrder) {
    attributes.forEach(
        new AttributeConsumer() {
          private int counter = 0;

          @Override
          public <T> void accept(AttributeKey<T> key, T value) {
            String k = key.getKey();
            Long val = (Long) value;
            assertThat(val).isEqualTo(expectedValueOrder.get(counter));
            assertThat(k).isEqualTo(expectedKeyOrder.get(counter++));
          }
        });
  }
}
