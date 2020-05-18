/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.trace;

import static io.opentelemetry.common.AttributeValue.arrayAttributeValue;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;
import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import io.opentelemetry.common.Attribute;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.AttributeKey.BooleanArrayValuedKey;
import io.opentelemetry.common.AttributeKey.BooleanValuedKey;
import io.opentelemetry.common.AttributeKey.DoubleArrayValuedKey;
import io.opentelemetry.common.AttributeKey.DoubleValuedKey;
import io.opentelemetry.common.AttributeKey.LongArrayValuedKey;
import io.opentelemetry.common.AttributeKey.LongValuedKey;
import io.opentelemetry.common.AttributeKey.StringArrayValuedKey;
import io.opentelemetry.common.AttributeKey.StringValuedKey;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.trace.Span.Kind;
import java.util.HashMap;
import java.util.Map;

public class SpanEx {

  private final Span span;

  public SpanEx(Span span) {
    this.span = span;
  }

  Span getSpan() {
    return span;
  }

  public void setStatus(Status status) {
    span.setStatus(status);
  }

  public void updateName(String name) {
    span.updateName(name);
  }

  public SpanContext getContext() {
    return span.getContext();
  }

  public boolean isRecording() {
    return span.isRecording();
  }

  public void end() {
    span.end();
  }

  public void end(EndSpanOptions endOptions) {
    span.end(endOptions);
  }

  public void setAttribute(StringValuedKey key, String value) {
    span.setAttribute(key.key(), AttributeValue.stringAttributeValue(value));
  }

  public void setAttribute(LongValuedKey key, long value) {
    span.setAttribute(key.key(), AttributeValue.longAttributeValue(value));
  }

  public void setAttribute(DoubleValuedKey key, double value) {
    span.setAttribute(key.key(), AttributeValue.doubleAttributeValue(value));
  }

  public void setAttribute(BooleanValuedKey key, boolean value) {
    span.setAttribute(key.key(), AttributeValue.booleanAttributeValue(value));
  }

  public void setAttribute(StringArrayValuedKey key, String... value) {
    span.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
  }

  public void setAttribute(LongArrayValuedKey key, Long... value) {
    span.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
  }

  public void setAttribute(DoubleArrayValuedKey key, Double... value) {
    span.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
  }

  public void setAttribute(BooleanArrayValuedKey key, Boolean... value) {
    span.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
  }

  public static Builder newBuilder(Tracer tracer, String spanName) {
    return new Builder(tracer, spanName);
  }

  public static class Builder {

    private final Span.Builder builder;

    public Builder(Tracer tracer, String spanName) {
      builder = tracer.spanBuilder(spanName);
    }

    public SpanEx.Builder setParent(SpanEx parent) {
      builder.setParent(parent.span);
      return this;
    }

    public SpanEx.Builder setParent(SpanContext remoteParent) {
      builder.setParent(remoteParent);
      return this;
    }

    public SpanEx.Builder setNoParent() {
      builder.setNoParent();
      return this;
    }

    // option 1... an interface that encapsulates the attribute nature
    public SpanEx.Builder addLink(SpanContext context, Attributes attributes) {
      builder.addLink(context, makeAttributeMap(attributes));
      return this;
    }

    // option 2... a type-safe key-value pair with varargs parameters
    public SpanEx.Builder addLink(SpanContext context, Attribute... attributes) {
      builder.addLink(context, makeAttributeMap(attributes));
      return this;
    }

    // option 3... provide a concrete Link class.
    public SpanEx.Builder addLink(LinkEx link) {
      return addLink(link.getSpanContext(), link.getAttributes());
    }

    public SpanEx.Builder setSpanKind(Kind spanKind) {
      builder.setSpanKind(spanKind);
      return this;
    }

    public SpanEx.Builder setStartTimestamp(long startTimestamp) {
      builder.setStartTimestamp(startTimestamp);
      return this;
    }

    public SpanEx startSpan() {
      return new SpanEx(builder.startSpan());
    }

    // option 1... provide a single attribute
    public SpanEx.Builder setAttribute(Attribute attribute) {
      builder.setAttribute(attribute.key().key(), makeValue(attribute));
      return this;
    }

    // option 2... a type-safe key-value pair with varargs parameters

    /** doc me. */
    public SpanEx.Builder setAttribute(Attribute... attributes) {
      for (Attribute attribute : attributes) {
        builder.setAttribute(attribute.key().key(), makeValue(attribute));
      }
      return this;
    }

    // option 3... accept a bunch of attributes at once

    /** doc me. */
    public SpanEx.Builder setAttribute(Attributes attributes) {
      for (AttributeKey key : attributes.getKeys()) {
        builder.setAttribute(key.key(), makeValue(attributes, key));
      }
      return this;
    }

    public SpanEx.Builder setAttribute(StringValuedKey key, String value) {
      builder.setAttribute(key.key(), AttributeValue.stringAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(LongValuedKey key, long value) {
      builder.setAttribute(key.key(), AttributeValue.longAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(DoubleValuedKey key, double value) {
      builder.setAttribute(key.key(), AttributeValue.doubleAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(BooleanValuedKey key, boolean value) {
      builder.setAttribute(key.key(), AttributeValue.booleanAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(StringArrayValuedKey key, String... value) {
      builder.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(LongArrayValuedKey key, Long... value) {
      builder.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(DoubleArrayValuedKey key, Double... value) {
      builder.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
      return this;
    }

    public SpanEx.Builder setAttribute(BooleanArrayValuedKey key, Boolean... value) {
      builder.setAttribute(key.key(), AttributeValue.arrayAttributeValue(value));
      return this;
    }
  }

  private static Map<String, AttributeValue> makeAttributeMap(Attribute[] attributes) {
    Map<String, AttributeValue> result = new HashMap<>();
    for (Attribute attribute : attributes) {
      result.put(attribute.key().key(), makeValue(attribute));
    }
    return result;
  }

  private static Map<String, AttributeValue> makeAttributeMap(Attributes attributes) {
    Map<String, AttributeValue> result = new HashMap<>();
    for (AttributeKey key : attributes.getKeys()) {
      result.put(key.key(), makeValue(attributes, key));
    }
    return result;
  }

  private static AttributeValue makeValue(Attribute attribute) {
    AttributeKey key = attribute.key();
    switch (key.getType()) {
      case BOOLEAN:
        return booleanAttributeValue(attribute.getBooleanValue());
      case LONG:
        return longAttributeValue(attribute.getLongValue());
      case DOUBLE:
        return doubleAttributeValue(attribute.getDoubleValue());
      case STRING:
        return stringAttributeValue(attribute.getStringValue());
      case STRING_ARRAY:
        return arrayAttributeValue(attribute.getStringArrayValue().toArray(new String[0]));
      case BOOLEAN_ARRAY:
        return arrayAttributeValue(attribute.getBooleanArrayValue().toArray(new Boolean[0]));
      case LONG_ARRAY:
        return arrayAttributeValue(attribute.getLongArrayValue().toArray(new Long[0]));
      case DOUBLE_ARRAY:
        return arrayAttributeValue(attribute.getDoubleArrayValue().toArray(new Double[0]));
    }
    throw new IllegalStateException("Unknown type: " + key.getType());
  }

  private static AttributeValue makeValue(Attributes attributes, AttributeKey key) {
    switch (key.getType()) {
      case BOOLEAN:
        return booleanAttributeValue(attributes.getValue((BooleanValuedKey) key));
      case LONG:
        return longAttributeValue(attributes.getValue((LongValuedKey) key));
      case DOUBLE:
        return doubleAttributeValue(attributes.getValue((DoubleValuedKey) key));
      case STRING:
        return stringAttributeValue(attributes.getValue((StringValuedKey) key));
      case STRING_ARRAY:
        return arrayAttributeValue(
            attributes.getValue((StringArrayValuedKey) key).toArray(new String[0]));
      case BOOLEAN_ARRAY:
        return arrayAttributeValue(
            attributes.getValue((BooleanArrayValuedKey) key).toArray(new Boolean[0]));
      case LONG_ARRAY:
        return arrayAttributeValue(
            attributes.getValue((LongArrayValuedKey) key).toArray(new Long[0]));
      case DOUBLE_ARRAY:
        return arrayAttributeValue(
            attributes.getValue((DoubleArrayValuedKey) key).toArray(new Double[0]));
    }
    throw new IllegalStateException("Unknown type: " + key.getType());
  }
}
