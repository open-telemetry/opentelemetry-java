/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

class AttributesMapTest {

  @Test
  void asMap() {
    AttributesMap attributesMap = AttributesMap.create(2, Integer.MAX_VALUE);
    attributesMap.put(longKey("one"), 1L);
    attributesMap.put(longKey("two"), 2L);

    assertThat(attributesMap.asMap())
        .containsOnly(entry(longKey("one"), 1L), entry(longKey("two"), 2L));
  }

  @Test
  void putSameKeyDifferentType_lastWins() {
    AttributesMap map = AttributesMap.create(128, Integer.MAX_VALUE);
    map.put(stringKey("k"), "hello");
    map.put(booleanKey("k"), false);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.get(booleanKey("k"))).isEqualTo(false);
    assertThat(map.get(stringKey("k"))).isNull();
  }

  @Test
  void putSameKeyDifferentType_doesNotConsumeExtraCapacity() {
    AttributesMap map = AttributesMap.create(2, Integer.MAX_VALUE);
    map.put(stringKey("a"), "v1");
    map.put(booleanKey("a"), false); // overwrite — must not consume a new capacity slot
    map.put(longKey("b"), 42L);

    assertThat(map.size()).isEqualTo(2);
    assertThat(map.get(booleanKey("a"))).isEqualTo(false);
    assertThat(map.get(longKey("b"))).isEqualTo(42L);
  }

  @Test
  void putSameKeyDifferentType_getByOldTypeMissAfterOverwrite() {
    AttributesMap map = AttributesMap.create(128, Integer.MAX_VALUE);
    map.put(stringKey("k"), "hello");
    map.put(booleanKey("k"), true);

    assertThat(map.get(stringKey("k"))).isNull();
    assertThat(map.get(booleanKey("k"))).isEqualTo(true);
  }

}
