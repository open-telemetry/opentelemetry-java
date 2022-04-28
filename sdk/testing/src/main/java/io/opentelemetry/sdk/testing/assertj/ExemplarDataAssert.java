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
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/** Assertions for an exported {@link ExemplarData}. */
public final class ExemplarDataAssert extends AbstractAssert<ExemplarDataAssert, ExemplarData> {
  ExemplarDataAssert(@Nullable ExemplarData actual) {
    super(actual, ExemplarDataAssert.class);
  }

  /** Asserts the exemplar has the given epoch timestamp, in nanos. */
  public ExemplarDataAssert hasEpochNanos(long expected) {
    isNotNull();
    assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given span ID. */
  public ExemplarDataAssert hasSpanId(String expected) {
    isNotNull();
    assertThat(actual.getSpanContext().getSpanId()).as("spanId").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given trace ID. */
  public ExemplarDataAssert hasTraceId(String expected) {
    isNotNull();
    assertThat(actual.getSpanContext().getTraceId()).as("traceId").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given value. */
  public ExemplarDataAssert hasValue(double expected) {
    isNotNull();
    double value =
        actual instanceof DoubleExemplarData
            ? ((DoubleExemplarData) actual).getValue()
            : ((LongExemplarData) actual).getValue();
    assertThat(value).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given filtered attribute. */
  public <T> ExemplarDataAssert hasFilteredAttribute(AttributeKey<T> key, T value) {
    return hasFilteredAttribute(OpenTelemetryAssertions.equalTo(key, value));
  }

  /** Asserts the exemplar has the given filtered attribute. */
  public ExemplarDataAssert hasFilteredAttribute(AttributeAssertion attributeAssertion) {
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
  public ExemplarDataAssert hasFilteredAttributes(Attributes expected) {
    isNotNull();
    assertThat(actual.getFilteredAttributes()).as("filtered_attributes").isEqualTo(expected);
    return this;
  }

  /** Asserts the exemplar has the given filtered attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final ExemplarDataAssert hasFilteredAttributes(
      Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasFilteredAttributes(attributes);
  }

  /** Asserts the exemplar has filtered attributes matching all {@code assertions} and no more. */
  public ExemplarDataAssert hasFilteredAttributesSatisfying(AttributeAssertion... assertions) {
    return hasFilteredAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the exemplar has filtered attributes matching all {@code assertions} and no more.
   * Assertions can be created using methods like {@link
   * OpenTelemetryAssertions#satisfies(AttributeKey, OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ExemplarDataAssert hasFilteredAttributesSatisfying(
      Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(actual.getFilteredAttributes(), assertions);
    return myself;
  }
}
