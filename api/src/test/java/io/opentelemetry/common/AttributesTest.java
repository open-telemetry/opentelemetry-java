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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for {@link Attributes}. */
public class AttributesTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

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

  @Test
  public void varargsCreation() {
    Attributes<Integer> attributes =
        Attributes.of(
            Integer.class,
            "foo",
            34,
            "bar",
            77,
            "baz",
            33,
            "boom",
            55,
            "silly",
            -99,
            "overtheedge",
            Integer.MAX_VALUE);
    final AtomicInteger valuesSeen = new AtomicInteger(0);
    attributes.forEach(
        new AttributeConsumer<Integer>() {
          @Override
          public void consume(String key, Integer value) {
            valuesSeen.incrementAndGet();
          }
        });
    assertThat(valuesSeen.get()).isEqualTo(6);
  }

  @Test
  public void varargsCreation_superType() {
    Attributes<Number> attributes =
        Attributes.of(
            Number.class,
            "foo",
            34,
            "bar",
            77,
            "baz",
            33,
            "boom",
            55,
            "silly",
            -99,
            "overtheedge",
            Integer.MAX_VALUE);
    final AtomicInteger valuesSeen = new AtomicInteger(0);
    attributes.forEach(
        new AttributeConsumer<Number>() {
          @Override
          public void consume(String key, Number value) {
            valuesSeen.incrementAndGet();
          }
        });
    assertThat(valuesSeen.get()).isEqualTo(6);
  }

  @Test
  public void varargsCreation_badType() {
    thrown.expect(IllegalArgumentException.class);
    Attributes.of(
        Integer.class,
        "foo",
        34,
        "bar",
        "badValue",
        "baz",
        33,
        "boom",
        55,
        "silly",
        -99,
        "overtheedge",
        Integer.MAX_VALUE);
  }

  @Test
  public void varargsCreation_nonEvenArgs() {
    thrown.expect(IllegalArgumentException.class);
    Attributes.of(
        Integer.class,
        "foo",
        34,
        "bar",
        "badValue",
        "baz",
        33,
        "boom",
        55,
        "silly",
        -99,
        "overtheedge");
  }
}
