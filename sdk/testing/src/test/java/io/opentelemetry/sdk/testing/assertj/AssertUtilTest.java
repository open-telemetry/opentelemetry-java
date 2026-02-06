/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssertUtilTest {
  private static final AttributeKey<Boolean> WARM = AttributeKey.booleanKey("warm");
  private static final AttributeKey<Long> TEMPERATURE = AttributeKey.longKey("temperature");
  private static final AttributeKey<Double> LENGTH = AttributeKey.doubleKey("length");
  private static final AttributeKey<List<String>> COLORS = AttributeKey.stringArrayKey("colors");
  private static final AttributeKey<Value<?>> BYTES = AttributeKey.valueKey("bytes");

  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put(WARM, true)
          .put(TEMPERATURE, 30)
          .put(LENGTH, 1.2)
          .put(COLORS, Arrays.asList("red", "blue"))
          .put(BYTES, Value.of(new byte[] {1, 2, 3}))
          .build();

  @Test
  void assertAttributesShouldThrowIfNoAttributeMatch() {
    List<AttributeAssertion> assertions = Arrays.asList(equalTo(WARM, false));

    assertThatThrownBy(() -> AssertUtil.assertAttributes(ATTRIBUTES, assertions))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void assertAttributesShouldNotThrowIfSomeAttributesMatch() {
    List<AttributeAssertion> assertions = Arrays.asList(equalTo(WARM, true));

    AssertUtil.assertAttributes(ATTRIBUTES, assertions);
  }

  @Test
  void assertAttributesShouldNotThrowIfAllAttributesMatch() {
    List<AttributeAssertion> assertions =
        Arrays.asList(
            equalTo(WARM, true),
            equalTo(TEMPERATURE, 30L),
            equalTo(LENGTH, 1.2),
            equalTo(COLORS, Arrays.asList("red", "blue")),
            equalTo(BYTES, Value.of(new byte[] {1, 2, 3})));

    AssertUtil.assertAttributes(ATTRIBUTES, assertions);
  }

  @Test
  void assertAttributesExactlyShouldThrowIfNoAttributeMatch() {
    List<AttributeAssertion> assertions = Arrays.asList(equalTo(WARM, false));

    assertThatThrownBy(() -> AssertUtil.assertAttributesExactly(ATTRIBUTES, assertions))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void assertAttributesExactlyShouldThrowIfSomeAttributesMatch() {
    List<AttributeAssertion> assertions = Arrays.asList(equalTo(WARM, true));

    assertThatThrownBy(() -> AssertUtil.assertAttributesExactly(ATTRIBUTES, assertions))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void assertAttributesExactlyShouldNotThrowIfAllAttributesMatch() {
    List<AttributeAssertion> assertions =
        Arrays.asList(
            equalTo(WARM, true),
            equalTo(TEMPERATURE, 30L),
            equalTo(LENGTH, 1.2),
            equalTo(COLORS, Arrays.asList("red", "blue")),
            equalTo(BYTES, Value.of(new byte[] {1, 2, 3})));

    AssertUtil.assertAttributesExactly(ATTRIBUTES, assertions);
  }
}
