/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.api.common.ValueType.KEY_VALUE_LIST;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertNotNull;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Test assertions for {@link LogRecordData}.
 *
 * @since 1.27.0
 */
public final class LogRecordDataAssert extends AbstractAssert<LogRecordDataAssert, LogRecordData> {

  private static final AttributeKey<String> EXCEPTION_TYPE =
      AttributeKey.stringKey("exception.type");
  private static final AttributeKey<String> EXCEPTION_MESSAGE =
      AttributeKey.stringKey("exception.message");
  private static final AttributeKey<String> EXCEPTION_STACKTRACE =
      AttributeKey.stringKey("exception.stacktrace");

  LogRecordDataAssert(@Nullable LogRecordData actual) {
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
   * Asserts the log has a resource satisfying the given condition.
   *
   * @since 1.29.0
   */
  public LogRecordDataAssert hasResourceSatisfying(Consumer<ResourceAssert> resource) {
    isNotNull();
    resource.accept(
        new ResourceAssert(actual.getResource(), String.format("log [%s]", actual.getBodyValue())));
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

  /**
   * Asserts the log has the given epoch {@code eventName}.
   *
   * @since 1.50.0
   */
  public LogRecordDataAssert hasEventName(String eventName) {
    isNotNull();
    if (!eventName.equals(actual.getEventName())) {
      failWithActualExpectedAndMessage(
          actual.getEventName(),
          eventName,
          "Expected log to have eventName <%s> but was <%s>",
          eventName,
          actual.getEventName());
    }
    return this;
  }

  /** Asserts the log has the given epoch {@code timestamp}. */
  public LogRecordDataAssert hasTimestamp(long timestampEpochNanos) {
    isNotNull();
    if (actual.getTimestampEpochNanos() != timestampEpochNanos) {
      failWithActualExpectedAndMessage(
          actual.getTimestampEpochNanos(),
          timestampEpochNanos,
          "Expected log to have timestamp <%s> nanos but was <%s>",
          timestampEpochNanos,
          actual.getTimestampEpochNanos());
    }
    return this;
  }

  /** Asserts the log has the given epoch {@code observedTimestamp}. */
  public LogRecordDataAssert hasObservedTimestamp(long observedEpochNanos) {
    isNotNull();
    if (actual.getObservedTimestampEpochNanos() != observedEpochNanos) {
      failWithActualExpectedAndMessage(
          actual.getObservedTimestampEpochNanos(),
          observedEpochNanos,
          "Expected log to have observed timestamp <%s> nanos but was <%s>",
          observedEpochNanos,
          actual.getObservedTimestampEpochNanos());
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
    return hasBody(Value.of(body));
  }

  /**
   * Asserts the log has the given body.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBody(@Nullable Value<?> body) {
    isNotNull();
    if (!Objects.equals(actual.getBodyValue(), body)) {
      failWithActualExpectedAndMessage(
          actual.getBodyValue(),
          body,
          "Expected log to have body <%s> but was <%s>",
          body,
          actual.getBodyValue());
    }
    return this;
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and String {@code value}.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, String value) {
    return hasBodyField(key, Value.of(value));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and long {@code value}.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, long value) {
    return hasBodyField(key, Value.of(value));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and double {@code value}.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, double value) {
    return hasBodyField(key, Value.of(value));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and boolean {@code value}.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, boolean value) {
    return hasBodyField(key, Value.of(value));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and list of String {@code value}s.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, String... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (String val : value) {
      values.add(Value.of(val));
    }
    return hasBodyField(key, Value.of(values));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and list of long {@code value}s.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, long... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (long val : value) {
      values.add(Value.of(val));
    }
    return hasBodyField(key, Value.of(values));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and list of double {@code value}s.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, double... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (double val : value) {
      values.add(Value.of(val));
    }
    return hasBodyField(key, Value.of(values));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and list of boolean {@code value}s.
   *
   * @since 1.42.0
   */
  public LogRecordDataAssert hasBodyField(String key, boolean... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (boolean val : value) {
      values.add(Value.of(val));
    }
    return hasBodyField(key, Value.of(values));
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given {@code key} and {@code value}.
   *
   * @since 1.42.0
   */
  @SuppressWarnings({"unchecked"})
  public LogRecordDataAssert hasBodyField(String key, Value<?> value) {
    isNotNull();
    Value<?> bodyValue = actual.getBodyValue();
    assertNotNull(
        "Body was not expected to be null.", bodyValue); // Can't use assertj or nullaway complains
    assertThat(bodyValue.getType()).isEqualTo(KEY_VALUE_LIST);
    Value<List<KeyValue>> body = (Value<List<KeyValue>>) bodyValue;
    List<KeyValue> payload = body.getValue();
    KeyValue expected = KeyValue.of(key, value);
    assertThat(payload).contains(expected);
    return this;
  }

  /**
   * Asserts the log has a body of type {@link ValueType#KEY_VALUE_LIST}, containing a field with
   * the given attribute {@code key} and {@code value}.
   *
   * @since 1.42.0
   */
  @SuppressWarnings({"unchecked"})
  public <T> LogRecordDataAssert hasBodyField(AttributeKey<T> key, T value) {
    switch (key.getType()) {
      case STRING:
        return hasBodyField(key.getKey(), (String) value);
      case BOOLEAN:
        return hasBodyField(key.getKey(), (boolean) value);
      case LONG:
        return hasBodyField(key.getKey(), (long) value);
      case DOUBLE:
        return hasBodyField(key.getKey(), (double) value);
      case STRING_ARRAY:
        return hasBodyField(
            key.getKey(),
            Value.of(((List<String>) value).stream().map(Value::of).collect(toList())));
      case BOOLEAN_ARRAY:
        return hasBodyField(
            key.getKey(),
            Value.of(((List<Boolean>) value).stream().map(Value::of).collect(toList())));
      case LONG_ARRAY:
        return hasBodyField(
            key.getKey(), Value.of(((List<Long>) value).stream().map(Value::of).collect(toList())));
      case DOUBLE_ARRAY:
        return hasBodyField(
            key.getKey(),
            Value.of(((List<Double>) value).stream().map(Value::of).collect(toList())));
      case VALUE:
        return hasBodyField(key.getKey(), (Value<?>) value);
    }
    return this;
  }

  /**
   * Asserts the log has exception attributes for the given {@link Throwable}. The stack trace is
   * not matched against.
   */
  @SuppressWarnings("NullAway")
  public LogRecordDataAssert hasException(Throwable exception) {
    isNotNull();

    assertThat(actual.getAttributes())
        .as("exception.type")
        .containsEntry(EXCEPTION_TYPE, exception.getClass().getCanonicalName());
    if (exception.getMessage() != null) {
      assertThat(actual.getAttributes())
          .as("exception.message")
          .containsEntry(EXCEPTION_MESSAGE, exception.getMessage());
    }

    // Exceptions used in assertions always have a different stack trace, just confirm it was
    // recorded.
    String stackTrace = actual.getAttributes().get(EXCEPTION_STACKTRACE);
    assertThat(stackTrace).as("exception.stacktrace").isNotNull();

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
