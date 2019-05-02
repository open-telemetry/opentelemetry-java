/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.opentracingshim;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.List;
import openconsensus.trace.AttributeValue;
import openconsensus.trace.Link;
import openconsensus.trace.Span.Kind;
import openconsensus.trace.Status;

@SuppressWarnings("deprecation")
final class SpanBuilderShim implements SpanBuilder {
  private final openconsensus.trace.Tracer tracer;
  private final String spanName;

  // The parent will be either a Span or a SpanContext.
  private openconsensus.trace.Span parentSpan;
  private openconsensus.trace.SpanContext parentSpanContext;
  private boolean ignoreActiveSpan;

  private final List<Link> parentLinks = new ArrayList<>();
  private final List<String> spanBuilderAttributeKeys = new ArrayList<>();
  private final List<AttributeValue> spanBuilderAttributeValues = new ArrayList<>();
  private Kind spanKind;
  private boolean error;

  public SpanBuilderShim(openconsensus.trace.Tracer tracer, String spanName) {
    this.tracer = tracer;
    this.spanName = spanName;
  }

  @Override
  public SpanBuilder asChildOf(SpanContext parent) {
    // TODO - Verify we handle a no-op SpanContext
    openconsensus.trace.SpanContext actualParent = getActualContext(parent);

    if (parentSpan == null && parentSpanContext == null) {
      parentSpanContext = actualParent;
    } else {
      parentLinks.add(Link.create(actualParent));
    }

    return this;
  }

  @Override
  public SpanBuilder asChildOf(Span parent) {
    // TODO - Verify we handle a no-op Span
    openconsensus.trace.Span actualParent = getActualSpan(parent);

    if (parentSpan == null && parentSpanContext == null) {
      parentSpan = actualParent;
    } else {
      parentLinks.add(Link.create(actualParent.getContext()));
    }

    return this;
  }

  @Override
  public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
    // TODO - Use referenceType
    openconsensus.trace.SpanContext actualContext = getActualContext(referencedContext);

    if (parentSpan == null && parentSpanContext == null) {
      parentSpanContext = actualContext;
    } else {
      parentLinks.add(Link.create(actualContext));
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

    openconsensus.trace.Span.Builder builder;
    if (ignoreActiveSpan && parentSpan == null && parentSpanContext == null) {
      builder = tracer.spanBuilderWithExplicitParent(spanName, null);
    } else if (parentSpan != null) {
      builder = tracer.spanBuilderWithExplicitParent(spanName, parentSpan);
    } else if (parentSpanContext != null) {
      builder = tracer.spanBuilderWithRemoteParent(spanName, parentSpanContext);
    } else {
      builder = tracer.spanBuilder(spanName);
    }

    if (!parentLinks.isEmpty()) {
      builder.addLinks(parentLinks);
    }

    builder.setSpanKind(spanKind);
    openconsensus.trace.Span span = builder.startSpan();

    for (int i = 0; i < this.spanBuilderAttributeKeys.size(); i++) {
      String key = this.spanBuilderAttributeKeys.get(i);
      AttributeValue value = this.spanBuilderAttributeValues.get(i);
      span.setAttribute(key, value);
    }
    if (error) {
      span.setStatus(Status.UNKNOWN);
    }

    return new SpanShim(span);
  }

  @Override
  public Span startManual() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Scope startActive(boolean finishSpanOnClose) {
    throw new UnsupportedOperationException();
  }

  static openconsensus.trace.Span getActualSpan(Span span) {
    if (!(span instanceof SpanShim)) {
      throw new IllegalArgumentException("span is not a valid SpanShim object");
    }

    return ((SpanShim) span).getSpan();
  }

  static openconsensus.trace.SpanContext getActualContext(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException("context is not a valid SpanContextShim object");
    }

    return ((SpanContextShim) context).getSpanContext();
  }
}
