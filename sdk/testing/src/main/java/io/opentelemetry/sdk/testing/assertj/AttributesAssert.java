/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

/** Assertions for {@link Attributes}. */
public class AttributesAssert extends AbstractAssert<AttributesAssert, Attributes> {
  AttributesAssert(Attributes actual) {
    super(actual, AttributesAssert.class);
  }

  /** Asserts the attributes have the given key and value. */
  public <T> AttributesAssert containsEntry(AttributeKey<T> key, T value) {
    isNotNull();
    T actualValue = actual.get(key);
    if (!Objects.equals(actualValue, value)) {
      failWithActualExpectedAndMessage(
          actualValue,
          value,
          "Expected attributes to have key <%s> with value <%s> but was value <%s>",
          key,
          value,
          actualValue);
    }
    return this;
  }

  /** Asserts the attributes have the given key and string value. */
  public AttributesAssert containsEntry(String key, String value) {
    return containsEntry(AttributeKey.stringKey(key), value);
  }

  /** Asserts the attributes have the given key and boolean value. */
  public AttributesAssert containsEntry(String key, boolean value) {
    return containsEntry(AttributeKey.booleanKey(key), value);
  }

  /** Asserts the attributes have the given key and long value. */
  public AttributesAssert containsEntry(String key, long value) {
    return containsEntry(AttributeKey.longKey(key), value);
  }

  /** Asserts the attributes have the given key and double value. */
  public AttributesAssert containsEntry(String key, double value) {
    return containsEntry(AttributeKey.doubleKey(key), value);
  }

  /** Asserts the attributes have the given key and string array value. */
  public AttributesAssert containsEntry(String key, String... value) {
    return containsEntry(AttributeKey.stringArrayKey(key), Arrays.asList(value));
  }

  /** Asserts the attributes have the given key and boolean array value. */
  public AttributesAssert containsEntry(String key, Boolean... value) {
    return containsEntry(AttributeKey.booleanArrayKey(key), Arrays.asList(value));
  }

  /** Asserts the attributes have the given key and long array value. */
  public AttributesAssert containsEntry(String key, Long... value) {
    return containsEntry(AttributeKey.longArrayKey(key), Arrays.asList(value));
  }

  /** Asserts the attributes have the given key and double array value. */
  public AttributesAssert containsEntry(String key, Double... value) {
    return containsEntry(AttributeKey.doubleArrayKey(key), Arrays.asList(value));
  }

  /** Asserts the attributes have the given key and string array value. */
  public AttributesAssert containsEntryWithStringValuesOf(String key, Iterable<String> value) {
    isNotNull();
    List<String> actualValue = actual.get(AttributeKey.stringArrayKey(key));
    assertThat(actualValue)
        .withFailMessage(
            "Expected attributes to have key <%s> with value <%s> but was <%s>",
            key, value, actualValue)
        .containsExactlyElementsOf(value);
    return this;
  }

  /** Asserts the attributes have the given key and boolean array value. */
  public AttributesAssert containsEntryWithBooleanValuesOf(String key, Iterable<Boolean> value) {
    isNotNull();
    List<Boolean> actualValue = actual.get(AttributeKey.booleanArrayKey(key));
    assertThat(actualValue)
        .withFailMessage(
            "Expected attributes to have key <%s> with value <%s> but was <%s>",
            key, value, actualValue)
        .containsExactlyElementsOf(value);
    return this;
  }

  /** Asserts the attributes have the given key and long array value. */
  public AttributesAssert containsEntryWithLongValuesOf(String key, Iterable<Long> value) {
    isNotNull();
    List<Long> actualValue = actual.get(AttributeKey.longArrayKey(key));
    assertThat(actualValue)
        .withFailMessage(
            "Expected attributes to have key <%s> with value <%s> but was <%s>",
            key, value, actualValue)
        .containsExactlyElementsOf(value);
    return this;
  }

  /** Asserts the attributes have the given key and double array value. */
  public AttributesAssert containsEntryWithDoubleValuesOf(String key, Iterable<Double> value) {
    isNotNull();
    List<Double> actualValue = actual.get(AttributeKey.doubleArrayKey(key));
    assertThat(actualValue)
        .withFailMessage(
            "Expected attributes to have key <%s> with value <%s> but was <%s>",
            key, value, actualValue)
        .containsExactlyElementsOf(value);
    return this;
  }
}
