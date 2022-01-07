/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/** Test assertions for {@link LogData}. */
public class LogDataAssert extends AbstractAssert<LogDataAssert, LogData> {
  protected LogDataAssert(LogData actual) {
    super(actual, LogDataAssert.class);
  }

  /** Asserts the {@link Resource} associated with a log matches the expected value. */
  public LogDataAssert hasResource(Resource resource) {
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
   * Asserts the {@link InstrumentationLibraryInfo} associated with a log matches the expected
   * value.
   */
  public LogDataAssert hasInstrumentationLibrary(
      InstrumentationLibraryInfo instrumentationLibrary) {
    isNotNull();
    if (!actual.getInstrumentationLibraryInfo().equals(instrumentationLibrary)) {
      failWithActualExpectedAndMessage(
          actual,
          "instrumentation library: " + instrumentationLibrary,
          "Expected log to have resource <%s> but found <%s>",
          instrumentationLibrary,
          actual.getInstrumentationLibraryInfo());
    }
    return this;
  }

  /** Asserts the log has the given epoch timestamp. */
  public LogDataAssert hasEpochNanos(long epochNanos) {
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
  public LogDataAssert hasSpanContext(SpanContext spanContext) {
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
  public LogDataAssert hasSeverity(Severity severity) {
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
  public LogDataAssert hasSeverityText(String severityText) {
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

  /** Asserts the log has the given name. */
  public LogDataAssert hasName(String name) {
    isNotNull();
    if (!name.equals(actual.getName())) {
      failWithActualExpectedAndMessage(
          actual.getName(),
          name,
          "Expected log to have name <%s> but was <%s>",
          name,
          actual.getName());
    }
    return this;
  }

  /** Asserts the log has the given body. */
  public LogDataAssert hasBody(String body) {
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
  public LogDataAssert hasAttributes(Attributes attributes) {
    isNotNull();
    if (!attributesAreEqual(attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected log to have attributes <%s> but was <%s>",
          actual.getName(),
          attributes,
          actual.getAttributes());
    }
    return this;
  }

  /** Asserts the log has the given attributes. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @SafeVarargs
  public final LogDataAssert hasAttributes(Map.Entry<? extends AttributeKey<?>, ?>... entries) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    for (Map.Entry<? extends AttributeKey<?>, ?> attr : entries) {
      attributesBuilder.put((AttributeKey) attr.getKey(), attr.getValue());
    }
    Attributes attributes = attributesBuilder.build();
    return hasAttributes(attributes);
  }

  /** Asserts the log has attributes satisfying the given condition. */
  public LogDataAssert hasAttributesSatisfying(Consumer<Attributes> attributes) {
    isNotNull();
    assertThat(actual.getAttributes()).as("attributes").satisfies(attributes);
    return this;
  }

  private boolean attributesAreEqual(Attributes attributes) {
    // compare as maps, since implementations do not have equals that work correctly across
    // implementations.
    return actual.getAttributes().asMap().equals(attributes.asMap());
  }
}
