/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/** Assertions for {@link EventData}. */
public final class EventDataAssert extends AbstractAssert<EventDataAssert, EventData> {
  EventDataAssert(@Nullable EventData actual) {
    super(actual, EventDataAssert.class);
  }

  /** Asserts the event has the given name. */
  public EventDataAssert hasName(String name) {
    isNotNull();
    if (!actual.getName().equals(name)) {
      failWithActualExpectedAndMessage(
          actual.getName(),
          name,
          "Expected event to have name <%s> but was <%s>",
          name,
          actual.getName());
    }
    return this;
  }

  /** Asserts the event has the given timestamp, in nanos. */
  public EventDataAssert hasTimestamp(long timestampNanos) {
    isNotNull();
    if (actual.getEpochNanos() != timestampNanos) {
      failWithActualExpectedAndMessage(
          actual.getEpochNanos(),
          timestampNanos,
          "Expected event [%s] to have timestamp <%s> nanos but was <%s>",
          actual.getName(),
          timestampNanos,
          actual.getEpochNanos());
    }
    return this;
  }

  /** Asserts the event has the given timestamp. */
  @SuppressWarnings("PreferJavaTimeOverload")
  public EventDataAssert hasTimestamp(long timestamp, TimeUnit unit) {
    return hasTimestamp(unit.toNanos(timestamp));
  }

  /** Asserts the event has the given timestamp, in nanos. */
  public EventDataAssert hasTimestamp(Instant timestamp) {
    return hasTimestamp(TimeUnit.SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano());
  }

  /** Asserts the event has the given attributes. */
  public EventDataAssert hasAttributes(Attributes attributes) {
    isNotNull();
    if (!actual.getAttributes().equals(attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected event [%s] to have attributes <%s> but was <%s>",
          actual.getName(),
          attributes,
          actual.getAttributes());
    }
    return this;
  }

  /** Asserts the event has attributes satisfying the given condition. */
  public EventDataAssert hasAttributesSatisfying(Consumer<Attributes> attributes) {
    isNotNull();
    assertThat(actual.getAttributes()).as("attributes").satisfies(attributes);
    return this;
  }

  /**
   * Asserts the event has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public EventDataAssert hasAttributesSatisfying(AttributeAssertion... assertions) {
    return hasAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the event has attributes matching all {@code assertions}. Assertions can be created
   * using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public EventDataAssert hasAttributesSatisfying(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(actual.getAttributes(), assertions);
    return this;
  }

  /**
   * Asserts the event has attributes matching all {@code assertions} and no more. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public EventDataAssert hasAttributesSatisfyingExactly(AttributeAssertion... assertions) {
    return hasAttributesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts the event has attributes matching all {@code assertions} and no more. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   *
   * @since 1.21.0
   */
  public EventDataAssert hasAttributesSatisfyingExactly(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(actual.getAttributes(), assertions);
    return this;
  }
}
