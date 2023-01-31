/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for {@link Resource}.
 *
 * @since 1.23.0
 */
public final class ResourceAssert extends AbstractAssert<ResourceAssert, Resource> {

  private final String label;

  ResourceAssert(Resource resource, String label) {
    super(resource, ResourceAssert.class);
    this.label = label;
  }

  /** Asserts the resource has a schemaUrl satisfying the given condition. */
  // Workaround "passing @Nullable parameter 'schemaUrl' where @NonNull is required", Nullaway
  // seems to think assertThat is supposed to be passed NonNull even though we know that can't be
  // true for assertions.
  @SuppressWarnings("NullAway")
  public ResourceAssert hasSchemaUrl(@Nullable String schemaUrl) {
    isNotNull();
    assertThat(actual.getSchemaUrl()).as("resource schema URL of %s", label).isEqualTo(schemaUrl);
    return this;
  }

  /** Asserts the resource has the given attribute. */
  public <T> ResourceAssert hasAttribute(AttributeKey<T> key, T value) {
    return hasAttribute(OpenTelemetryAssertions.equalTo(key, value));
  }

  /** Asserts the resource has an attribute matching the {@code attributeAssertion}. */
  public ResourceAssert hasAttribute(AttributeAssertion attributeAssertion) {
    isNotNull();

    Set<AttributeKey<?>> actualKeys = actual.getAttributes().asMap().keySet();
    AttributeKey<?> key = attributeAssertion.getKey();

    assertThat(actualKeys).as("resource attribute keys of %s", label).contains(key);

    Object value = actual.getAttributes().get(key);
    AbstractAssert<?, ?> assertion = AttributeAssertion.attributeValueAssertion(key, value);
    attributeAssertion.getAssertion().accept(assertion);

    return this;
  }

  /** Asserts the resource has the given attributes. */
  public ResourceAssert hasAttributes(Attributes attributes) {
    isNotNull();
    if (!AssertUtil.attributesAreEqual(actual.getAttributes(), attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected resource of <%s> to have attributes <%s> but was <%s>",
          label,
          attributes,
          actual.getAttributes());
    }
    return this;
  }

  /** Asserts the resource has the given attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final ResourceAssert hasAttributes(Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasAttributes(attributes);
  }

  /** Asserts the resource has attributes satisfying the given condition. */
  public ResourceAssert hasAttributesSatisfying(Consumer<Attributes> attributes) {
    isNotNull();
    OpenTelemetryAssertions.assertThat(actual.getAttributes())
        .as("resource attributes of %s", label)
        .satisfies(attributes);
    return this;
  }

  /**
   * Asserts the event has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ResourceAssert hasAttributesSatisfying(AttributeAssertion... assertions) {
    return hasAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the event has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ResourceAssert hasAttributesSatisfying(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(
        actual.getAttributes(), assertions, String.format("resource of %s attribute keys", label));
    return this;
  }

  /**
   * Asserts the resource has attributes matching all {@code assertions} and no more. Assertions can
   * be created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ResourceAssert hasAttributesSatisfyingExactly(AttributeAssertion... assertions) {
    return hasAttributesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts the resource has attributes matching all {@code assertions} and no more. Assertions can
   * be created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public ResourceAssert hasAttributesSatisfyingExactly(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(
        actual.getAttributes(), assertions, String.format("resource of %s attribute keys", label));
    return this;
  }
}
