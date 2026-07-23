/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AttributeValueLimitsTest {

  @Test
  void isValidLength_unlimited_alwaysTrue() {
    assertThat(AttributeValueLimits.isValidLength("anything", Integer.MAX_VALUE)).isTrue();
    assertThat(AttributeValueLimits.isValidLength(Arrays.asList("a", "b"), Integer.MAX_VALUE))
        .isTrue();
  }

  @Test
  void isValidLength_stringWithinLimit() {
    assertThat(AttributeValueLimits.isValidLength("abc", 3)).isTrue();
    assertThat(AttributeValueLimits.isValidLength("ab", 3)).isTrue();
  }

  @Test
  void isValidLength_stringOverLimit() {
    assertThat(AttributeValueLimits.isValidLength("abcd", 3)).isFalse();
  }

  @Test
  void isValidLength_numericScalar_alwaysTrue() {
    assertThat(AttributeValueLimits.isValidLength(42L, 0)).isTrue();
    assertThat(AttributeValueLimits.isValidLength(3.14, 0)).isTrue();
    assertThat(AttributeValueLimits.isValidLength(true, 0)).isTrue();
  }

  @Test
  void isValidLength_stringArrayWithinLimit() {
    assertThat(AttributeValueLimits.isValidLength(Arrays.asList("ab", "cd"), 3)).isTrue();
  }

  @Test
  void isValidLength_stringArrayWithOverLongEntry() {
    assertThat(AttributeValueLimits.isValidLength(Arrays.asList("ok", "way-too-long"), 3))
        .isFalse();
  }

  @Test
  void isValidLength_nestedValueWithOverLongString() {
    Value<?> nested = Value.of(Value.of("way-too-long"));
    assertThat(AttributeValueLimits.isValidLength(nested, 4)).isFalse();
  }

  @Test
  void isValidLength_nestedMapWithOverLongString() {
    Value<?> map = Value.of(KeyValue.of("k", Value.of("way-too-long")));
    assertThat(AttributeValueLimits.isValidLength(map, 4)).isFalse();
  }

  @Test
  void applyLengthLimit_returnsInputWhenUnchanged() {
    String input = "abc";
    assertThat(AttributeValueLimits.applyLengthLimit(input, 5)).isSameAs(input);
  }

  @Test
  void apply_unlimited_returnsInput() {
    Value<?> value = Value.of(Value.of("anything"));
    assertThat(AttributeValueLimits.apply(value, Integer.MAX_VALUE, Integer.MAX_VALUE))
        .isSameAs(value);
  }
}
