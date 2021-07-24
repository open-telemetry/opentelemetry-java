/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Attributes}s. */
@SuppressWarnings("rawtypes")
class AttributesTest {

  @Test
  void forEach() {
    final Map<AttributeKey, Object> entriesSeen = new LinkedHashMap<>();

    Attributes attributes = Attributes.of(stringKey("key1"), "value1", longKey("key2"), 333L);

    attributes.forEach(entriesSeen::put);

    assertThat(entriesSeen)
        .containsExactly(entry(stringKey("key1"), "value1"), entry(longKey("key2"), 333L));
  }

  @Test
  void forEach_singleAttribute() {
    final Map<AttributeKey, Object> entriesSeen = new HashMap<>();

    Attributes attributes = Attributes.of(stringKey("key"), "value");
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(stringKey("key"), "value"));
  }

  @Test
  void putAll() {
    Attributes attributes = Attributes.of(stringKey("key1"), "value1", longKey("key2"), 333L);
    assertThat(Attributes.builder().put(booleanKey("key3"), true).putAll(attributes).build())
        .isEqualTo(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, booleanKey("key3"), true));
  }

  @Test
  void putAll_null() {
    assertThat(Attributes.builder().put(booleanKey("key3"), true).putAll(null).build())
        .isEqualTo(Attributes.of(booleanKey("key3"), true));
  }

  @Test
  void removeAll() {
    Attributes complete = Attributes.builder().put("x", 1).put("y", 2).put("z", 3).build();
    Attributes removed = Attributes.builder().put("y", 2).build();
    assertThat(complete.removeAll(removed))
        .isEqualTo(Attributes.builder().put("x", 1).put("z", 3).build());
    assertThat(complete.removeAll(complete)).isEqualTo(Attributes.empty());
    assertThat(removed.removeAll(complete)).isEqualTo(Attributes.empty());

    Attributes removedWithExtra = Attributes.builder().put("b", 2).put("y", 2).put("a", 1).build();
    assertThat(complete.removeAll(removedWithExtra))
        .isEqualTo(Attributes.builder().put("x", 1).put("z", 3).build());
  }

  @SuppressWarnings("CollectionIncompatibleType")
  @Test
  void asMap() {
    Attributes attributes = Attributes.of(stringKey("key1"), "value1", longKey("key2"), 333L);

    Map<AttributeKey<?>, Object> map = attributes.asMap();
    assertThat(map)
        .containsExactly(entry(stringKey("key1"), "value1"), entry(longKey("key2"), 333L));

    assertThat(map.get(stringKey("key1"))).isEqualTo("value1");
    assertThat(map.get(longKey("key2"))).isEqualTo(333L);
    // Map of AttributeKey, not String
    assertThat(map.get("key1")).isNull();
    assertThat(map.get(null)).isNull();
    assertThat(map.keySet()).containsExactlyInAnyOrder(stringKey("key1"), longKey("key2"));
    assertThat(map.values()).containsExactlyInAnyOrder("value1", 333L);
    assertThat(map.entrySet())
        .containsExactlyInAnyOrder(
            entry(stringKey("key1"), "value1"), entry(longKey("key2"), 333L));
    assertThat(map.entrySet().contains(entry(stringKey("key1"), "value1"))).isTrue();
    assertThat(map.entrySet().contains(entry(stringKey("key1"), "value2"))).isFalse();
    assertThat(map.isEmpty()).isFalse();
    assertThat(map.containsKey(stringKey("key1"))).isTrue();
    assertThat(map.containsKey(longKey("key2"))).isTrue();
    assertThat(map.containsKey(stringKey("key3"))).isFalse();
    assertThat(map.containsKey(null)).isFalse();
    assertThat(map.containsValue("value1")).isTrue();
    assertThat(map.containsValue(333L)).isTrue();
    assertThat(map.containsValue("cat")).isFalse();
    assertThatThrownBy(() -> map.put(stringKey("animal"), "cat"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.remove(stringKey("key1")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.putAll(Collections.emptyMap()))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(map::clear).isInstanceOf(UnsupportedOperationException.class);

    assertThat(map.keySet().contains(stringKey("key1"))).isTrue();
    assertThat(map.keySet().contains(stringKey("key3"))).isFalse();
    assertThat(map.keySet().containsAll(Arrays.asList(stringKey("key1"), longKey("key2"))))
        .isTrue();
    assertThat(map.keySet().containsAll(Arrays.asList(stringKey("key1"), longKey("key3"))))
        .isFalse();
    assertThat(map.keySet().containsAll(null)).isFalse();
    assertThat(map.keySet().containsAll(Collections.emptyList())).isTrue();
    assertThat(map.keySet().size()).isEqualTo(2);
    assertThat(map.keySet().toArray())
        .containsExactlyInAnyOrder(stringKey("key1"), longKey("key2"));
    AttributeKey<?>[] keys = new AttributeKey[2];
    map.keySet().toArray(keys);
    assertThat(keys).containsExactlyInAnyOrder(stringKey("key1"), longKey("key2"));
    keys = new AttributeKey[0];
    assertThat(map.keySet().toArray(keys))
        .containsExactlyInAnyOrder(stringKey("key1"), longKey("key2"));
    assertThat(keys).isEmpty(); // Didn't use input array.
    assertThatThrownBy(() -> map.keySet().iterator().remove())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(map.keySet().containsAll(singletonList(stringKey("key1")))).isTrue();
    assertThat(map.keySet().containsAll(Arrays.asList(stringKey("key1"), stringKey("key3"))))
        .isFalse();
    assertThat(map.keySet().isEmpty()).isFalse();
    assertThatThrownBy(() -> map.keySet().add(stringKey("key3")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().remove(stringKey("key1")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().addAll(Collections.singletonList(stringKey("key3"))))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().retainAll(Collections.singletonList(stringKey("key3"))))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().removeAll(Collections.singletonList(stringKey("key3"))))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().clear())
        .isInstanceOf(UnsupportedOperationException.class);

    assertThat(map.values().contains("value1")).isTrue();
    assertThat(map.values().contains("value3")).isFalse();

    assertThat(map.toString()).isEqualTo("ReadOnlyArrayMap{key1=value1,key2=333}");

    Map<AttributeKey<?>, Object> emptyMap = Attributes.builder().build().asMap();
    assertThat(emptyMap.isEmpty()).isTrue();
    assertThatThrownBy(() -> emptyMap.entrySet().iterator().next())
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void builder_nullKey() {
    Attributes attributes = Attributes.builder().put(stringKey(null), "value").build();
    assertThat(attributes).isEqualTo(Attributes.empty());
  }

  @Test
  void forEach_empty() {
    final AtomicBoolean sawSomething = new AtomicBoolean(false);
    Attributes emptyAttributes = Attributes.empty();
    emptyAttributes.forEach((key, value) -> sawSomething.set(true));
    assertThat(sawSomething.get()).isFalse();
  }

  @Test
  void orderIndependentEquality() {
    Attributes one =
        Attributes.of(
            stringKey("key1"), "value1",
            stringKey("key2"), "value2");
    Attributes two =
        Attributes.of(
            stringKey("key2"), "value2",
            stringKey("key1"), "value1");

    assertThat(one).isEqualTo(two);

    Attributes three =
        Attributes.of(
            stringKey("key1"), "value1",
            stringKey("key2"), "value2",
            stringKey(""), "empty",
            stringKey("key3"), "value3",
            stringKey("key4"), "value4");
    Attributes four =
        Attributes.of(
            null,
            "null",
            stringKey("key2"),
            "value2",
            stringKey("key1"),
            "value1",
            stringKey("key4"),
            "value4",
            stringKey("key3"),
            "value3");

    assertThat(three).isEqualTo(four);
  }

  @Test
  void deduplication() {
    Attributes one =
        Attributes.of(
            stringKey("key1"), "valueX",
            stringKey("key1"), "value1");
    Attributes two = Attributes.of(stringKey("key1"), "value1");

    assertThat(one).isEqualTo(two);
  }

  @Test
  void deduplication_oddNumberElements() {
    Attributes one =
        Attributes.builder()
            .put(stringKey("key2"), "valueX")
            .put(stringKey("key2"), "value2")
            .put(stringKey("key1"), "value1")
            .build();
    Attributes two =
        Attributes.builder()
            .put(stringKey("key2"), "value2")
            .put(stringKey("key1"), "value1")
            .build();

    assertThat(one).isEqualTo(two);
  }

  @Test
  void emptyAndNullKey() {
    Attributes noAttributes = Attributes.of(stringKey(""), "empty", null, "null");

    assertThat(noAttributes.size()).isEqualTo(0);
  }

  @Test
  void builder() {
    Attributes attributes =
        Attributes.builder()
            .put("string", "value1")
            .put("long", 100)
            .put(longKey("long2"), 10)
            .put("double", 33.44)
            .put("boolean", "duplicateShouldBeRemoved")
            .put("boolean", false)
            .build();

    Attributes wantAttributes =
        Attributes.of(
            stringKey("string"),
            "value1",
            longKey("long"),
            100L,
            longKey("long2"),
            10L,
            doubleKey("double"),
            33.44,
            booleanKey("boolean"),
            false);
    assertThat(attributes).isEqualTo(wantAttributes);

    AttributesBuilder newAttributes = attributes.toBuilder();
    newAttributes.put("newKey", "newValue");
    assertThat(newAttributes.build())
        .isEqualTo(
            Attributes.of(
                stringKey("string"),
                "value1",
                longKey("long"),
                100L,
                longKey("long2"),
                10L,
                doubleKey("double"),
                33.44,
                booleanKey("boolean"),
                false,
                stringKey("newKey"),
                "newValue"));
    // Original not mutated.
    assertThat(attributes).isEqualTo(wantAttributes);
  }

  @Test
  void builder_arrayTypes() {
    Attributes attributes =
        Attributes.builder()
            .put("string", "value1", "value2", null)
            .put("long", 100L, 200L)
            .put("double", 33.44, -44.33)
            .put("boolean", "duplicateShouldBeRemoved")
            .put(stringKey("boolean"), "true")
            .put("boolean", false, true)
            .build();

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                stringArrayKey("string"), Arrays.asList("value1", "value2", null),
                longArrayKey("long"), Arrays.asList(100L, 200L),
                doubleArrayKey("double"), Arrays.asList(33.44, -44.33),
                booleanArrayKey("boolean"), Arrays.asList(false, true)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_Null() {
    assertThat(Attributes.empty().get(stringKey("foo"))).isNull();
    assertThat(Attributes.of(stringKey("key"), "value").get(stringKey("foo"))).isNull();
    assertThat(Attributes.of(stringKey("key"), "value").get((AttributeKey) null)).isNull();
  }

  @Test
  void get() {
    assertThat(Attributes.of(stringKey("key"), "value").get(stringKey("key"))).isEqualTo("value");
    assertThat(Attributes.of(stringKey("key"), "value").get(stringKey("value"))).isNull();
    Attributes threeElements =
        Attributes.of(
            stringKey("string"), "value", booleanKey("boolean"), true, longKey("long"), 1L);
    assertThat(threeElements.get(booleanKey("boolean"))).isEqualTo(true);
    assertThat(threeElements.get(stringKey("string"))).isEqualTo("value");
    assertThat(threeElements.get(longKey("long"))).isEqualTo(1L);
    Attributes twoElements =
        Attributes.of(stringKey("string"), "value", booleanKey("boolean"), true);
    assertThat(twoElements.get(booleanKey("boolean"))).isEqualTo(true);
    assertThat(twoElements.get(stringKey("string"))).isEqualTo("value");
    Attributes fourElements =
        Attributes.of(
            stringKey("string"),
            "value",
            booleanKey("boolean"),
            true,
            longKey("long"),
            1L,
            stringArrayKey("array"),
            Arrays.asList("one", "two", "three"));
    assertThat(fourElements.get(stringArrayKey("array")))
        .isEqualTo(Arrays.asList("one", "two", "three"));
    assertThat(threeElements.get(booleanKey("boolean"))).isEqualTo(true);
    assertThat(threeElements.get(stringKey("string"))).isEqualTo("value");
    assertThat(threeElements.get(longKey("long"))).isEqualTo(1L);
  }

  @Test
  void toBuilder() {
    Attributes filled = Attributes.builder().put("cat", "meow").put("dog", "bark").build();

    Attributes fromEmpty =
        Attributes.empty().toBuilder().put("cat", "meow").put("dog", "bark").build();
    assertThat(fromEmpty).isEqualTo(filled);
    // Original not mutated.
    assertThat(Attributes.empty().isEmpty()).isTrue();

    Attributes partial = Attributes.builder().put("cat", "meow").build();
    Attributes fromPartial = partial.toBuilder().put("dog", "bark").build();
    assertThat(fromPartial).isEqualTo(filled);
    // Original not mutated.
    assertThat(partial).isEqualTo(Attributes.builder().put("cat", "meow").build());
  }

  @Test
  void nullsAreNoOps() {
    AttributesBuilder builder = Attributes.builder();
    builder.put(stringKey("attrValue"), "attrValue");
    builder.put("string", "string");
    builder.put("long", 10);
    builder.put("double", 1.0);
    builder.put("bool", true);
    builder.put("arrayString", new String[] {"string"});
    builder.put("arrayLong", new long[] {10L});
    builder.put("arrayDouble", new double[] {1.0});
    builder.put("arrayBool", new boolean[] {true});
    assertThat(builder.build().size()).isEqualTo(9);

    // note: currently these are no-op calls; that behavior is not required, so if it needs to
    // change, that is fine.
    builder.put(stringKey("attrValue"), null);
    builder.put("string", (String) null);
    builder.put("arrayString", (String[]) null);
    builder.put("arrayLong", (long[]) null);
    builder.put("arrayDouble", (double[]) null);
    builder.put("arrayBool", (boolean[]) null);

    Attributes attributes = builder.build();
    assertThat(attributes.size()).isEqualTo(9);
    assertThat(attributes.get(stringKey("string"))).isEqualTo("string");
    assertThat(attributes.get(stringArrayKey("arrayString"))).isEqualTo(singletonList("string"));
    assertThat(attributes.get(longArrayKey("arrayLong"))).isEqualTo(singletonList(10L));
    assertThat(attributes.get(doubleArrayKey("arrayDouble"))).isEqualTo(singletonList(1.0d));
    assertThat(attributes.get(booleanArrayKey("arrayBool"))).isEqualTo(singletonList(true));
  }

  @Test
  void attributesToString() {
    Attributes attributes =
        Attributes.builder()
            .put("otel.status_code", "OK")
            .put("http.response_size", 100)
            .put("process.cpu_consumed", 33.44)
            .put("error", true)
            .put("success", "true")
            .build();

    assertThat(attributes.toString())
        .isEqualTo(
            "{error=true, http.response_size=100, "
                + "otel.status_code=\"OK\", process.cpu_consumed=33.44, success=\"true\"}");
  }

  @Test
  void onlySameTypeCanRetrieveValue() {
    Attributes attributes = Attributes.of(stringKey("animal"), "cat");
    assertThat(attributes.get(stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(longKey("animal"))).isNull();
  }
}
