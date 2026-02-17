/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.ImplicitContextKeyed;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/*
 * SpanContextShim is directly stored in the SpanShim for simplicity
 * and performance reasons, as opposed to keeping a global map
 * link OTel's Span and OT Span/SpanContext.
 */
final class SpanShim implements Span, ImplicitContextKeyed {

  private static final AttributeKey<String> EXCEPTION_TYPE =
      AttributeKey.stringKey("exception.type");
  private static final AttributeKey<String> EXCEPTION_MESSAGE =
      AttributeKey.stringKey("exception.message");
  private static final AttributeKey<String> EXCEPTION_STACKTRACE =
      AttributeKey.stringKey("exception.stacktrace");
  private static final String EXCEPTION_EVENT_NAME = "exception";

  private static final String DEFAULT_EVENT_NAME = "log";
  private static final String ERROR = "error";
  private static final ContextKey<SpanShim> SPAN_SHIM_KEY =
      ContextKey.named("opentracing-shim-key");

  private final io.opentelemetry.api.trace.Span span;
  private final Object spanContextShimLock;
  private volatile SpanContextShim spanContextShim;

  SpanShim(io.opentelemetry.api.trace.Span span) {
    this(span, Baggage.empty());
  }

  SpanShim(io.opentelemetry.api.trace.Span span, Baggage baggage) {
    this.span = span;
    this.spanContextShimLock = new Object();
    this.spanContextShim = new SpanContextShim(span.getSpanContext(), baggage);
  }

  io.opentelemetry.api.trace.Span getSpan() {
    return span;
  }

  Baggage getBaggage() {
    return spanContextShim.getBaggage();
  }

  @Nullable
  public static SpanShim current() {
    return Context.current().get(SPAN_SHIM_KEY);
  }

  @Override
  public Context storeInContext(Context context) {
    context = context.with(SPAN_SHIM_KEY, this).with(span).with(spanContextShim.getBaggage());

    return context;
  }

  @Override
  public SpanContext context() {
    return spanContextShim;
  }

  @Override
  public Span setTag(String key, String value) {
    if (Tags.ERROR.getKey().equals(key)) {
      StatusCode canonicalCode = Boolean.parseBoolean(value) ? StatusCode.ERROR : StatusCode.OK;
      span.setStatus(canonicalCode);
    } else {
      span.setAttribute(key, value);
    }

    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    if (Tags.ERROR.getKey().equals(key)) {
      StatusCode canonicalCode = value ? StatusCode.ERROR : StatusCode.OK;
      span.setStatus(canonicalCode);
    } else {
      span.setAttribute(key, value);
    }

    return this;
  }

  @Override
  public Span setTag(String key, Number value) {
    if (value == null) {
      return this;
    }

    if (value instanceof Integer
        || value instanceof Long
        || value instanceof Short
        || value instanceof Byte) {
      span.setAttribute(key, value.longValue());
    } else if (value instanceof Float || value instanceof Double) {
      span.setAttribute(key, value.doubleValue());
    } else {
      span.setAttribute(key, value.toString());
    }

    return this;
  }

  @Override
  public <T> Span setTag(Tag<T> tag, T value) {
    if (tag == null) {
      return this;
    }
    tag.set(this, value);
    return this;
  }

  @Override
  public Span log(Map<String, ?> fields) {
    logInternal(-1, fields);
    return this;
  }

  @Override
  public Span log(long timestampMicroseconds, Map<String, ?> fields) {
    logInternal(timestampMicroseconds, fields);
    return this;
  }

  @Override
  public Span log(String event) {
    span.addEvent(event);
    return this;
  }

  @Override
  public Span log(long timestampMicroseconds, String event) {
    span.addEvent(event, timestampMicroseconds, TimeUnit.MICROSECONDS);
    return this;
  }

  @Override
  public Span setBaggageItem(String key, String value) {
    // TagKey nor TagValue can be created with null values.
    if (key == null || value == null) {
      return this;
    }

    synchronized (spanContextShimLock) {
      spanContextShim = spanContextShim.newWithKeyValue(key, value);
    }

    return this;
  }

  @Nullable
  @Override
  public String getBaggageItem(String key) {
    if (key == null) {
      return null;
    }

    return spanContextShim.getBaggageItem(key);
  }

  @Override
  public Span setOperationName(String operationName) {
    span.updateName(operationName);
    return this;
  }

  @Override
  public void finish() {
    span.end();
  }

  @Override
  public void finish(long finishMicros) {
    span.end(finishMicros, TimeUnit.MICROSECONDS);
  }

  private void logInternal(long timestampMicroseconds, Map<String, ?> fields) {
    String name = getEventNameFromFields(fields);
    Throwable throwable = null;
    boolean isError = false;
    if (name.equals(ERROR)) {
      throwable = findThrowable(fields);
      isError = true;
      if (throwable == null) {
        name = EXCEPTION_EVENT_NAME;
      }
    }
    Attributes attributes = convertToAttributes(fields, isError, throwable != null);

    if (throwable != null) {
      // timestamp is not recorded if specified
      span.recordException(throwable, attributes);
    } else if (timestampMicroseconds != -1) {
      span.addEvent(name, attributes, timestampMicroseconds, TimeUnit.MICROSECONDS);
    } else {
      span.addEvent(name, attributes);
    }
  }

  private static String getEventNameFromFields(Map<String, ?> fields) {
    Object eventValue = fields == null ? null : fields.get(Fields.EVENT);
    if (eventValue != null) {
      return eventValue.toString();
    }

    return DEFAULT_EVENT_NAME;
  }

  private static Attributes convertToAttributes(
      Map<String, ?> fields, boolean isError, boolean isRecordingException) {
    AttributesBuilder attributesBuilder = Attributes.builder();

    for (Map.Entry<String, ?> entry : fields.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      // TODO - verify null values are NOT allowed.
      if (value == null) {
        continue;
      }

      if (value instanceof Byte
          || value instanceof Short
          || value instanceof Integer
          || value instanceof Long) {
        attributesBuilder.put(longKey(key), ((Number) value).longValue());
      } else if (value instanceof Float || value instanceof Double) {
        attributesBuilder.put(doubleKey(key), ((Number) value).doubleValue());
      } else if (value instanceof Boolean) {
        attributesBuilder.put(booleanKey(key), (Boolean) value);
      } else {
        AttributeKey<String> attributeKey = null;
        if (isError && !isRecordingException) {
          if (key.equals(Fields.ERROR_KIND)) {
            attributeKey = EXCEPTION_TYPE;
          } else if (key.equals(Fields.MESSAGE)) {
            attributeKey = EXCEPTION_MESSAGE;
          } else if (key.equals(Fields.STACK)) {
            attributeKey = EXCEPTION_STACKTRACE;
          }
        }
        if (isRecordingException && key.equals(Fields.ERROR_OBJECT)) {
          // Already recorded as the exception itself so don't add as attribute.
          continue;
        }

        if (attributeKey == null) {
          attributeKey = stringKey(key);
        }
        attributesBuilder.put(attributeKey, value.toString());
      }
    }

    return attributesBuilder.build();
  }

  @Nullable
  private static Throwable findThrowable(Map<String, ?> fields) {
    Object value = fields.get(Fields.ERROR_OBJECT);
    if (value instanceof Throwable) {
      return (Throwable) value;
    }
    return null;
  }
}
