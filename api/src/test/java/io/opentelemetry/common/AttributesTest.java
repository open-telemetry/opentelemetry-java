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

import static io.opentelemetry.common.AttributeValue.arrayAttributeValue;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;
import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Attributes}s. */
class AttributesTest {

  @Test
  void forEach() {
    final Map<String, AttributeValue> entriesSeen = new HashMap<>();

    Attributes attributes =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key2", AttributeValue.longAttributeValue(333));

    attributes.forEach(entriesSeen::put);

    assertThat(entriesSeen)
        .containsExactly(
            entry("key1", stringAttributeValue("value1")), entry("key2", longAttributeValue(333)));
  }

  @Test
  void forEach_singleAttribute() {
    final Map<String, AttributeValue> entriesSeen = new HashMap<>();

    Attributes attributes = Attributes.of("key", stringAttributeValue("value"));
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry("key", stringAttributeValue("value")));
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
            "key1", stringAttributeValue("value1"),
            "key2", stringAttributeValue("value2"));
    Attributes two =
        Attributes.of(
            "key2", stringAttributeValue("value2"),
            "key1", stringAttributeValue("value1"));

    assertThat(one).isEqualTo(two);

    Attributes three =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key2", stringAttributeValue("value2"),
            "", stringAttributeValue("empty"),
            "key3", stringAttributeValue("value3"),
            "key4", stringAttributeValue("value4"));
    Attributes four =
        Attributes.of(
            null,
            stringAttributeValue("null"),
            "key2",
            stringAttributeValue("value2"),
            "key1",
            stringAttributeValue("value1"),
            "key4",
            stringAttributeValue("value4"),
            "key3",
            stringAttributeValue("value3"));

    assertThat(three).isEqualTo(four);
  }

  @Test
  void deduplication() {
    Attributes one =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key1", stringAttributeValue("valueX"));
    Attributes two = Attributes.of("key1", stringAttributeValue("value1"));

    assertThat(one).isEqualTo(two);
  }

  @Test
  void emptyAndNullKey() {
    Attributes noAttributes =
        Attributes.of("", stringAttributeValue("empty"), null, stringAttributeValue("null"));

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

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                "string", stringAttributeValue("value1"),
                "long", longAttributeValue(100),
                "double", doubleAttributeValue(33.44),
                "boolean", booleanAttributeValue(false)));
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
            .setAttribute("boolean", stringAttributeValue("dropped"))
            .build();

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                "string", arrayAttributeValue("value1", "value2"),
                "long", arrayAttributeValue(100L, 200L),
                "double", arrayAttributeValue(33.44, -44.33),
                "boolean", arrayAttributeValue(false, true)));
  }

  @Test
  void get_Null() {
    assertThat(Attributes.empty().get("foo")).isNull();
    assertThat(Attributes.of("key", stringAttributeValue("value")).get("foo")).isNull();
  }

  @Test
  void get() {
    assertThat(Attributes.of("key", stringAttributeValue("value")).get("key"))
        .isEqualTo(stringAttributeValue("value"));
    assertThat(Attributes.of("key", stringAttributeValue("value")).get("value")).isNull();
    Attributes threeElements =
        Attributes.of(
            "string", stringAttributeValue("value"),
            "boolean", booleanAttributeValue(true),
            "long", longAttributeValue(1L));
    assertThat(threeElements.get("boolean")).isEqualTo(booleanAttributeValue(true));
    assertThat(threeElements.get("string")).isEqualTo(stringAttributeValue("value"));
    assertThat(threeElements.get("long")).isEqualTo(longAttributeValue(1L));
    Attributes twoElements =
        Attributes.of(
            "string", stringAttributeValue("value"),
            "boolean", booleanAttributeValue(true));
    assertThat(twoElements.get("boolean")).isEqualTo(booleanAttributeValue(true));
    assertThat(twoElements.get("string")).isEqualTo(stringAttributeValue("value"));
    Attributes fourElements =
        Attributes.of(
            "string", stringAttributeValue("value"),
            "boolean", booleanAttributeValue(true),
            "long", longAttributeValue(1L),
            "array", arrayAttributeValue("one", "two", "three"));
    assertThat(fourElements.get("array")).isEqualTo(arrayAttributeValue("one", "two", "three"));
    assertThat(threeElements.get("boolean")).isEqualTo(booleanAttributeValue(true));
    assertThat(threeElements.get("string")).isEqualTo(stringAttributeValue("value"));
    assertThat(threeElements.get("long")).isEqualTo(longAttributeValue(1L));
  }
}
