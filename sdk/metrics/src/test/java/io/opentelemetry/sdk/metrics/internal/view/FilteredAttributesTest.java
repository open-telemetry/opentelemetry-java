/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.collect.ImmutableSet;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link FilteredAttributes}s. */
@SuppressWarnings("rawtypes")
class FilteredAttributesTest {

  @Test
  void forEach() {
    Map<AttributeKey, Object> entriesSeen = new LinkedHashMap<>();

    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), longKey("key2")));

    attributes.forEach(entriesSeen::put);

    assertThat(entriesSeen)
        .containsExactly(entry(stringKey("key1"), "value1"), entry(longKey("key2"), 333L));
  }

  @Test
  void forEach_singleAttribute() {
    Map<AttributeKey, Object> entriesSeen = new HashMap<>();

    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(stringKey("key"), "value"), ImmutableSet.of(stringKey("key")));
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(stringKey("key"), "value"));
  }

  @SuppressWarnings("CollectionIncompatibleType")
  @Test
  void asMap() {
    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), longKey("key2")));

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

    assertThat(map.containsKey(stringKey("key1"))).isTrue();
    assertThat(map.containsKey(stringKey("key3"))).isFalse();
    assertThat(map.keySet().containsAll(Arrays.asList(stringKey("key1"), longKey("key2"))))
        .isTrue();
    assertThat(map.keySet().containsAll(Arrays.asList(stringKey("key1"), longKey("key3"))))
        .isFalse();
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
    assertThat(map.containsKey(stringKey("key1"))).isTrue();
    assertThat(map.keySet().containsAll(Arrays.asList(stringKey("key1"), stringKey("key3"))))
        .isFalse();
    assertThat(map.keySet().isEmpty()).isFalse();
    assertThatThrownBy(() -> map.keySet().add(stringKey("key3")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().remove(stringKey("key1")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().add(stringKey("key3")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().retainAll(Collections.singletonList(stringKey("key3"))))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().removeAll(Collections.singletonList(stringKey("key3"))))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> map.keySet().clear())
        .isInstanceOf(UnsupportedOperationException.class);

    assertThat(map.containsValue("value1")).isTrue();
    assertThat(map.containsValue("value3")).isFalse();

    assertThat(map.toString()).isEqualTo("{key1=value1, key2=333}");

    Map<AttributeKey<?>, Object> emptyMap = Attributes.builder().build().asMap();
    assertThat(emptyMap.isEmpty()).isTrue();
    assertThatThrownBy(() -> emptyMap.entrySet().iterator().next())
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void equalsAndHashCode() {
    Attributes one =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1",
                stringKey("key2"), "value2",
                stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), stringKey("key2")));
    Attributes two =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1",
                stringKey("key2"), "value2",
                stringKey("key3"), "other"),
            ImmutableSet.of(stringKey("key1"), stringKey("key2")));
    Attributes three =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1",
                stringKey("key2"), "value2",
                stringKey("key4"), "value4"),
            ImmutableSet.of(stringKey("key1"), stringKey("key2")));

    assertThat(one).isEqualTo(two);
    assertThat(one).isEqualTo(three);
    assertThat(two).isEqualTo(three);
    assertThat(one.hashCode()).isEqualTo(two.hashCode());
    assertThat(two.hashCode()).isEqualTo(three.hashCode());

    Attributes four =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1",
                stringKey("key2"), "other"),
            ImmutableSet.of(stringKey("key1"), stringKey("key2")));
    Attributes five =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1",
                stringKey("key2"), "value2",
                stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), stringKey("key2"), stringKey("key3")));
    assertThat(one).isNotEqualTo(four);
    assertThat(one).isNotEqualTo(five);
    assertThat(one.hashCode()).isNotEqualTo(four.hashCode());
    assertThat(one.hashCode()).isNotEqualTo(five.hashCode());

    assertThat(two).isNotEqualTo(four);
    assertThat(two).isNotEqualTo(five);
    assertThat(two.hashCode()).isNotEqualTo(four.hashCode());
    assertThat(two.hashCode()).isNotEqualTo(five.hashCode());

    assertThat(three).isNotEqualTo(four);
    assertThat(three).isNotEqualTo(five);
    assertThat(three.hashCode()).isNotEqualTo(four.hashCode());
    assertThat(three.hashCode()).isNotEqualTo(five.hashCode());
  }

  @Test
  void get_Null() {
    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), longKey("key2")));
    assertThat(attributes.get(stringKey("foo"))).isNull();
    assertThat(attributes.get(stringKey("key3"))).isNull();
    assertThat(attributes.get(stringKey("key3"))).isNull();
  }

  @Test
  void get() {
    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), longKey("key2")));
    assertThat(attributes.get(stringKey("key1"))).isEqualTo("value1");
    assertThat(attributes.get(longKey("key2"))).isEqualTo(333L);
  }

  @Test
  void toBuilder() {
    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), longKey("key2")));
    assertThat(attributes.toBuilder().build())
        .isEqualTo(Attributes.of(stringKey("key1"), "value1", longKey("key2"), 333L));
  }

  @Test
  void stringRepresentation() {
    Attributes attributes =
        FilteredAttributes.create(
            Attributes.of(
                stringKey("key1"), "value1", longKey("key2"), 333L, stringKey("key3"), "value3"),
            ImmutableSet.of(stringKey("key1"), longKey("key2")));
    assertThat(attributes.toString()).isEqualTo("FilteredAttributes{key1=value1,key2=333}");
  }
}
