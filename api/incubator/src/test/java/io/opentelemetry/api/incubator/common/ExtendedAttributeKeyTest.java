/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("deprecation") // Testing deprecated EXTENDED_ATTRIBUTES until removed
public class ExtendedAttributeKeyTest {

  @ParameterizedTest
  @MethodSource("attributeKeyArgs")
  void test(
      ExtendedAttributeKey<?> key,
      String expectedKey,
      ExtendedAttributeType expectedType,
      @Nullable AttributeKey<?> expectedAttributeKey) {
    assertThat(key.getKey()).isEqualTo(expectedKey);
    assertThat(key.getType()).isEqualTo(expectedType);
    assertThat(key.asAttributeKey()).isEqualTo(expectedAttributeKey);

    if (expectedAttributeKey != null) {
      ExtendedAttributeKey<?> extendedAttributeKey =
          ExtendedAttributeKey.fromAttributeKey(expectedAttributeKey);
      assertThat(extendedAttributeKey).isEqualTo(key);
    }
  }

  private static Stream<Arguments> attributeKeyArgs() {
    return Stream.of(
        Arguments.of(
            ExtendedAttributeKey.stringKey("key"),
            "key",
            ExtendedAttributeType.STRING,
            AttributeKey.stringKey("key")),
        Arguments.of(
            ExtendedAttributeKey.booleanKey("key"),
            "key",
            ExtendedAttributeType.BOOLEAN,
            AttributeKey.booleanKey("key")),
        Arguments.of(
            ExtendedAttributeKey.longKey("key"),
            "key",
            ExtendedAttributeType.LONG,
            AttributeKey.longKey("key")),
        Arguments.of(
            ExtendedAttributeKey.doubleKey("key"),
            "key",
            ExtendedAttributeType.DOUBLE,
            AttributeKey.doubleKey("key")),
        Arguments.of(
            ExtendedAttributeKey.stringArrayKey("key"),
            "key",
            ExtendedAttributeType.STRING_ARRAY,
            AttributeKey.stringArrayKey("key")),
        Arguments.of(
            ExtendedAttributeKey.booleanArrayKey("key"),
            "key",
            ExtendedAttributeType.BOOLEAN_ARRAY,
            AttributeKey.booleanArrayKey("key")),
        Arguments.of(
            ExtendedAttributeKey.longArrayKey("key"),
            "key",
            ExtendedAttributeType.LONG_ARRAY,
            AttributeKey.longArrayKey("key")),
        Arguments.of(
            ExtendedAttributeKey.doubleArrayKey("key"),
            "key",
            ExtendedAttributeType.DOUBLE_ARRAY,
            AttributeKey.doubleArrayKey("key")),
        Arguments.of(
            ExtendedAttributeKey.extendedAttributesKey("key"),
            "key",
            ExtendedAttributeType.EXTENDED_ATTRIBUTES,
            null),
        Arguments.of(
            ExtendedAttributeKey.valueKey("key"), "key", ExtendedAttributeType.VALUE, null));
  }
}
