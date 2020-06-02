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
import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import io.opentelemetry.common.Attributes.AttributeConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

/** Unit tests for {@link Attributes}. */
public class AttributesTest {

  @Test
  public void forEach() {
    final Map<String, AttributeValue> entriesSeen = new HashMap<>();

    Attributes<AttributeValue> attributes =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key2", AttributeValue.longAttributeValue(333));

    attributes.forEach(
        new AttributeConsumer<AttributeValue>() {
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

    Attributes<AttributeValue> attributes = Attributes.of("key", stringAttributeValue("value"));
    attributes.forEach(
        new AttributeConsumer<AttributeValue>() {
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
    Attributes<AttributeValue> emptyAttributes = Attributes.empty();
    emptyAttributes.forEach(
        new AttributeConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            sawSomething.set(true);
          }
        });
    assertThat(sawSomething.get()).isFalse();
  }

  @Test
  public void orderIndependentEquality() {
    Attributes<?> one =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key2", stringAttributeValue("value2"));
    Attributes<?> two =
        Attributes.of(
            "key2", stringAttributeValue("value2"),
            "key1", stringAttributeValue("value1"));

    assertThat(one).isEqualTo(two);
  }

  @Test
  public void deduplication() {
    Attributes<?> one =
        Attributes.of(
            "key1", stringAttributeValue("value1"),
            "key1", stringAttributeValue("valueX"));
    Attributes<?> two = Attributes.of("key1", stringAttributeValue("value1"));

    assertThat(one).isEqualTo(two);
  }

  @Test
  public void builder() {
    Attributes<String> attributes =
        Attributes.<String>newBuilder()
            .addAttribute("key1", "value1")
            .addAttribute("key2", "value2")
            .addAttribute("key1", "value3")
            .build();

    assertThat(attributes).isEqualTo(Attributes.of("key1", "value1", "key2", "value2"));
  }
}
