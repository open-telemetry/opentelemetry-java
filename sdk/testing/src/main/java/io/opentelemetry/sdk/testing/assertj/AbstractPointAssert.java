/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 * Assertions for an exported {@link PointData}.
 *
 * @since 1.14.0
 */
public abstract class AbstractPointAssert<
        PointAssertT extends AbstractPointAssert<PointAssertT, PointT>, PointT extends PointData>
    extends AbstractAssert<PointAssertT, PointT> {

  AbstractPointAssert(@Nullable PointT actual, Class<PointAssertT> assertClass) {
    super(actual, assertClass);
  }

  /** Asserts the point has the given start epoch timestamp, in nanos. */
  public final PointAssertT hasStartEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getStartEpochNanos()).as("startEpochNanos").isEqualTo(expected);
    return myself;
  }

  /** Asserts the point has the given epoch timestamp, in nanos. */
  public final PointAssertT hasEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return myself;
  }

  /** Asserts the point has the given attribute. */
  public final <T> PointAssertT hasAttribute(AttributeKey<T> key, T value) {
    return hasAttribute(OpenTelemetryAssertions.equalTo(key, value));
  }

  /** Asserts the point has an attribute matching the {@code attributeAssertion}. */
  public final PointAssertT hasAttribute(AttributeAssertion attributeAssertion) {
    isNotNull();

    Set<AttributeKey<?>> actualKeys = actual.getAttributes().asMap().keySet();
    AttributeKey<?> key = attributeAssertion.getKey();

    assertThat(actualKeys).as("attribute keys").contains(key);

    Object value = actual.getAttributes().get(key);
    AbstractAssert<?, ?> assertion = AttributeAssertion.attributeValueAssertion(key, value);
    attributeAssertion.getAssertion().accept(assertion);

    return myself;
  }

  /** Asserts the point has the given attributes. */
  public final PointAssertT hasAttributes(Attributes attributes) {
    isNotNull();
    if (!AssertUtil.attributesAreEqual(actual.getAttributes(), attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected point to have attributes <%s> but was <%s>",
          attributes,
          actual.getAttributes());
    }
    return myself;
  }

  /** Asserts the point has the given attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final PointAssertT hasAttributes(Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasAttributes(attributes);
  }

  /**
   * Asserts the point has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public final PointAssertT hasAttributesSatisfying(AttributeAssertion... assertions) {
    return hasAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the point has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public final PointAssertT hasAttributesSatisfying(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(actual.getAttributes(), assertions);
    return myself;
  }

  /**
   * Asserts the point has attributes matching all {@code assertions} and no more. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public final PointAssertT hasAttributesSatisfyingExactly(AttributeAssertion... assertions) {
    return hasAttributesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts the point has attributes matching all {@code assertions} and no more. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public final PointAssertT hasAttributesSatisfyingExactly(
      Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(actual.getAttributes(), assertions);
    return myself;
  }
}
