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

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.arrayAttributeValue;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;
import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import io.opentelemetry.common.ImmutableKeyValuePairs.KeyValueConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for {@link Attributes}s. */
public class AttributesTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void forEach() {
    final Map<String, AttributeValue> entriesSeen = new HashMap<>();

    Attributes attributes =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key2", AttributeValue.longAttributeValue(333));

    attributes.forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            entriesSeen.put(key, value);
          }
        });

    assertThat(entriesSeen)
        .containsExactly("key1", stringAttributeValue("value1"), "key2", longAttributeValue(333));
  }

  @Test
  public void forEach_singleAttribute() {
    final Map<String, AttributeValue> entriesSeen = new HashMap<>();

    Attributes attributes = Attributes.of("key", stringAttributeValue("value"));
    attributes.forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            entriesSeen.put(key, value);
          }
        });
    assertThat(entriesSeen).containsExactly("key", stringAttributeValue("value"));
  }

  @Test
  public void forEach_empty() {
    final AtomicBoolean sawSomething = new AtomicBoolean(false);
    Attributes emptyAttributes = Attributes.empty();
    emptyAttributes.forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            sawSomething.set(true);
          }
        });
    assertThat(sawSomething.get()).isFalse();
  }

  @Test
  public void orderIndependentEquality() {
    Attributes one =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key2", stringAttributeValue("value2"));
    Attributes two =
        Attributes.of(
            "key2", stringAttributeValue("value2"),
            "key1", stringAttributeValue("value1"));

    assertThat(one).isEqualTo(two);
  }

  @Test
  public void deduplication() {
    Attributes one =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key1", stringAttributeValue("valueX"));
    Attributes two = Attributes.of("key1", stringAttributeValue("value1"));

    assertThat(one).isEqualTo(two);
  }

  @Test
  public void builder() {
    Attributes attributes =
        Attributes.newBuilder()
            .addAttribute("string", "value1")
            .addAttribute("long", 100)
            .addAttribute("double", 33.44)
            .addAttribute("boolean", false)
            .addAttribute("boolean", "duplicateShouldBeRemoved")
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
  public void builder_arrayTypes() {
    Attributes attributes =
        Attributes.newBuilder()
            .addAttribute("string", "value1", "value2")
            .addAttribute("long", 100L, 200L)
            .addAttribute("double", 33.44, -44.33)
            .addAttribute("boolean", false, true)
            .addAttribute("boolean", "duplicateShouldBeRemoved")
            .build();

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                "string", arrayAttributeValue("value1", "value2"),
                "long", arrayAttributeValue(100L, 200L),
                "double", arrayAttributeValue(33.44, -44.33),
                "boolean", arrayAttributeValue(false, true)));
  }
}
