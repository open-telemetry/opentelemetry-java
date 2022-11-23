/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/** Test assertions for {@link LogRecordData}. */
public class LogRecordDataAssert extends AbstractAssert<LogRecordDataAssert, LogRecordData> {
  protected LogRecordDataAssert(LogRecordData actual) {
    super(actual, LogRecordDataAssert.class);
  }

  /** Asserts the {@link Resource} associated with a log matches the expected value. */
  public LogRecordDataAssert hasResource(Resource resource) {
    isNotNull();
    if (!actual.getResource().equals(resource)) {
      failWithActualExpectedAndMessage(
          actual,
          "resource: " + resource,
          "Expected log to have resource <%s> but found <%s>",
          resource,
          actual.getResource());
    }
    return this;
  }

  /**
   * Asserts the {@link InstrumentationScopeInfo} associated with a log matches the expected value.
   */
  public LogRecordDataAssert hasInstrumentationScope(
      InstrumentationScopeInfo instrumentationScopeInfo) {
    isNotNull();
    if (!actual.getInstrumentationScopeInfo().equals(instrumentationScopeInfo)) {
      failWithActualExpectedAndMessage(
          actual,
          "instrumentation scope: " + instrumentationScopeInfo,
          "Expected log to have scope <%s> but found <%s>",
          instrumentationScopeInfo,
          actual.getInstrumentationScopeInfo());
    }
    return this;
  }

  /** Asserts the log has the given epoch timestamp. */
  public LogRecordDataAssert hasEpochNanos(long epochNanos) {
    isNotNull();
    if (actual.getEpochNanos() != epochNanos) {
      failWithActualExpectedAndMessage(
          actual.getEpochNanos(),
          epochNanos,
          "Expected log to have epoch <%s> nanos but was <%s>",
          epochNanos,
          actual.getEpochNanos());
    }
    return this;
  }

  /** Asserts the log has the given span context. */
  public LogRecordDataAssert hasSpanContext(SpanContext spanContext) {
    isNotNull();
    if (!actual.getSpanContext().equals(spanContext)) {
      failWithActualExpectedAndMessage(
          actual.getSpanContext(),
          spanContext,
          "Expected log to have span context <%s> nanos but was <%s>",
          spanContext,
          actual.getSpanContext());
    }
    return this;
  }

  /** Asserts the log has the given severity. */
  public LogRecordDataAssert hasSeverity(Severity severity) {
    isNotNull();
    if (actual.getSeverity() != severity) {
      failWithActualExpectedAndMessage(
          actual.getSeverity(),
          severity,
          "Expected log to have severity <%s> but was <%s>",
          severity,
          actual.getSeverity());
    }
    return this;
  }

  /** Asserts the log has the given severity text. */
  public LogRecordDataAssert hasSeverityText(String severityText) {
    isNotNull();
    if (!severityText.equals(actual.getSeverityText())) {
      failWithActualExpectedAndMessage(
          actual.getSeverityText(),
          severityText,
          "Expected log to have severity text <%s> but was <%s>",
          severityText,
          actual.getSeverityText());
    }
    return this;
  }

  /** Asserts the log has the given body. */
  public LogRecordDataAssert hasBody(String body) {
    isNotNull();
    if (!actual.getBody().asString().equals(body)) {
      failWithActualExpectedAndMessage(
          actual.getBody(),
          body,
          "Expected log to have body <%s> but was <%s>",
          body,
          actual.getBody().asString());
    }
    return this;
  }

  /** Asserts the log has the given attributes. */
  public LogRecordDataAssert hasAttributes(Attributes attributes) {
    isNotNull();
    if (!attributesAreEqual(attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected log to have attributes <%s> but was <%s>",
          attributes,
          actual.getAttributes());
    }
    return this;
  }

  /** Asserts the log has the given attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final LogRecordDataAssert hasAttributes(
      Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasAttributes(attributes);
  }

  /** Asserts the log has attributes satisfying the given condition. */
  public LogRecordDataAssert hasAttributesSatisfying(Consumer<Attributes> attributes) {
    isNotNull();
    assertThat(actual.getAttributes()).as("attributes").satisfies(attributes);
    return this;
  }

  /**
   * Asserts the log has attributes matching all {@code assertions}. Assertions can be created using
   * methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public LogRecordDataAssert hasAttributesSatisfying(AttributeAssertion... assertions) {
    return hasAttributesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the log has attributes matching all {@code assertions}. Assertions can be created using
   * methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public LogRecordDataAssert hasAttributesSatisfying(Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributes(actual.getAttributes(), assertions);
    return myself;
  }

  /**
   * Asserts the log has attributes matching all {@code assertions} and no more. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public LogRecordDataAssert hasAttributesSatisfyingExactly(AttributeAssertion... assertions) {
    return hasAttributesSatisfyingExactly(Arrays.asList(assertions));
  }

  /**
   * Asserts the log has attributes matching all {@code assertions} and no more. Assertions can be
   * created using methods like {@link OpenTelemetryAssertions#satisfies(AttributeKey,
   * OpenTelemetryAssertions.LongAssertConsumer)}.
   */
  public LogRecordDataAssert hasAttributesSatisfyingExactly(
      Iterable<AttributeAssertion> assertions) {
    AssertUtil.assertAttributesExactly(actual.getAttributes(), assertions);
    return myself;
  }

  private boolean attributesAreEqual(Attributes attributes) {
    // compare as maps, since implementations do not have equals that work correctly across
    // implementations.
    return actual.getAttributes().asMap().equals(attributes.asMap());
  }

  /** Asserts the log has the given total attributes. */
  public LogRecordDataAssert hasTotalAttributeCount(int totalAttributeCount) {
    isNotNull();
    if (actual.getTotalAttributeCount() != totalAttributeCount) {
      failWithActualExpectedAndMessage(
          actual.getTotalAttributeCount(),
          totalAttributeCount,
          "Expected log to have recorded <%s> total attributes but did not",
          totalAttributeCount);
    }
    return this;
  }
}
