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

import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttributesMapTest {

  // ---- put ----

  @Test
  void put_returnsNullForNewEntry() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);

    assertThat(map.put(stringKey("k"), "v")).isNull();
  }

  @Test
  void put_returnsOldValueOnOverwrite() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("k"), "first");

    assertThat(map.put(stringKey("k"), "second")).isEqualTo("first");
    assertThat(map.get(stringKey("k"))).isEqualTo("second");
  }

  @Test
  void put_ignoresNullValue() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("k"), null);

    assertThat(map.size()).isEqualTo(0);
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.getTotalAddedValues()).isEqualTo(0);
  }

  @Test
  void putSameKeyDifferentType_lastValueWins() {
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
  void putSameKeyDifferentType_previousTypeGetReturnsNull() {
    AttributesMap map = AttributesMap.create(128, Integer.MAX_VALUE);
    map.put(stringKey("k"), "hello");
    map.put(booleanKey("k"), true);

    assertThat(map.get(stringKey("k"))).isNull();
    assertThat(map.get(booleanKey("k"))).isEqualTo(true);
  }

  // ---- get ----

  @Test
  void get_returnsNullForAbsentKey() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);

    assertThat(map.get(stringKey("absent"))).isNull();
  }

  // ---- capacity ----

  @Test
  void capacity_dropsEntriesBeyondLimit() {
    AttributesMap map = AttributesMap.create(2, Integer.MAX_VALUE);
    map.put(stringKey("a"), "v1");
    map.put(stringKey("b"), "v2");
    map.put(stringKey("c"), "v3"); // dropped — capacity reached

    assertThat(map.size()).isEqualTo(2);
    assertThat(map.getTotalAddedValues()).isEqualTo(3);
    assertThat(map.get(stringKey("c"))).isNull();
  }

  @Test
  void capacity_zeroDropsAllEntries() {
    AttributesMap map = AttributesMap.create(0, Integer.MAX_VALUE);
    map.put(stringKey("k"), "v");

    assertThat(map.size()).isEqualTo(0);
    assertThat(map.isEmpty()).isTrue();
  }

  // ---- grow ----

  @Test
  void grow_preservesAllEntriesWhenSizeExceedsInitialArrayLength() {
    // init = min(capacity, 16) = 16; grow() is triggered when the 17th entry is inserted
    int n = 20;
    AttributesMap map = AttributesMap.create(n, Integer.MAX_VALUE);
    for (int i = 0; i < n; i++) {
      map.put(stringKey("key" + i), "val" + i);
    }

    assertThat(map.size()).isEqualTo(n);
    for (int i = 0; i < n; i++) {
      assertThat(map.get(stringKey("key" + i))).isEqualTo("val" + i);
    }
  }

  // ---- lengthLimit ----

  @Test
  void lengthLimit_truncatesStringValues() {
    AttributesMap map = AttributesMap.create(10, 3);
    map.put(stringKey("k"), "hello");

    assertThat(map.get(stringKey("k"))).isEqualTo("hel");
  }

  // ---- forEach ----

  @Test
  void forEach_iteratesInInsertionOrder() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("first"), "v1");
    map.put(stringKey("second"), "v2");
    map.put(stringKey("third"), "v3");

    List<String> keys = new ArrayList<>();
    map.forEach((k, v) -> keys.add(k.getKey()));

    assertThat(keys).containsExactly("first", "second", "third");
  }

  // ---- views ----

  @Test
  void asMap() {
    AttributesMap attributesMap = AttributesMap.create(2, Integer.MAX_VALUE);
    attributesMap.put(longKey("one"), 1L);
    attributesMap.put(longKey("two"), 2L);

    assertThat(attributesMap.asMap())
        .containsOnly(entry(longKey("one"), 1L), entry(longKey("two"), 2L));
  }

  @Test
  void immutableCopy_containsAllEntries() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("a"), "v1");
    map.put(longKey("b"), 42L);

    Attributes copy = map.immutableCopy();

    assertThat(copy.get(stringKey("a"))).isEqualTo("v1");
    assertThat(copy.get(longKey("b"))).isEqualTo(42L);
  }
}
