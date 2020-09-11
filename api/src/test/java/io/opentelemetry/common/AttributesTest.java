/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.common;

import static io.opentelemetry.common.AttributesKeys.booleanArrayKey;
import static io.opentelemetry.common.AttributesKeys.booleanKey;
import static io.opentelemetry.common.AttributesKeys.doubleArrayKey;
import static io.opentelemetry.common.AttributesKeys.doubleKey;
import static io.opentelemetry.common.AttributesKeys.longArrayKey;
import static io.opentelemetry.common.AttributesKeys.longKey;
import static io.opentelemetry.common.AttributesKeys.stringArrayKey;
import static io.opentelemetry.common.AttributesKeys.stringKey;
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
        .containsExactly(entry(stringKey("key1"), "value1"), entry(stringKey("key2"), 333L));
  }

  @Test
  void forEach_singleAttribute() {
    final Map<AttributeKey, Object> entriesSeen = new HashMap<>();

    Attributes attributes = Attributes.of(stringKey("key"), "value");
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(stringKey("key"), "value"));
  }

  @Test
  void forEach_empty() {
    final AtomicBoolean sawSomething = new AtomicBoolean(false);
    Attributes emptyAttributes = Attributes.empty();
    emptyAttributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void consume(AttributeKey<T> key, T value) {
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
            stringKey("key1"), "value1",
            stringKey("key1"), "valueX");
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
        Attributes.newBuilder()
            .setAttribute("string", "value1")
            .setAttribute("long", 100)
            .setAttribute("double", 33.44)
            .setAttribute("boolean", false)
            .setAttribute("boolean", "duplicateShouldBeRemoved")
            .build();

    Attributes wantAttributes =
        Attributes.of(
            stringKey("string"),
            "value1",
            longKey("long"),
            100L,
            doubleKey("double"),
            33.44,
            booleanKey("boolean"),
            false);
    assertThat(attributes).isEqualTo(wantAttributes);

    Attributes.Builder newAttributes = Attributes.newBuilder(attributes);
    newAttributes.setAttribute("newKey", "newValue");
    assertThat(newAttributes.build())
        .isEqualTo(
            Attributes.of(
                stringKey("string"),
                "value1",
                longKey("long"),
                100L,
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
        Attributes.newBuilder()
            .setAttribute("string", "value1", "value2")
            .setAttribute("long", 100L, 200L)
            .setAttribute("double", 33.44, -44.33)
            .setAttribute("boolean", false, true)
            .setAttribute("boolean", "duplicateShouldBeRemoved")
            .setAttribute(stringKey("boolean"), "dropped")
            .build();

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                stringArrayKey("string"), Arrays.asList("value1", "value2"),
                longArrayKey("long"), Arrays.asList(100L, 200L),
                doubleArrayKey("double"), Arrays.asList(33.44, -44.33),
                booleanArrayKey("boolean"), Arrays.asList(false, true)));
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
    Attributes filled =
        Attributes.newBuilder().setAttribute("cat", "meow").setAttribute("dog", "bark").build();

    Attributes fromEmpty =
        Attributes.empty()
            .toBuilder()
            .setAttribute("cat", "meow")
            .setAttribute("dog", "bark")
            .build();
    assertThat(fromEmpty).isEqualTo(filled);
    // Original not mutated.
    assertThat(Attributes.empty().isEmpty()).isTrue();

    Attributes partial = Attributes.newBuilder().setAttribute("cat", "meow").build();
    Attributes fromPartial = partial.toBuilder().setAttribute("dog", "bark").build();
    assertThat(fromPartial).isEqualTo(filled);
    // Original not mutated.
    assertThat(partial).isEqualTo(Attributes.newBuilder().setAttribute("cat", "meow").build());
  }

  @Test
  void deleteByNull() {
    Attributes.Builder attributes = Attributes.newBuilder();
    attributes.setAttribute(stringKey("attrValue"), "attrValue");
    attributes.setAttribute("string", "string");
    attributes.setAttribute("long", 10);
    attributes.setAttribute("double", 1.0);
    attributes.setAttribute("bool", true);
    attributes.setAttribute("arrayString", new String[] {"string"});
    attributes.setAttribute("arrayLong", new Long[] {10L});
    attributes.setAttribute("arrayDouble", new Double[] {1.0});
    attributes.setAttribute("arrayBool", new Boolean[] {true});
    assertThat(attributes.build().size()).isEqualTo(9);
    attributes.setAttribute(stringKey("attrValue"), null);
    attributes.setAttribute("string", (String) null);
    attributes.setAttribute("arrayString", (String[]) null);
    attributes.setAttribute("arrayLong", (Long[]) null);
    attributes.setAttribute("arrayDouble", (Double[]) null);
    attributes.setAttribute("arrayBool", (Boolean[]) null);
    assertThat(attributes.build().size()).isEqualTo(3);
  }
}
