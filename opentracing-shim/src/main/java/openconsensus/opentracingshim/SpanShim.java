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

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tag;
import java.util.HashMap;
import java.util.Map;
import openconsensus.tags.TagMap;
import openconsensus.trace.AttributeValue;
import openconsensus.trace.Status;

final class SpanShim implements Span {
  private static final String DEFAULT_EVENT_NAME = "log";

  private final openconsensus.trace.Span span;
  private final SpanContextShim contextShim;

  public SpanShim(TracerShim tracerShim, openconsensus.trace.Span span) {
    this(tracerShim, span, tracerShim.tagger().empty());
  }

  public SpanShim(TracerShim tracerShim, openconsensus.trace.Span span, TagMap tagMap) {
    this.span = span;
    this.contextShim = new SpanContextShim(tracerShim, span.getContext(), tagMap);
  }

  openconsensus.trace.Span getSpan() {
    return span;
  }

  @Override
  public SpanContext context() {
    return contextShim;
  }

  @Override
  public Span setTag(String key, String value) {
    if ("span.kind".equals(key)) {
      // TODO: confirm we can safely ignore span.kind after Span was created
      // https://github.com/bogdandrutu/openconsensus/issues/42
    } else if ("error".equals(key) && value.equalsIgnoreCase("true")) {
      this.span.setStatus(Status.UNKNOWN);
    } else {
      span.setAttribute(key, value);
    }

    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    if ("error".equals(key) && value) {
      this.span.setStatus(Status.UNKNOWN);
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
    // TODO
    return this;
  }

  @Override
  public String getBaggageItem(String key) {
    return contextShim.getBaggageItem(key);
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
    // TODO: Take finishMicros into account
    span.end();
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
