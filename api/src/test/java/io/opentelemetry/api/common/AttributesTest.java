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
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Attributes}s. */
@SuppressWarnings("rawtypes")
class AttributesTest {

  @Test
  void forEach() {
    final Map<AttributeKey, Object> entriesSeen = new HashMap<>();

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
  void builder_nullKey() {
    Attributes attributes = Attributes.builder().put(stringKey(null), "value").build();
    assertThat(attributes).isEqualTo(Attributes.empty());
  }

  @Test
  void forEach_empty() {
    final AtomicBoolean sawSomething = new AtomicBoolean(false);
    Attributes emptyAttributes = Attributes.empty();
    emptyAttributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void accept(AttributeKey<T> key, T value) {
            sawSomething.set(true);
          }
        });
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

    AttributesBuilder newAttributes = Attributes.builder(attributes);
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
            .put("long", null, 100L, 200L)
            .put("double", 33.44, null, -44.33)
            .put("boolean", "duplicateShouldBeRemoved")
            .put(stringKey("boolean"), "true")
            .put("boolean", false, null, true)
            .build();

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                stringArrayKey("string"), Arrays.asList("value1", "value2", null),
                longArrayKey("long"), Arrays.asList(null, 100L, 200L),
                doubleArrayKey("double"), Arrays.asList(33.44, null, -44.33),
                booleanArrayKey("boolean"), Arrays.asList(false, null, true)));
  }

  @Test
  void get_Null() {
    assertThat(Attributes.empty().get(stringKey("foo"))).isNull();
    assertThat(Attributes.of(stringKey("key"), "value").get(stringKey("foo"))).isNull();
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
    builder.put("arrayLong", new Long[] {10L});
    builder.put("arrayDouble", new Double[] {1.0});
    builder.put("arrayBool", new Boolean[] {true});
    assertThat(builder.build().size()).isEqualTo(9);

    // note: currently these are no-op calls; that behavior is not required, so if it needs to
    // change, that is fine.
    builder.put(stringKey("attrValue"), null);
    builder.put("string", (String) null);
    builder.put("arrayString", (String[]) null);
    builder.put("arrayLong", (Long[]) null);
    builder.put("arrayDouble", (Double[]) null);
    builder.put("arrayBool", (Boolean[]) null);

    Attributes attributes = builder.build();
    assertThat(attributes.size()).isEqualTo(9);
    assertThat(attributes.get(stringKey("string"))).isEqualTo("string");
    assertThat(attributes.get(stringArrayKey("arrayString"))).isEqualTo(singletonList("string"));
    assertThat(attributes.get(longArrayKey("arrayLong"))).isEqualTo(singletonList(10L));
    assertThat(attributes.get(doubleArrayKey("arrayDouble"))).isEqualTo(singletonList(1.0d));
    assertThat(attributes.get(booleanArrayKey("arrayBool"))).isEqualTo(singletonList(true));
  }
}
