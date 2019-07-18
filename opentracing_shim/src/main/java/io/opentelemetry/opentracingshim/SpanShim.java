/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.EmptyDistributedContext;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Status;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.HashMap;
import java.util.Map;

// Should we also store TelemetryInfo?
final class SpanShim implements Span {
  private static final String DEFAULT_EVENT_NAME = "log";

  private final io.opentelemetry.trace.Span span;
  private SpanContextShim contextShim;

  public SpanShim(TelemetryInfo telemetryInfo, io.opentelemetry.trace.Span span) {
    this(telemetryInfo, span, EmptyDistributedContext.getInstance());
  }

  public SpanShim(
      TelemetryInfo telemetryInfo,
      io.opentelemetry.trace.Span span,
      DistributedContext distContext) {
    this.span = span;
    this.contextShim = new SpanContextShim(telemetryInfo, span.getContext(), distContext);
  }

  io.opentelemetry.trace.Span getSpan() {
    return span;
  }

  @Override
  public SpanContext context() {
    synchronized (this) {
      return contextShim;
    }
  }

  @Override
  public Span setTag(String key, String value) {
    if (Tags.SPAN_KIND.getKey().equals(key)) {
      // TODO: confirm we can safely ignore span.kind after Span was created
      // https://github.com/bogdandrutu/opentelemetry/issues/42
    } else if (Tags.ERROR.getKey().equals(key)) {
      Status status = Boolean.parseBoolean(value) ? Status.UNKNOWN : Status.OK;
      span.setStatus(status);
    } else {
      span.setAttribute(key, value);
    }

    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    if (Tags.ERROR.getKey().equals(key)) {
      Status status = value ? Status.UNKNOWN : Status.OK;
      span.setStatus(status);
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

    synchronized (this) {
      contextShim = contextShim.newWithKeyValue(key, value);
    }

    return this;
  }

  @Override
  public String getBaggageItem(String key) {
    synchronized (this) {
      return contextShim.getBaggageItem(key);
    }
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

  static Map<String, AttributeValue> convertToAttributes(Map<String, ?> fields) {
    Map<String, AttributeValue> attrMap = new HashMap<>();

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
        attrMap.put(key, AttributeValue.longAttributeValue(((Number) value).longValue()));
      } else if (value instanceof Float || value instanceof Double) {
        attrMap.put(key, AttributeValue.doubleAttributeValue(((Number) value).doubleValue()));
      } else if (value instanceof Boolean) {
        attrMap.put(key, AttributeValue.booleanAttributeValue((Boolean) value));
      } else {
        attrMap.put(key, AttributeValue.stringAttributeValue(value.toString()));
      }
    }

    return attrMap;
  }
}
