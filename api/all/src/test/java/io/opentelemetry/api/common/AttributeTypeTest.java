/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

final class AttributeTypeTest {

  @ParameterizedTest
  @CsvSource(
      value = {
        "STRING, true",
        "BOOLEAN, true",
        "LONG, true",
        "DOUBLE, true",
        "STRING_ARRAY, false",
        "BOOLEAN_ARRAY, false",
        "LONG_ARRAY, false",
        "DOUBLE_ARRAY, false",
      },
      delimiterString = ",")
  void isPrimitive(AttributeType type, boolean expected) {
    assertThat(type.isPrimitive()).isEqualTo(expected);
  }

  @ParameterizedTest
  @EnumSource(AttributeType.class)
  void isPrimitive_CoversAllValues(AttributeType type) {
    assertDoesNotThrow(type::isPrimitive);
  }
}
