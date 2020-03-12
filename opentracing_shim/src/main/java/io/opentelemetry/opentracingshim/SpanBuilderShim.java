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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.List;

final class SpanBuilderShim extends BaseShimObject implements SpanBuilder {
  private final String spanName;

  // The parent will be either a Span or a SpanContext.
  // Inherited baggage is supported only for the main parent.
  private SpanShim parentSpan;
  private SpanContextShim parentSpanContext;
  private boolean ignoreActiveSpan;

  private final List<io.opentelemetry.trace.SpanContext> parentLinks = new ArrayList<>();
  private final List<String> spanBuilderAttributeKeys = new ArrayList<>();
  private final List<AttributeValue> spanBuilderAttributeValues = new ArrayList<>();
  private Kind spanKind;
  private boolean error;

  public SpanBuilderShim(TelemetryInfo telemetryInfo, String spanName) {
    super(telemetryInfo);
    this.spanName = spanName;
  }

  @Override
  public SpanBuilder asChildOf(Span parent) {
    if (parent == null) {
      return this;
    }

    // TODO - Verify we handle a no-op Span
    SpanShim spanShim = getSpanShim(parent);

    if (parentSpan == null && parentSpanContext == null) {
      parentSpan = spanShim;
    } else {
      parentLinks.add(spanShim.getSpan().getContext());
    }

    return this;
  }

  @Override
  public SpanBuilder asChildOf(SpanContext parent) {
    return addReference(null, parent);
  }

  @Override
  public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
    if (referencedContext == null) {
      return this;
    }

    // TODO - Use referenceType
    SpanContextShim contextShim = getContextShim(referencedContext);

    if (parentSpan == null && parentSpanContext == null) {
      parentSpanContext = contextShim;
    } else {
      parentLinks.add(contextShim.getSpanContext());
    }

    return this;
  }

  @Override
  public SpanBuilder ignoreActiveSpan() {
    ignoreActiveSpan = true;
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, String value) {
    if (Tags.SPAN_KIND.getKey().equals(key)) {
      switch (value) {
        case Tags.SPAN_KIND_CLIENT:
          spanKind = Kind.CLIENT;
          break;
        case Tags.SPAN_KIND_SERVER:
          spanKind = Kind.SERVER;
          break;
        case Tags.SPAN_KIND_PRODUCER:
          spanKind = Kind.PRODUCER;
          break;
        case Tags.SPAN_KIND_CONSUMER:
          spanKind = Kind.CONSUMER;
          break;
        default:
          spanKind = Kind.INTERNAL;
          break;
      }
    } else if (Tags.ERROR.getKey().equals(key)) {
      error = Boolean.parseBoolean(value);
    } else {
      this.spanBuilderAttributeKeys.add(key);
      this.spanBuilderAttributeValues.add(AttributeValue.stringAttributeValue(value));
    }

    return this;
  }

  @Override
  public SpanBuilder withTag(String key, boolean value) {
    if (Tags.ERROR.getKey().equals(key)) {
      error = value;
    } else {
      this.spanBuilderAttributeKeys.add(key);
      this.spanBuilderAttributeValues.add(AttributeValue.booleanAttributeValue(value));
    }
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, Number value) {
    // TODO - Verify only the 'basic' types are supported/used.
    if (value instanceof Integer
        || value instanceof Long
        || value instanceof Short
        || value instanceof Byte) {
      this.spanBuilderAttributeKeys.add(key);
      this.spanBuilderAttributeValues.add(AttributeValue.longAttributeValue(value.longValue()));
    } else if (value instanceof Float || value instanceof Double) {
      this.spanBuilderAttributeKeys.add(key);
      this.spanBuilderAttributeValues.add(AttributeValue.doubleAttributeValue(value.doubleValue()));
    } else {
      throw new IllegalArgumentException("Number type not supported");
    }

    return this;
  }

  @Override
  public <T> SpanBuilder withTag(Tag<T> tag, T value) {
    if (value instanceof String) {
      this.withTag(tag.getKey(), (String) value);
    } else if (value instanceof Boolean) {
      this.withTag(tag.getKey(), (Boolean) value);
    } else if (value instanceof Number) {
      this.withTag(tag.getKey(), (Number) value);
    } else {
      this.withTag(tag.getKey(), value.toString());
    }

    return this;
  }

  @Override
  public SpanBuilder withStartTimestamp(long microseconds) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Span start() {
    io.opentelemetry.correlationcontext.CorrelationContext distContext = null;
    io.opentelemetry.trace.Span.Builder builder = tracer().spanBuilder(spanName);

    if (ignoreActiveSpan && parentSpan == null && parentSpanContext == null) {
      builder.setNoParent();
    } else if (parentSpan != null) {
      builder.setParent(parentSpan.getSpan());
      SpanContextShim contextShim = spanContextTable().get(parentSpan);
      distContext = contextShim == null ? null : contextShim.getCorrelationContext();
    } else if (parentSpanContext != null) {
      builder.setParent(parentSpanContext.getSpanContext());
      distContext = parentSpanContext.getCorrelationContext();
    }

    for (io.opentelemetry.trace.SpanContext link : parentLinks) {
      builder.addLink(link);
    }

    if (spanKind != null) {
      builder.setSpanKind(spanKind);
    }

    io.opentelemetry.trace.Span span = builder.startSpan();

    for (int i = 0; i < this.spanBuilderAttributeKeys.size(); i++) {
      String key = this.spanBuilderAttributeKeys.get(i);
      AttributeValue value = this.spanBuilderAttributeValues.get(i);
      span.setAttribute(key, value);
    }
    if (error) {
      span.setStatus(Status.UNKNOWN);
    }

    SpanShim spanShim = new SpanShim(telemetryInfo(), span);

    if (distContext != null && distContext != telemetryInfo().emptyCorrelationContext()) {
      spanContextTable().create(spanShim, distContext);
    }

    return spanShim;
  }

  private static SpanShim getSpanShim(Span span) {
    if (!(span instanceof SpanShim)) {
      throw new IllegalArgumentException("span is not a valid SpanShim object");
    }

    return (SpanShim) span;
  }

  private static SpanContextShim getContextShim(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException("context is not a valid SpanContextShim object");
    }

    return (SpanContextShim) context;
  }
}
