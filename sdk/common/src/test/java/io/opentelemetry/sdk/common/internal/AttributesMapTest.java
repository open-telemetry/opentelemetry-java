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

import io.opentelemetry.api.common.AttributeKey;
import java.util.HashMap;
import java.util.Map;
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
  void put_lastValueWins_differentTypes() {
    AttributesMap attributesMap = AttributesMap.create(10, Integer.MAX_VALUE);
    attributesMap.put(stringKey("key"), "value");
    attributesMap.put(booleanKey("key"), false);

    assertThat(attributesMap.asMap()).containsOnly(entry(booleanKey("key"), false));
  }

  @Test
  void put_lastValueWins_sameType() {
    AttributesMap attributesMap = AttributesMap.create(10, Integer.MAX_VALUE);
    attributesMap.put(stringKey("key"), "value1");
    attributesMap.put(stringKey("key"), "value2");

    assertThat(attributesMap.asMap()).containsOnly(entry(stringKey("key"), "value2"));
  }

  @Test
  void putAll() {
    AttributesMap attributesMap = AttributesMap.create(10, Integer.MAX_VALUE);
    Map<AttributeKey<?>, Object> newAttrs = new HashMap<>();
    newAttrs.put(stringKey("key1"), "value1");
    newAttrs.put(booleanKey("key2"), true);
    attributesMap.putAll(newAttrs);

    assertThat(attributesMap.asMap())
        .containsOnly(entry(stringKey("key1"), "value1"), entry(booleanKey("key2"), true));
  }
}
