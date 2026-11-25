/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class DeclarativeConfigPropertiesTest {

  @Test
  void empty() {
    DeclarativeConfigProperties empty = DeclarativeConfigProperties.empty();
    assertThat(empty).isNotNull();
    assertThat(empty.getString("str")).isNull();
    assertThat(empty.getBoolean("bool")).isNull();
    assertThat(empty.getInt("int")).isNull();
    assertThat(empty.getLong("long")).isNull();
    assertThat(empty.getDouble("double")).isNull();
    assertThat(empty.getScalarList("list", String.class)).isNull();
    assertThat(empty.getStructured("struct")).isNull();
    assertThat(empty.getStructuredList("structList")).isNull();
    assertThat(empty.getPropertyKeys()).isEmpty();
    assertThat(empty.getComponentLoader()).isNotNull();

    // Default methods with default values
    assertThat(empty.getString("str", "def")).isEqualTo("def");
    assertThat(empty.getBoolean("bool", true)).isTrue();
    assertThat(empty.getInt("int", 42)).isEqualTo(42);
    assertThat(empty.getLong("long", 100L)).isEqualTo(100L);
    assertThat(empty.getDouble("double", 3.14)).isEqualTo(3.14);
    assertThat(empty.getScalarList("list", String.class, Collections.singletonList("d")))
        .containsExactly("d");
    assertThat(empty.getStructured("struct", empty)).isSameAs(empty);
    assertThat(empty.get("struct")).isNotNull();
    assertThat(empty.getStructuredList("structList", Collections.singletonList(empty)))
        .containsExactly(empty);

    // Default type check methods
    assertThat(empty.isString("str")).isFalse();
    assertThat(empty.isBoolean("bool")).isFalse();
    assertThat(empty.isInt("int")).isFalse();
    assertThat(empty.isLong("long")).isFalse();
    assertThat(empty.isDouble("double")).isFalse();
  }

  @Test
  void toMap() {
    DeclarativeConfigProperties empty = DeclarativeConfigProperties.empty();
    Map<String, Object> map = DeclarativeConfigProperties.toMap(empty);
    assertThat(map).isNotNull().isEmpty();
  }

  @Test
  void toMapWithValues() {
    DeclarativeConfigProperties props = mock(DeclarativeConfigProperties.class);
    DeclarativeConfigProperties subProps = mock(DeclarativeConfigProperties.class);
    DeclarativeConfigProperties listItemProps = mock(DeclarativeConfigProperties.class);

    when(props.getScalarList(anyString(), any())).thenReturn(null);
    when(props.getStructuredList(anyString())).thenReturn(null);
    when(props.getStructured(anyString())).thenReturn(null);

    when(props.getPropertyKeys())
        .thenReturn(
            new HashSet<>(
                Arrays.asList(
                    "str",
                    "bool",
                    "long",
                    "double",
                    "strList",
                    "boolList",
                    "longList",
                    "doubleList",
                    "struct",
                    "structList",
                    "unknown")));

    when(props.isString("str")).thenReturn(true);
    when(props.getString("str")).thenReturn("hello");

    when(props.isBoolean("bool")).thenReturn(true);
    when(props.getBoolean("bool")).thenReturn(true);

    when(props.isLong("long")).thenReturn(true);
    when(props.getLong("long")).thenReturn(123L);

    when(props.isDouble("double")).thenReturn(true);
    when(props.getDouble("double")).thenReturn(45.67);

    when(props.getScalarList("strList", String.class)).thenReturn(Arrays.asList("a", "b"));
    when(props.getScalarList("boolList", Boolean.class)).thenReturn(Arrays.asList(true, false));
    when(props.getScalarList("longList", Long.class)).thenReturn(Arrays.asList(1L, 2L));
    when(props.getScalarList("doubleList", Double.class)).thenReturn(Arrays.asList(1.1, 2.2));

    when(subProps.getPropertyKeys()).thenReturn(Collections.singleton("subKey"));
    when(subProps.isString("subKey")).thenReturn(true);
    when(subProps.getString("subKey")).thenReturn("subVal");
    when(props.getStructured("struct")).thenReturn(subProps);

    when(listItemProps.getPropertyKeys()).thenReturn(Collections.singleton("itemKey"));
    when(listItemProps.isLong("itemKey")).thenReturn(true);
    when(listItemProps.getLong("itemKey")).thenReturn(999L);
    when(props.getStructuredList("structList"))
        .thenReturn(Collections.singletonList(listItemProps));

    Map<String, Object> map = DeclarativeConfigProperties.toMap(props);
    assertThat(map).containsEntry("str", "hello");
    assertThat(map).containsEntry("bool", true);
    assertThat(map).containsEntry("long", 123L);
    assertThat(map).containsEntry("double", 45.67);
    assertThat(map).containsEntry("strList", Arrays.asList("a", "b"));
    assertThat(map).containsEntry("boolList", Arrays.asList(true, false));
    assertThat(map).containsEntry("longList", Arrays.asList(1L, 2L));
    assertThat(map).containsEntry("doubleList", Arrays.asList(1.1, 2.2));
    assertThat(map).containsEntry("struct", Collections.singletonMap("subKey", "subVal"));
    assertThat(map)
        .containsEntry(
            "structList", Collections.singletonList(Collections.singletonMap("itemKey", 999L)));
    assertThat(map).containsEntry("unknown", null);
  }

  @Test
  void declarativeConfigException() {
    DeclarativeConfigException ex1 = new DeclarativeConfigException("error message");
    assertThat(ex1.getMessage()).isEqualTo("error message");

    RuntimeException cause = new RuntimeException("cause");
    DeclarativeConfigException ex2 = new DeclarativeConfigException("error with cause", cause);
    assertThat(ex2.getMessage()).isEqualTo("error with cause");
    assertThat(ex2.getCause()).isSameAs(cause);
  }
}
