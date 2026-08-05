/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

  @Test
  void lengthLimit_failureDoesNotInsertPartialEntry() {
    AttributesMap map = AttributesMap.create(10, 3);

    assertThatThrownBy(() -> map.put(stringArrayKey("k"), throwingList()))
        .isInstanceOf(IllegalStateException.class);

    assertThat(map.isEmpty()).isTrue();
    assertThat(map.asMap()).isEmpty();
  }

  @Test
  void lengthLimit_failureDoesNotPartiallyOverwriteEntry() {
    AttributesMap map = AttributesMap.create(10, 3);
    map.put(stringKey("k"), "old");

    assertThatThrownBy(() -> map.put(stringArrayKey("k"), throwingList()))
        .isInstanceOf(IllegalStateException.class);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.get(stringKey("k"))).isEqualTo("old");
    assertThat(map.get(stringArrayKey("k"))).isNull();
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

  // ---- hash collisions ----

  @Test
  void hashCollision_bothEntriesStoredAndRetrievable() {
    // "Aa".hashCode() == "BB".hashCode() == 2112: collide in any table size.
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("Aa"), "v-Aa");
    map.put(stringKey("BB"), "v-BB");

    assertThat(map.size()).isEqualTo(2);
    assertThat(map.get(stringKey("Aa"))).isEqualTo("v-Aa");
    assertThat(map.get(stringKey("BB"))).isEqualTo("v-BB");
  }

  @Test
  void findSlot_wrapsAroundEndOfTable() {
    // capacity=4 => mask=7. "o" (111) and "w" (119) both hash to slot 7; the second wraps to 0.
    AttributesMap map = AttributesMap.create(4, Integer.MAX_VALUE);
    map.put(stringKey("o"), "v-o");
    map.put(stringKey("w"), "v-w");

    assertThat(map.size()).isEqualTo(2);
    assertThat(map.get(stringKey("o"))).isEqualTo("v-o");
    assertThat(map.get(stringKey("w"))).isEqualTo("v-w");
  }

  @Test
  void grow_preservesEntriesIncludingPreExistingCollision() {
    // capacity=20 => init=16 => grow triggers on 17th insert. "Aa"/"BB" collide at slot 0 both
    // before and after grow (2112 & 31 == 2112 & 63 == 0), so rehash must preserve the probe path.
    AttributesMap map = AttributesMap.create(20, Integer.MAX_VALUE);
    map.put(stringKey("Aa"), "v-Aa");
    map.put(stringKey("BB"), "v-BB");
    for (int i = 0; i < 18; i++) {
      map.put(stringKey("k" + i), "v" + i);
    }

    assertThat(map.size()).isEqualTo(20);
    assertThat(map.get(stringKey("Aa"))).isEqualTo("v-Aa");
    assertThat(map.get(stringKey("BB"))).isEqualTo("v-BB");
    for (int i = 0; i < 18; i++) {
      assertThat(map.get(stringKey("k" + i))).isEqualTo("v" + i);
    }
  }

  // ---- concurrent modification detection ----

  @Test
  void forEach_throwsCmeOnConcurrentModification() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("a"), "v1");
    map.put(stringKey("b"), "v2");

    assertThatThrownBy(() -> map.forEach((k, v) -> map.put(stringKey("c"), "v3")))
        .isInstanceOf(ConcurrentModificationException.class);
  }

  @Test
  void forEach_overwriteDuringIterationThrowsCme() {
    // Overwrite (no size change) still bumps modCount.
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("a"), "v1");

    assertThatThrownBy(() -> map.forEach((k, v) -> map.put(stringKey("a"), "v2")))
        .isInstanceOf(ConcurrentModificationException.class);
  }

  @Test
  void forEach_noModification_doesNotThrow() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("a"), "v1");
    map.put(stringKey("b"), "v2");

    map.forEach((k, v) -> {});
  }

  // ---- fuzz ----

  @Test
  void fuzz_matchesReferenceHashMap() {
    // Random puts vs reference HashMap. Exercises grow, overwrites, and type-varying puts to
    // the same name. Fixed seed for reproducibility.
    long seed = 0xC0FFEEL;
    Random r = new Random(seed);
    int capacity = 1000;
    int ops = 5000;
    int namePoolSize = 200; // ~25 overwrites per name on average

    AttributesMap map = AttributesMap.create(capacity, Integer.MAX_VALUE);
    Map<String, Map.Entry<AttributeKey<?>, Object>> reference = new HashMap<>();

    for (int i = 0; i < ops; i++) {
      String name = "key" + r.nextInt(namePoolSize);
      AttributeKey<?> key;
      Object value;
      switch (r.nextInt(3)) {
        case 0:
          key = stringKey(name);
          value = "s" + i;
          break;
        case 1:
          key = longKey(name);
          value = (long) i;
          break;
        default:
          key = booleanKey(name);
          value = (i & 1) == 0;
          break;
      }
      map.put(key, value);
      reference.put(name, new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    assertThat(map.size()).isEqualTo(reference.size());
    for (Map.Entry<String, Map.Entry<AttributeKey<?>, Object>> refEntry : reference.entrySet()) {
      AttributeKey<?> expectedKey = refEntry.getValue().getKey();
      Object expectedValue = refEntry.getValue().getValue();
      assertThat(map.get(expectedKey)).as("key=%s", expectedKey).isEqualTo(expectedValue);
    }
  }

  @Test
  void equals_andHashCode() {
    AttributesMap mapV1a = AttributesMap.create(10, Integer.MAX_VALUE);
    mapV1a.put(stringKey("k"), "v1");
    AttributesMap mapV1b = AttributesMap.create(10, Integer.MAX_VALUE);
    mapV1b.put(stringKey("k"), "v1");
    AttributesMap mapV2 = AttributesMap.create(10, Integer.MAX_VALUE);
    mapV2.put(stringKey("k"), "v2");

    new EqualsTester().addEqualityGroup(mapV1a, mapV1b).addEqualityGroup(mapV2).testEquals();
  }

  @Test
  void equals_isSymmetricWithOtherAttributesImplementations() {
    AttributesMap map = AttributesMap.create(10, Integer.MAX_VALUE);
    map.put(stringKey("k"), "v");
    Attributes attributes = Attributes.of(stringKey("k"), "v");

    assertThat(map).isNotEqualTo(attributes);
    assertThat(attributes).isNotEqualTo(map);
  }

  private static List<String> throwingList() {
    return new AbstractList<String>() {
      @Override
      public String get(int index) {
        throw new IllegalStateException("test");
      }

      @Override
      public int size() {
        return 1;
      }
    };
  }
}
