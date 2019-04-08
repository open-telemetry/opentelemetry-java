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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import openconsensus.trace.Span.Kind;
import openconsensus.trace.data.AttributeValue;

@SuppressWarnings("deprecation")
final class SpanBuilderShim implements SpanBuilder {
  openconsensus.trace.Tracer tracer;
  openconsensus.trace.SpanBuilder builder;

  // TODO: should it be any of concurrent maps?
  Map<String, AttributeValue> spanBuilderAttributes = new HashMap<String, AttributeValue>();

  public SpanBuilderShim(
      openconsensus.trace.Tracer tracer, openconsensus.trace.SpanBuilder builder) {
    this.tracer = tracer;
    this.builder = builder;
  }

  @Override
  public SpanBuilder asChildOf(SpanContext parent) {
    // TODO - Verify we handle a no-op SpanContext
    return this;
  }

  @Override
  public SpanBuilder asChildOf(Span parent) {
    // TODO - Verify we handle a no-op Span
    return this;
  }

  @Override
  public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder ignoreActiveSpan() {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, String value) {
    if ("span.kind".equals(key)) {
      switch (value) {
        case "CLIENT":
          this.builder.setSpanKind(Kind.CLIENT);
          break;
        case "SERVER":
          this.builder.setSpanKind(Kind.SERVER);
          break;
        case "PRODUCER":
          this.builder.setSpanKind(Kind.PRODUCER);
          break;
        case "CONSUMER":
          this.builder.setSpanKind(Kind.CONSUMER);
          break;
        default:
          this.builder.setSpanKind(Kind.UNDEFINED);
          break;
      }
    } else if ("error".equals(key)) {
      // TODO: confirm we can ignore it
      // https://github.com/bogdandrutu/openconsensus/issues/41
    } else {
      this.spanBuilderAttributes.put(key, AttributeValue.stringAttributeValue(value));
    }

    return this;
  }

  @Override
  public SpanBuilder withTag(String key, boolean value) {
    if ("error".equals(key) && (value == true)) {
      // TODO: confirm we can ignore it
      // https://github.com/bogdandrutu/openconsensus/issues/41
    } else {
      this.spanBuilderAttributes.put(key, AttributeValue.booleanAttributeValue(value));
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
      this.spanBuilderAttributes.put(key, AttributeValue.longAttributeValue(value.longValue()));
    } else if (value instanceof Float || value instanceof Double) {
      this.spanBuilderAttributes.put(key, AttributeValue.doubleAttributeValue(value.doubleValue()));
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
      this.withTag(tag.getKey(), ((Boolean) value).booleanValue());
    } else if (value instanceof Number) {
      this.withTag(tag.getKey(), (Number) value);
    } else {
      this.withTag(tag.getKey(), value.toString());
    }

    return this;
  }

  @Override
  public SpanBuilder withStartTimestamp(long microseconds) {
    this.spanBuilderAttributes.put(
        "ot.start_timestamp", AttributeValue.longAttributeValue(microseconds));
    return this;
  }

  @Override
  public Span start() {
    openconsensus.trace.Span span = builder.startSpan();

    Iterator<Entry<String, AttributeValue>> entries =
        this.spanBuilderAttributes.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry<String, AttributeValue> entry = entries.next();
      span.setAttribute(entry.getKey(), entry.getValue());
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
}
