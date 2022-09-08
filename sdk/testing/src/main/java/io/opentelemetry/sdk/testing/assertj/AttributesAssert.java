/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.ShouldContainKeys;
import org.assertj.core.error.ShouldNotContainKeys;

/** Assertions for {@link Attributes}. */
public final class AttributesAssert extends AbstractAssert<AttributesAssert, Attributes> {

  AttributesAssert(@Nullable Attributes actual) {
    super(actual, AttributesAssert.class);
  }

  /** Asserts the attributes have the given key and value. */
  public <T> AttributesAssert containsEntry(AttributeKey<T> key, T value) {
    isNotNull();
    containsKey(key);
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

  /** Asserts the attributes have the given key and value. */
  public AttributesAssert containsEntry(AttributeKey<Long> key, int value) {
    return containsEntry(key, (long) value);
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
    containsKey(key);
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
    containsKey(key);
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
    containsKey(key);
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
    containsKey(key);
    List<Double> actualValue = actual.get(AttributeKey.doubleArrayKey(key));
    assertThat(actualValue)
        .withFailMessage(
            "Expected attributes to have key <%s> with value <%s> but was <%s>",
            key, value, actualValue)
        .containsExactlyElementsOf(value);
    return this;
  }

  /** Asserts the attributes have the given key with a value satisfying the given condition. */
  public <T> AttributesAssert hasEntrySatisfying(AttributeKey<T> key, Consumer<T> valueCondition) {
    isNotNull();
    containsKey(key);
    assertThat(actual.get(key)).as("value").satisfies(valueCondition);
    return this;
  }

  /** Asserts the attributes only contain the given entries. */
  // NB: SafeVarArgs requires final on the method even if it's on the class.
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final AttributesAssert containsOnly(Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    isNotNull();
    assertThat(actual.asMap()).containsOnly(entries);
    return this;
  }

  /** Asserts the attributes contain the given key. */
  public AttributesAssert containsKey(AttributeKey<?> key) {
    if (actual.get(key) == null) {
      failWithMessage(
          ShouldContainKeys.shouldContainKeys(actual, Collections.singleton(key))
              .create(info.description(), info.representation()));
    }
    return this;
  }

  /** Asserts the attributes contain the given key. */
  public AttributesAssert containsKey(String key) {
    Optional<AttributeKey<?>> resolved =
        actual.asMap().keySet().stream()
            .filter(attributeKey -> attributeKey.getKey().equals(key))
            .findFirst();
    if (!resolved.isPresent()) {
      failWithMessage(
          ShouldContainKeys.shouldContainKeys(actual, Collections.singleton(key))
              .create(info.description(), info.representation()));
    }
    return this;
  }

  /**
   * Asserts the attributes do not contain the given key.
   *
   * @since 1.18.0
   */
  public AttributesAssert doesNotContainKey(AttributeKey<?> key) {
    if (actual.get(key) != null) {
      failWithMessage(
          ShouldNotContainKeys.shouldNotContainKeys(actual, Collections.singleton(key))
              .create(info.description(), info.representation()));
    }
    return this;
  }

  /**
   * Asserts the attributes do not contain the given key.
   *
   * @since 1.18.0
   */
  public AttributesAssert doesNotContainKey(String key) {
    boolean containsKey =
        actual.asMap().keySet().stream()
            .anyMatch(attributeKey -> attributeKey.getKey().equals(key));
    if (containsKey) {
      failWithMessage(
          ShouldNotContainKeys.shouldNotContainKeys(actual, Collections.singleton(key))
              .create(info.description(), info.representation()));
    }
    return this;
  }

  /** Asserts the attributes have no entries. */
  public AttributesAssert isEmpty() {
    return isEqualTo(Attributes.empty());
  }

  /** Asserts the number of attributes in the collection. */
  public AttributesAssert hasSize(int numberOfEntries) {
    int size = actual.size();
    if (size != numberOfEntries) {
      failWithActualExpectedAndMessage(
          size,
          numberOfEntries,
          "Expected attributes to have <%s> entries but actually has <%s>",
          numberOfEntries,
          size);
    }
    return this;
  }
}
