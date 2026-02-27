/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("deprecation") // Testing deprecated EXTENDED_ATTRIBUTES until removed
class ExtendedAttributesTest {

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void get_ExtendedAttributeKey(
      ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    expectedMap.forEach(
        (key, value) -> {
          ExtendedAttributeKey<?> extendedAttributeKey = getKey(key, value);
          Object actualValue = extendedAttributes.get(extendedAttributeKey);
          if (actualValue instanceof ExtendedAttributes) {
            Map<String, Object> mapValue = toMap((ExtendedAttributes) actualValue);
            actualValue = mapValue;
          }

          assertThat(actualValue)
              .describedAs(key + "(" + extendedAttributeKey.getType() + ")")
              .isEqualTo(value);
        });
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void get_AttributeKey(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    expectedMap.forEach(
        (key, value) -> {
          ExtendedAttributeKey<?> extendedAttributeKey = getKey(key, value);
          AttributeKey<?> attributeKey = extendedAttributeKey.asAttributeKey();

          // Skip attribute keys which cannot be represented as AttributeKey
          if (attributeKey == null) {
            return;
          }

          Object actualValue = extendedAttributes.get(attributeKey);

          assertThat(actualValue)
              .describedAs(key + "(" + attributeKey.getType() + ")")
              .isEqualTo(value);
        });
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void forEach(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    // toMap uses .forEach to convert
    Map<String, Object> seenEntries = toMap(extendedAttributes);

    assertThat(seenEntries).isEqualTo(expectedMap);
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void size(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    assertThat(extendedAttributes.size()).isEqualTo(expectedMap.size());
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void isEmpty(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    assertThat(extendedAttributes.isEmpty()).isEqualTo(expectedMap.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void asMap(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    assertEquals(extendedAttributes.asMap(), expectedMap);
  }

  @SuppressWarnings("unchecked")
  private static void assertEquals(
      Map<ExtendedAttributeKey<?>, Object> actual, Map<String, Object> expected) {
    assertThat(actual.size()).isEqualTo(expected.size());
    actual.forEach(
        (key, value) -> {
          if (key.getType() == ExtendedAttributeType.EXTENDED_ATTRIBUTES) {
            assertEquals(
                ((ExtendedAttributes) value).asMap(),
                (Map<String, Object>) expected.get(key.getKey()));
            return;
          }
          assertThat(expected.get(key.getKey())).isEqualTo(value);
        });
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void asAttributes(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    Attributes attributes = extendedAttributes.asAttributes();

    attributes.forEach(
        (key, value) -> {
          assertThat(value).isEqualTo(expectedMap.get(key.getKey()));
        });

    long expectedSize =
        expectedMap.values().stream()
            .filter(value -> !(value instanceof Map))
            .filter(value -> !(value instanceof Value))
            .count();
    assertThat(attributes.size()).isEqualTo(expectedSize);
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void toBuilder(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    ExtendedAttributesBuilder builder = extendedAttributes.toBuilder();

    builder.put("extraKey", "value");

    ExtendedAttributes extendedAttributes1 = builder.build();
    assertThat(extendedAttributes1.size()).isEqualTo(expectedMap.size() + 1);

    ExtendedAttributes extendedAttributes2 =
        extendedAttributes1.toBuilder().remove(ExtendedAttributeKey.stringKey("extraKey")).build();

    assertThat(extendedAttributes2).isEqualTo(extendedAttributes);
    assertThat(extendedAttributes2.size()).isEqualTo(expectedMap.size());
  }

  @ParameterizedTest
  @MethodSource("attributesArgs")
  void equalsAndHashcode(ExtendedAttributes extendedAttributes, Map<String, Object> expectedMap) {
    ExtendedAttributes withExtraEntry =
        extendedAttributes.toBuilder().put("extraKey", "value").build();
    assertThat(extendedAttributes).isNotEqualTo(withExtraEntry);
    assertThat(extendedAttributes.hashCode()).isNotEqualTo(withExtraEntry.hashCode());

    ExtendedAttributes copy1 =
        extendedAttributes.toBuilder().remove(ExtendedAttributeKey.stringKey("extraKey")).build();
    assertThat(extendedAttributes).isEqualTo(copy1);
    assertThat(extendedAttributes.hashCode()).isEqualTo(copy1.hashCode());

    ExtendedAttributes copy2 = fromMap(expectedMap);
    assertThat(extendedAttributes).isEqualTo(copy2);
    assertThat(extendedAttributes.hashCode()).isEqualTo(copy2.hashCode());
  }

  @SuppressWarnings("unchecked")
  private static ExtendedAttributes fromMap(Map<String, Object> map) {
    ExtendedAttributesBuilder builder = ExtendedAttributes.builder();
    map.forEach(
        (key, value) -> {
          ExtendedAttributeKey<?> extendedAttributeKey = getKey(key, value);
          if (extendedAttributeKey.getType() == ExtendedAttributeType.EXTENDED_ATTRIBUTES) {
            builder.put(
                (ExtendedAttributeKey<ExtendedAttributes>) extendedAttributeKey,
                fromMap((Map<String, Object>) value));
            return;
          }
          putInBuilder((ExtendedAttributeKey<Object>) extendedAttributeKey, value, builder);
        });
    return builder.build();
  }

  private static void putInBuilder(
      ExtendedAttributeKey<Object> key, Object value, ExtendedAttributesBuilder builder) {
    builder.put(key, value);
  }

  private static Stream<Arguments> attributesArgs() {
    return Stream.of(
        // Single entry attributes
        Arguments.of(ExtendedAttributes.builder().build(), Collections.emptyMap()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", "value").build(),
            ImmutableMap.builder().put("key", "value").build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", true).build(),
            ImmutableMap.builder().put("key", true).build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", 1L).build(),
            ImmutableMap.builder().put("key", 1L).build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", 1.1).build(),
            ImmutableMap.builder().put("key", 1.1).build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", "value1", "value2").build(),
            ImmutableMap.builder().put("key", Arrays.asList("value1", "value2")).build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", true, false).build(),
            ImmutableMap.builder().put("key", Arrays.asList(true, false)).build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", 1L, 2L).build(),
            ImmutableMap.builder().put("key", Arrays.asList(1L, 2L)).build()),
        Arguments.of(
            ExtendedAttributes.builder().put("key", 1.1, 2.2).build(),
            ImmutableMap.builder().put("key", Arrays.asList(1.1, 2.2)).build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put("key", ExtendedAttributes.builder().put("child", "value").build())
                .build(),
            ImmutableMap.builder()
                .put("key", ImmutableMap.builder().put("child", "value").build())
                .build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.valueKey("key"), Value.of("value"))
                .build(),
            ImmutableMap.builder().put("key", "value").build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.stringKey("key"), "value")
                .build(),
            ImmutableMap.builder().put("key", "value").build()),
        Arguments.of(
            ExtendedAttributes.builder().put(ExtendedAttributeKey.booleanKey("key"), true).build(),
            ImmutableMap.builder().put("key", true).build()),
        Arguments.of(
            ExtendedAttributes.builder().put(ExtendedAttributeKey.longKey("key"), 1L).build(),
            ImmutableMap.builder().put("key", 1L).build()),
        Arguments.of(
            ExtendedAttributes.builder().put(ExtendedAttributeKey.doubleKey("key"), 1.1).build(),
            ImmutableMap.builder().put("key", 1.1).build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.stringArrayKey("key"), Arrays.asList("value1", "value2"))
                .build(),
            ImmutableMap.builder().put("key", Arrays.asList("value1", "value2")).build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.booleanArrayKey("key"), Arrays.asList(true, false))
                .build(),
            ImmutableMap.builder().put("key", Arrays.asList(true, false)).build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.longArrayKey("key"), Arrays.asList(1L, 2L))
                .build(),
            ImmutableMap.builder().put("key", Arrays.asList(1L, 2L)).build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.doubleArrayKey("key"), Arrays.asList(1.1, 2.2))
                .build(),
            ImmutableMap.builder().put("key", Arrays.asList(1.1, 2.2)).build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(
                    ExtendedAttributeKey.extendedAttributesKey("key"),
                    ExtendedAttributes.builder().put("child", "value").build())
                .build(),
            ImmutableMap.builder()
                .put("key", ImmutableMap.builder().put("child", "value").build())
                .build()),
        Arguments.of(
            ExtendedAttributes.builder()
                .put(ExtendedAttributeKey.valueKey("key"), Value.of("value"))
                .build(),
            ImmutableMap.builder().put("key", "value").build()),
        // Multiple entries
        Arguments.of(
            ExtendedAttributes.builder()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", true)
                .put("key4", 1L)
                .put("key5", 1.1)
                .put("key6", "value1", "value2")
                .put("key7", true, false)
                .put("key8", 1L, 2L)
                .put("key9", 1.1, 2.2)
                .put("key10", ExtendedAttributes.builder().put("child", "value").build())
                .put(ExtendedAttributeKey.valueKey("key11"), Value.of("value"))
                .build(),
            ImmutableMap.builder()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", true)
                .put("key4", 1L)
                .put("key5", 1.1)
                .put("key6", Arrays.asList("value1", "value2"))
                .put("key7", Arrays.asList(true, false))
                .put("key8", Arrays.asList(1L, 2L))
                .put("key9", Arrays.asList(1.1, 2.2))
                .put("key10", ImmutableMap.builder().put("child", "value").build())
                .put("key11", "value")
                .build()));
  }

  private static Map<String, Object> toMap(ExtendedAttributes extendedAttributes) {
    Map<String, Object> map = new HashMap<>();
    extendedAttributes.forEach(
        (key, value) -> {
          if (key.getType() == ExtendedAttributeType.EXTENDED_ATTRIBUTES) {
            map.put(key.getKey(), toMap((ExtendedAttributes) value));
            return;
          }
          map.put(key.getKey(), value);
        });
    return map;
  }

  private static ExtendedAttributeKey<?> getKey(String key, Object value) {
    switch (getType(value)) {
      case STRING:
        return ExtendedAttributeKey.stringKey(key);
      case BOOLEAN:
        return ExtendedAttributeKey.booleanKey(key);
      case LONG:
        return ExtendedAttributeKey.longKey(key);
      case DOUBLE:
        return ExtendedAttributeKey.doubleKey(key);
      case STRING_ARRAY:
        return ExtendedAttributeKey.stringArrayKey(key);
      case BOOLEAN_ARRAY:
        return ExtendedAttributeKey.booleanArrayKey(key);
      case LONG_ARRAY:
        return ExtendedAttributeKey.longArrayKey(key);
      case DOUBLE_ARRAY:
        return ExtendedAttributeKey.doubleArrayKey(key);
      case EXTENDED_ATTRIBUTES:
        return ExtendedAttributeKey.extendedAttributesKey(key);
      case VALUE:
        return ExtendedAttributeKey.valueKey(key);
    }
    throw new IllegalArgumentException();
  }

  @SuppressWarnings("unchecked")
  private static ExtendedAttributeType getType(Object value) {
    if (value instanceof String) {
      return ExtendedAttributeType.STRING;
    }
    if (value instanceof Boolean) {
      return ExtendedAttributeType.BOOLEAN;
    }
    if ((value instanceof Long) || (value instanceof Integer)) {
      return ExtendedAttributeType.LONG;
    }
    if ((value instanceof Double) || (value instanceof Float)) {
      return ExtendedAttributeType.DOUBLE;
    }
    if (value instanceof List) {
      List<Object> list = (List<Object>) value;
      if (list.isEmpty()) {
        throw new IllegalArgumentException("Empty list");
      }
      if (list.get(0) instanceof String) {
        return ExtendedAttributeType.STRING_ARRAY;
      }
      if (list.get(0) instanceof Boolean) {
        return ExtendedAttributeType.BOOLEAN_ARRAY;
      }
      if ((list.get(0) instanceof Long) || (list.get(0) instanceof Integer)) {
        return ExtendedAttributeType.LONG_ARRAY;
      }
      if ((list.get(0) instanceof Double) || (list.get(0) instanceof Float)) {
        return ExtendedAttributeType.DOUBLE_ARRAY;
      }
    }
    if ((value instanceof Map)) {
      return ExtendedAttributeType.EXTENDED_ATTRIBUTES;
    }
    if (value instanceof Value<?>) {
      return ExtendedAttributeType.VALUE;
    }
    throw new IllegalArgumentException("Unrecognized value type: " + value);
  }

  @Test
  void complexValueStoredAsString() {
    // When putting a VALUE attribute with a string Value, it should be stored as STRING type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(ExtendedAttributeKey.valueKey("key"), Value.of("test"))
            .build();

    // Should be stored as STRING type internally
    assertThat(attributes.get(ExtendedAttributeKey.stringKey("key"))).isEqualTo("test");
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(Value.of("test"));

    // forEach should show STRING type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(ExtendedAttributeKey.stringKey("key"), "test"));

    // asMap should show STRING type
    assertThat(attributes.asMap())
        .containsExactly(entry(ExtendedAttributeKey.stringKey("key"), "test"));
  }

  @Test
  void complexValueStoredAsLong() {
    // When putting a VALUE attribute with a long Value, it should be stored as LONG type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(ExtendedAttributeKey.valueKey("key"), Value.of(123L))
            .build();

    // Should be stored as LONG type internally
    assertThat(attributes.get(ExtendedAttributeKey.longKey("key"))).isEqualTo(123L);
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(Value.of(123L));

    // forEach should show LONG type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(ExtendedAttributeKey.longKey("key"), 123L));

    // asMap should show LONG type
    assertThat(attributes.asMap())
        .containsExactly(entry(ExtendedAttributeKey.longKey("key"), 123L));
  }

  @Test
  void complexValueStoredAsDouble() {
    // When putting a VALUE attribute with a double Value, it should be stored as DOUBLE type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(ExtendedAttributeKey.valueKey("key"), Value.of(1.23))
            .build();

    // Should be stored as DOUBLE type internally
    assertThat(attributes.get(ExtendedAttributeKey.doubleKey("key"))).isEqualTo(1.23);
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(Value.of(1.23));

    // forEach should show DOUBLE type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(ExtendedAttributeKey.doubleKey("key"), 1.23));

    // asMap should show DOUBLE type
    assertThat(attributes.asMap())
        .containsExactly(entry(ExtendedAttributeKey.doubleKey("key"), 1.23));
  }

  @Test
  void complexValueStoredAsBoolean() {
    // When putting a VALUE attribute with a boolean Value, it should be stored as BOOLEAN type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(ExtendedAttributeKey.valueKey("key"), Value.of(true))
            .build();

    // Should be stored as BOOLEAN type internally
    assertThat(attributes.get(ExtendedAttributeKey.booleanKey("key"))).isEqualTo(true);
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(Value.of(true));

    // forEach should show BOOLEAN type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen).containsExactly(entry(ExtendedAttributeKey.booleanKey("key"), true));

    // asMap should show BOOLEAN type
    assertThat(attributes.asMap())
        .containsExactly(entry(ExtendedAttributeKey.booleanKey("key"), true));
  }

  @Test
  void complexValueStoredAsStringArray() {
    // When putting a VALUE attribute with a homogeneous string array, it should be stored as
    // STRING_ARRAY type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(
                ExtendedAttributeKey.valueKey("key"),
                Value.of(Arrays.asList(Value.of("a"), Value.of("b"))))
            .build();

    // Should be stored as STRING_ARRAY type internally
    assertThat(attributes.get(ExtendedAttributeKey.stringArrayKey("key")))
        .containsExactly("a", "b");
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key")))
        .isEqualTo(Value.of(Arrays.asList(Value.of("a"), Value.of("b"))));

    // forEach should show STRING_ARRAY type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(
            entry(ExtendedAttributeKey.stringArrayKey("key"), Arrays.asList("a", "b")));

    // asMap should show STRING_ARRAY type
    assertThat(attributes.asMap())
        .containsExactly(
            entry(ExtendedAttributeKey.stringArrayKey("key"), Arrays.asList("a", "b")));
  }

  @Test
  void complexValueStoredAsLongArray() {
    // When putting a VALUE attribute with a homogeneous long array, it should be stored as
    // LONG_ARRAY type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(
                ExtendedAttributeKey.valueKey("key"),
                Value.of(Arrays.asList(Value.of(1L), Value.of(2L))))
            .build();

    // Should be stored as LONG_ARRAY type internally
    assertThat(attributes.get(ExtendedAttributeKey.longArrayKey("key"))).containsExactly(1L, 2L);
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key")))
        .isEqualTo(Value.of(Arrays.asList(Value.of(1L), Value.of(2L))));

    // forEach should show LONG_ARRAY type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(entry(ExtendedAttributeKey.longArrayKey("key"), Arrays.asList(1L, 2L)));

    // asMap should show LONG_ARRAY type
    assertThat(attributes.asMap())
        .containsExactly(entry(ExtendedAttributeKey.longArrayKey("key"), Arrays.asList(1L, 2L)));
  }

  @Test
  void complexValueStoredAsDoubleArray() {
    // When putting a VALUE attribute with a homogeneous double array, it should be stored as
    // DOUBLE_ARRAY type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(
                ExtendedAttributeKey.valueKey("key"),
                Value.of(Arrays.asList(Value.of(1.1), Value.of(2.2))))
            .build();

    // Should be stored as DOUBLE_ARRAY type internally
    assertThat(attributes.get(ExtendedAttributeKey.doubleArrayKey("key")))
        .containsExactly(1.1, 2.2);
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key")))
        .isEqualTo(Value.of(Arrays.asList(Value.of(1.1), Value.of(2.2))));

    // forEach should show DOUBLE_ARRAY type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(
            entry(ExtendedAttributeKey.doubleArrayKey("key"), Arrays.asList(1.1, 2.2)));

    // asMap should show DOUBLE_ARRAY type
    assertThat(attributes.asMap())
        .containsExactly(
            entry(ExtendedAttributeKey.doubleArrayKey("key"), Arrays.asList(1.1, 2.2)));
  }

  @Test
  void complexValueStoredAsBooleanArray() {
    // When putting a VALUE attribute with a homogeneous boolean array, it should be stored as
    // BOOLEAN_ARRAY type
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(
                ExtendedAttributeKey.valueKey("key"),
                Value.of(Arrays.asList(Value.of(true), Value.of(false))))
            .build();

    // Should be stored as BOOLEAN_ARRAY type internally
    assertThat(attributes.get(ExtendedAttributeKey.booleanArrayKey("key")))
        .containsExactly(true, false);
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key")))
        .isEqualTo(Value.of(Arrays.asList(Value.of(true), Value.of(false))));

    // forEach should show BOOLEAN_ARRAY type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(
            entry(ExtendedAttributeKey.booleanArrayKey("key"), Arrays.asList(true, false)));

    // asMap should show BOOLEAN_ARRAY type
    assertThat(attributes.asMap())
        .containsExactly(
            entry(ExtendedAttributeKey.booleanArrayKey("key"), Arrays.asList(true, false)));
  }

  @Test
  void simpleAttributeRetrievedAsComplexValue() {
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put("string", "test")
            .put("long", 123L)
            .put("double", 1.23)
            .put("boolean", true)
            .put("stringArray", "a", "b")
            .put("longArray", 1L, 2L)
            .put("doubleArray", 1.1, 2.2)
            .put("booleanArray", true, false)
            .build();
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("string"))).isEqualTo(Value.of("test"));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("long"))).isEqualTo(Value.of(123L));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("double"))).isEqualTo(Value.of(1.23));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("boolean"))).isEqualTo(Value.of(true));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("stringArray")))
        .isEqualTo(Value.of(Arrays.asList(Value.of("a"), Value.of("b"))));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("longArray")))
        .isEqualTo(Value.of(Arrays.asList(Value.of(1L), Value.of(2L))));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("doubleArray")))
        .isEqualTo(Value.of(Arrays.asList(Value.of(1.1), Value.of(2.2))));
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("booleanArray")))
        .isEqualTo(Value.of(Arrays.asList(Value.of(true), Value.of(false))));
  }

  @Test
  void emptyValueArrayRetrievedAsAnyArrayType() {
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(ExtendedAttributeKey.valueKey("key"), Value.of(Collections.emptyList()))
            .build();
    assertThat(attributes.get(ExtendedAttributeKey.stringArrayKey("key"))).isEmpty();
    assertThat(attributes.get(ExtendedAttributeKey.longArrayKey("key"))).isEmpty();
    assertThat(attributes.get(ExtendedAttributeKey.doubleArrayKey("key"))).isEmpty();
    assertThat(attributes.get(ExtendedAttributeKey.booleanArrayKey("key"))).isEmpty();
  }

  @Test
  void getNullKey() {
    ExtendedAttributes attributes = ExtendedAttributes.builder().put("key", "value").build();
    assertThat(attributes.get((ExtendedAttributeKey<?>) null)).isNull();
  }

  @Test
  void putNullKey() {
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put((ExtendedAttributeKey<String>) null, "value").build();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void putNullValue() {
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put(ExtendedAttributeKey.stringKey("key"), null).build();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void putEmptyKey() {
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put(ExtendedAttributeKey.stringKey(""), "value").build();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void extendedAttributesNotConvertibleToValue() {
    ExtendedAttributes nested = ExtendedAttributes.builder().put("child", "value").build();
    ExtendedAttributes attributes =
        ExtendedAttributes.builder()
            .put(ExtendedAttributeKey.extendedAttributesKey("key"), nested)
            .build();

    // Getting as VALUE should return null since EXTENDED_ATTRIBUTES cannot be converted to Value
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isNull();
  }

  @Test
  void complexValueWithKeyValueList() {
    // KEY_VALUE_LIST should be kept as VALUE type
    Value<?> kvListValue = Value.of(Collections.emptyMap());
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put(ExtendedAttributeKey.valueKey("key"), kvListValue).build();

    // Should be stored as VALUE type
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(kvListValue);

    // forEach should show VALUE type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(entry(ExtendedAttributeKey.valueKey("key"), kvListValue));
  }

  @Test
  void complexValueWithBytes() {
    // BYTES should be kept as VALUE type
    Value<?> bytesValue = Value.of(new byte[] {1, 2, 3});
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put(ExtendedAttributeKey.valueKey("key"), bytesValue).build();

    // Should be stored as VALUE type
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(bytesValue);

    // forEach should show VALUE type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(entry(ExtendedAttributeKey.valueKey("key"), bytesValue));
  }

  @Test
  void complexValueWithNonHomogeneousArray() {
    // Non-homogeneous array should be kept as VALUE type
    Value<?> mixedArray = Value.of(Arrays.asList(Value.of("string"), Value.of(123L)));
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put(ExtendedAttributeKey.valueKey("key"), mixedArray).build();

    // Should be stored as VALUE type
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(mixedArray);

    // forEach should show VALUE type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(entry(ExtendedAttributeKey.valueKey("key"), mixedArray));
  }

  @Test
  void complexValueWithNestedArray() {
    // Array containing arrays should be kept as VALUE type
    Value<?> nestedArray =
        Value.of(
            Arrays.asList(
                Value.of(Arrays.asList(Value.of("a"), Value.of("b"))),
                Value.of(Arrays.asList(Value.of("c"), Value.of("d")))));
    ExtendedAttributes attributes =
        ExtendedAttributes.builder().put(ExtendedAttributeKey.valueKey("key"), nestedArray).build();

    // Should be stored as VALUE type
    assertThat(attributes.get(ExtendedAttributeKey.valueKey("key"))).isEqualTo(nestedArray);

    // forEach should show VALUE type
    Map<ExtendedAttributeKey<?>, Object> entriesSeen = new HashMap<>();
    attributes.forEach(entriesSeen::put);
    assertThat(entriesSeen)
        .containsExactly(entry(ExtendedAttributeKey.valueKey("key"), nestedArray));
  }

  @Test
  void getNonExistentArrayType() {
    // Test the code path where we look for an array type that doesn't exist
    ExtendedAttributes attributes = ExtendedAttributes.builder().put("key", "value").build();

    // Looking for an array type when only a string exists should return null
    assertThat(attributes.get(ExtendedAttributeKey.stringArrayKey("key"))).isNull();
    assertThat(attributes.get(ExtendedAttributeKey.longArrayKey("key"))).isNull();
    assertThat(attributes.get(ExtendedAttributeKey.doubleArrayKey("key"))).isNull();
    assertThat(attributes.get(ExtendedAttributeKey.booleanArrayKey("key"))).isNull();
  }
}
