/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.Map;
import javax.annotation.Nullable;

/*
 * SpanContextShim is not directly stored in the SpanShim,
 * as its changes need to be visible in all threads at *any* moment.
 * By default, the related SpanContextShim will not be created
 * in order to avoid overhead (which would require taking a write lock
 * at creation time).
 *
 * Calling context() or setBaggageItem() will effectively force the creation
 * of SpanContextShim object if none existed yet.
 */
final class SpanShim extends BaseShimObject implements Span {
  private static final String DEFAULT_EVENT_NAME = "log";

  private final io.opentelemetry.api.trace.Span span;

  public SpanShim(TelemetryInfo telemetryInfo, io.opentelemetry.api.trace.Span span) {
    super(telemetryInfo);
    this.span = span;
  }

  io.opentelemetry.api.trace.Span getSpan() {
    return span;
  }

  @Override
  public SpanContext context() {
    /* Read the value using the read lock first. */
    SpanContextShim contextShim = spanContextTable().get(this);

    /* Switch to the write lock *only* for the relatively exceptional case
     * of no context being created.
     * (as we cannot upgrade read->write lock sadly).*/
    if (contextShim == null) {
      contextShim = spanContextTable().create(this);
    }

    return contextShim;
  }

  @Override
  public Span setTag(String key, String value) {
    if (Tags.SPAN_KIND.getKey().equals(key)) {
      // TODO: confirm we can safely ignore span.kind after Span was created
      // https://github.com/bogdandrutu/opentelemetry/issues/42
    } else if (Tags.ERROR.getKey().equals(key)) {
      StatusCode canonicalCode = Boolean.parseBoolean(value) ? StatusCode.ERROR : StatusCode.UNSET;
      span.setStatus(canonicalCode);
    } else {
      span.setAttribute(key, value);
    }

    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    if (Tags.ERROR.getKey().equals(key)) {
      StatusCode canonicalCode = value ? StatusCode.ERROR : StatusCode.UNSET;
      span.setStatus(canonicalCode);
    } else {
      span.setAttribute(key, value);
    }

    return this;
  }

  @Override
  public Span setTag(String key, Number value) {
    // TODO - Verify only the 'basic' types are supported/used.
    if (value instanceof Integer
        || value instanceof Long
        || value instanceof Short
        || value instanceof Byte) {
      span.setAttribute(key, value.longValue());
    } else if (value instanceof Float || value instanceof Double) {
      span.setAttribute(key, value.doubleValue());
    } else {
      throw new IllegalArgumentException("Number type not supported");
    }

    return this;
  }

  @Override
  public <T> Span setTag(Tag<T> tag, T value) {
    tag.set(this, value);
    return this;
  }

  @Override
  public Span log(Map<String, ?> fields) {
    span.addEvent(getEventNameFromFields(fields), convertToAttributes(fields));
    return this;
  }

  @Override
  public Span log(long timestampMicroseconds, Map<String, ?> fields) {
    span.addEvent(getEventNameFromFields(fields), convertToAttributes(fields));
    return this;
  }

  @Override
  public Span log(String event) {
    span.addEvent(event);
    return this;
  }

  @Override
  public Span log(long timestampMicroseconds, String event) {
    span.addEvent(event);
    return this;
  }

  @Override
  public Span setBaggageItem(String key, String value) {
    // TagKey nor TagValue can be created with null values.
    if (key == null || value == null) {
      return this;
    }

    spanContextTable().setBaggageItem(this, key, value);

    return this;
  }

  @Nullable
  @Override
  public String getBaggageItem(String key) {
    if (key == null) {
      return null;
    }

    return spanContextTable().getBaggageItem(this, key);
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
    throw new UnsupportedOperationException();
  }

  static String getEventNameFromFields(Map<String, ?> fields) {
    Object eventValue = fields == null ? null : fields.get(Fields.EVENT);
    if (eventValue != null) {
      return eventValue.toString();
    }

    return DEFAULT_EVENT_NAME;
  }

  static Attributes convertToAttributes(Map<String, ?> fields) {
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
        attributesBuilder.put(stringKey(key), value.toString());
      }
    }

    return attributesBuilder.build();
  }
}
