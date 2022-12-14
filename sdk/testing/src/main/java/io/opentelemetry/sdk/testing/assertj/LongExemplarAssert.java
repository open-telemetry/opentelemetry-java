/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for an exported {@link LongExemplarData}.
 *
 * @since 1.14.0
 */
public final class LongExemplarAssert extends AbstractAssert<LongExemplarAssert, LongExemplarData> {
  LongExemplarAssert(@Nullable LongExemplarData actual) {
    super(actual, LongExemplarAssert.class);
  }

  /** Asserts the exemplar has the given epoch timestamp, in nanos. */
  public LongExemplarAssert hasEpochNanos(long expected) {
    isNotNull();
    assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given span ID. */
  public LongExemplarAssert hasSpanId(String expected) {
    isNotNull();
    assertThat(actual.getSpanContext().getSpanId()).as("spanId").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given trace ID. */
  public LongExemplarAssert hasTraceId(String expected) {
    isNotNull();
    assertThat(actual.getSpanContext().getTraceId()).as("traceId").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given value. */
  public LongExemplarAssert hasValue(long expected) {
    isNotNull();
    assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given filtered attribute. */
  public <T> LongExemplarAssert hasFilteredAttribute(AttributeKey<T> key, T value) {
    return hasFilteredAttribute(OpenTelemetryAssertions.equalTo(key, value));
  }

  /** Asserts the exemplar has the given filtered attribute. */
  public LongExemplarAssert hasFilteredAttribute(AttributeAssertion attributeAssertion) {
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
  public LongExemplarAssert hasFilteredAttributes(Attributes expected) {
    isNotNull();
    assertThat(actual.getFilteredAttributes()).as("filtered_attributes").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given filtered attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final LongExemplarAssert hasFilteredAttributes(
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
  public LongExemplarAssert hasFilteredAttributesSatisfying(AttributeAssertion... assertions) {
    return hasFilteredAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions}. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public LongExemplarAssert hasFilteredAttributesSatisfying(
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
  public LongExemplarAssert hasFilteredAttributesSatisfyingExactly(
      AttributeAssertion... assertions) {
    return hasFilteredAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions} and no more.
   * Assertions can be created using methods like {@link
   * OpenTelemetryAssertions#satisfies(AttributeKey, OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public LongExemplarAssert hasFilteredAttributesSatisfyingExactly(
      Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(actual.getFilteredAttributes(), assertions);
    return myself;
  }
}
