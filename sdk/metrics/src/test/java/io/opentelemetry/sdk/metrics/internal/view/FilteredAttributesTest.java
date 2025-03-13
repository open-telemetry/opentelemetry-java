/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link FilteredAttributes}s. */
@SuppressWarnings("rawtypes")
class FilteredAttributesTest {

  private static final AttributeKey<String> KEY1 = stringKey("key1");
  private static final AttributeKey<String> KEY2 = stringKey("key2");
  private static final AttributeKey<String> KEY3 = stringKey("key3");
  private static final AttributeKey<String> KEY4 = stringKey("key4");
  private static final AttributeKey<Long> KEY2_LONG = longKey("key2");
  private static final Set<AttributeKey<?>> ALL_KEYS =
      ImmutableSet.of(KEY1, KEY2, KEY3, KEY4, KEY2_LONG);
  private static final Attributes ALL_ATTRIBUTES =
      Attributes.of(KEY1, "value1", KEY2, "value2", KEY2_LONG, 222L, KEY3, "value3");
  private static final Attributes FILTERED_ATTRIBUTES_ONE =
      FilteredAttributes.create(ALL_ATTRIBUTES, ImmutableSet.of(KEY1));
  private static final Attributes FILTERED_ATTRIBUTES_TWO =
      FilteredAttributes.create(ALL_ATTRIBUTES, ImmutableSet.of(KEY1, KEY2_LONG));
  private static final Attributes FILTERED_ATTRIBUTES_THREE =
      FilteredAttributes.create(ALL_ATTRIBUTES, ImmutableSet.of(KEY1, KEY2_LONG, KEY3));
  private static final Attributes FILTERED_ATTRIBUTES_FOUR =
      FilteredAttributes.create(ALL_ATTRIBUTES, ImmutableSet.of(KEY1, KEY2_LONG, KEY3, KEY4));
  private static final Attributes FILTERED_ATTRIBUTES_EMPTY_SOURCE =
      FilteredAttributes.create(Attributes.empty(), ImmutableSet.of(KEY1));
  private static final Attributes FILTERED_ATTRIBUTES_EMPTY =
      FilteredAttributes.create(ALL_ATTRIBUTES, Collections.emptySet());

  @ParameterizedTest
  @MethodSource("mapArgs")
  void forEach(Attributes filteredAttributes, Map<AttributeKey<?>, Object> expectedMapEntries) {
    Map<AttributeKey, Object> entriesSeen = new HashMap<>();
    filteredAttributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).isEqualTo(expectedMapEntries);
  }

  @ParameterizedTest
  @MethodSource("mapArgs")
  void asMap(Attributes filteredAttributes, Map<AttributeKey<?>, Object> expectedMapEntries) {
    assertThat(filteredAttributes.asMap()).isEqualTo(expectedMapEntries);
  }

  @ParameterizedTest
  @MethodSource("mapArgs")
  void size(Attributes filteredAttributes, Map<AttributeKey<?>, Object> expectedMapEntries) {
    assertThat(filteredAttributes.size()).isEqualTo(expectedMapEntries.size());
  }

  @ParameterizedTest
  @MethodSource("mapArgs")
  void isEmpty(Attributes filteredAttributes, Map<AttributeKey<?>, Object> expectedMapEntries) {
    assertThat(filteredAttributes.isEmpty).isEqualTo(expectedMapEntries.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("mapArgs")
  void get(Attributes filteredAttributes, Map<AttributeKey<?>, Object> expectedMapEntries) {
    for (AttributeKey<?> key : ALL_KEYS) {
      Object expectedValue = expectedMapEntries.get(key);
      assertThat(filteredAttributes.get(key)).isEqualTo(expectedValue);
    }
  }

  @ParameterizedTest
  @MethodSource("mapArgs")
  void toBuilder(Attributes filteredAttributes, Map<AttributeKey<?>, Object> expectedMapEntries) {
    Attributes attributes = filteredAttributes.toBuilder().build();
    assertThat(attributes.asMap()).isEqualTo(expectedMapEntries);
  }

  private static Stream<Arguments> mapArgs() {
    return Stream.of(
        Arguments.of(FILTERED_ATTRIBUTES_ONE, ImmutableMap.of(KEY1, "value1")),
        Arguments.of(FILTERED_ATTRIBUTES_TWO, ImmutableMap.of(KEY1, "value1", KEY2_LONG, 222L)),
        Arguments.of(
            FILTERED_ATTRIBUTES_THREE,
            ImmutableMap.of(KEY1, "value1", KEY2_LONG, 222L, KEY3, "value3")),
        Arguments.of(
            FILTERED_ATTRIBUTES_FOUR,
            ImmutableMap.of(KEY1, "value1", KEY2_LONG, 222L, KEY3, "value3")),
        Arguments.of(FILTERED_ATTRIBUTES_EMPTY_SOURCE, Collections.emptyMap()),
        Arguments.of(FILTERED_ATTRIBUTES_EMPTY, Collections.emptyMap()));
  }

  @Test
  void stringRepresentation() {
    assertThat(FILTERED_ATTRIBUTES_ONE.toString()).isEqualTo("FilteredAttributes{key1=value1}");
    assertThat(FILTERED_ATTRIBUTES_TWO.toString())
        .isEqualTo("FilteredAttributes{key1=value1,key2=222}");
    assertThat(FILTERED_ATTRIBUTES_THREE.toString())
        .isEqualTo("FilteredAttributes{key1=value1,key2=222,key3=value3}");
    assertThat(FILTERED_ATTRIBUTES_FOUR.toString())
        .isEqualTo("FilteredAttributes{key1=value1,key2=222,key3=value3}");
    assertThat(FILTERED_ATTRIBUTES_EMPTY_SOURCE.toString()).isEqualTo("{}");
    assertThat(FILTERED_ATTRIBUTES_EMPTY.toString()).isEqualTo("{}");
  }

  /**
   * Test behavior of attributes with more than the 32 limit of FilteredAttributes.filteredIndices.
   */
  @RepeatedTest(10)
  void largeAttributes() {
    Set<AttributeKey<?>> allKeys = new HashSet<>();
    AttributesBuilder allAttributesBuilder = Attributes.builder();
    IntStream.range(0, 100)
        .forEach(
            i -> {
              AttributeKey<String> key = stringKey("key" + i);
              allKeys.add(key);
              allAttributesBuilder.put(key, "value" + i);
            });
    Attributes allAttributes = allAttributesBuilder.build();

    Attributes empty = FilteredAttributes.create(allAttributes, Collections.emptySet());
    assertThat(empty.size()).isEqualTo(0);
    assertThat(empty.isEmpty).isTrue();

    Set<AttributeKey<?>> oneKey = allKeys.stream().limit(1).collect(Collectors.toSet());
    Attributes one = FilteredAttributes.create(allAttributes, oneKey);
    assertThat(one.size()).isEqualTo(1);
    assertThat(one.isEmpty).isFalse();
    allKeys.stream()
        .forEach(
            key -> {
              if (oneKey.contains(key)) {
                assertThat(one.get(key)).isNotNull();
              } else {
                assertThat(one.get(key)).isNull();
              }
            });

    Set<AttributeKey<?>> tenKeys = allKeys.stream().limit(10).collect(Collectors.toSet());
    Attributes ten = FilteredAttributes.create(allAttributes, tenKeys);
    assertThat(ten.size()).isEqualTo(10);
    assertThat(ten.isEmpty).isFalse();
    allKeys.stream()
        .forEach(
            key -> {
              if (tenKeys.contains(key)) {
                assertThat(ten.get(key)).isNotNull();
              } else {
                assertThat(ten.get(key)).isNull();
              }
            });
  }

  @Test
  void equalsAndHashCode() {
    new EqualsTester()
        .addEqualityGroup(
            FILTERED_ATTRIBUTES_ONE,
            FilteredAttributes.create(Attributes.of(KEY1, "value1"), Collections.singleton(KEY1)),
            FilteredAttributes.create(Attributes.of(KEY1, "value1"), ImmutableSet.of(KEY1, KEY2)),
            FilteredAttributes.create(
                Attributes.of(KEY1, "value1", KEY2, "value2"), Collections.singleton(KEY1)),
            FilteredAttributes.create(
                Attributes.of(KEY1, "value1", KEY2_LONG, 222L), Collections.singleton(KEY1)))
        .addEqualityGroup(FILTERED_ATTRIBUTES_TWO)
        .addEqualityGroup(FILTERED_ATTRIBUTES_THREE, FILTERED_ATTRIBUTES_FOUR)
        .addEqualityGroup(FILTERED_ATTRIBUTES_EMPTY, FILTERED_ATTRIBUTES_EMPTY_SOURCE)
        .testEquals();
  }
}
