/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for an exported {@link DoubleExemplarData}.
 *
 * @since 1.14.0
 */
public final class DoubleExemplarAssert
    extends AbstractAssert<DoubleExemplarAssert, DoubleExemplarData> {
  DoubleExemplarAssert(@Nullable DoubleExemplarData actual) {
    super(actual, DoubleExemplarAssert.class);
  }

  /** Asserts the exemplar has the given epoch timestamp, in nanos. */
  public DoubleExemplarAssert hasEpochNanos(long expected) {
    isNotNull();
    assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given span ID. */
  public DoubleExemplarAssert hasSpanId(String expected) {
    isNotNull();
    assertThat(actual.getSpanContext().getSpanId()).as("spanId").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given trace ID. */
  public DoubleExemplarAssert hasTraceId(String expected) {
    isNotNull();
    assertThat(actual.getSpanContext().getTraceId()).as("traceId").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given value. */
  public DoubleExemplarAssert hasValue(double expected) {
    isNotNull();
    assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given filtered attribute. */
  public <T> DoubleExemplarAssert hasFilteredAttribute(AttributeKey<T> key, T value) {
    return hasFilteredAttribute(OpenTelemetryAssertions.equalTo(key, value));
  }

  /** Asserts the exemplar has the given filtered attribute. */
  public DoubleExemplarAssert hasFilteredAttribute(AttributeAssertion attributeAssertion) {
    isNotNull();

    Set<AttributeKey<?>> actualKeys = actual.getFilteredAttributes().asMap().keySet();
    AttributeKey<?> key = attributeAssertion.getKey();

    assertThat(actualKeys).as("attribute keys").contains(key);

    Object value = actual.getFilteredAttributes().get(key);
    AbstractAssert<?, ?> assertion = AttributeAssertion.attributeValueAssertion(key, value);
    attributeAssertion.getAssertion().accept(assertion);

    return this;
  }

  /** Asserts the exemplar has the given filtered attributes. */
  public DoubleExemplarAssert hasFilteredAttributes(Attributes expected) {
    isNotNull();
    assertThat(actual.getFilteredAttributes()).as("filtered_attributes").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given filtered attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final DoubleExemplarAssert hasFilteredAttributes(
      Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasFilteredAttributes(attributes);
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions}. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public DoubleExemplarAssert hasFilteredAttributesSatisfying(AttributeAssertion... assertions) {
    return hasFilteredAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions}. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public DoubleExemplarAssert hasFilteredAttributesSatisfying(
      Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(actual.getFilteredAttributes(), assertions);
    return myself;
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions} and no more.
   * Assertions can be created using methods like {@link
   * OpenTelemetryAssertions#satisfies(AttributeKey, OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public DoubleExemplarAssert hasFilteredAttributesSatisfyingExactly(
      AttributeAssertion... assertions) {
    return hasFilteredAttributesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions} and no more.
   * Assertions can be created using methods like {@link
   * OpenTelemetryAssertions#satisfies(AttributeKey, OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public DoubleExemplarAssert hasFilteredAttributesSatisfyingExactly(
      Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(actual.getFilteredAttributes(), assertions);
    return myself;
  }
}
