/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;

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

    // Default methods with fallback values
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
    assertThat(DeclarativeConfigProperties.toMap(empty)).isNotNull().isEmpty();
  }

  @Test
  void declarativeConfigException() {
    DeclarativeConfigException ex1 = new DeclarativeConfigException("test error");
    assertThat(ex1.getMessage()).isEqualTo("test error");

    RuntimeException cause = new RuntimeException("cause");
    DeclarativeConfigException ex2 = new DeclarativeConfigException("error with cause", cause);
    assertThat(ex2.getMessage()).isEqualTo("error with cause");
    assertThat(ex2.getCause()).isSameAs(cause);
  }
}
