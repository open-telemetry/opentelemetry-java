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

package io.opentelemetry.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultSpan}. */
class DefaultSpanTest {

  @Test
  void hasInvalidContextAndDefaultSpanOptions() {
    SpanContext context = DefaultSpan.getInvalid().getContext();
    assertThat(context.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(context.getTraceState()).isEqualTo(TraceState.getDefault());
  }

  @Test
  void doNotCrash() {
    Span span = DefaultSpan.getInvalid();
    span.setAttribute(
        "MyStringAttributeKey",
        AttributeValue.Factory.stringAttributeValue("MyStringAttributeValue"));
    span.setAttribute("MyBooleanAttributeKey", AttributeValue.Factory.booleanAttributeValue(true));
    span.setAttribute("MyLongAttributeKey", AttributeValue.Factory.longAttributeValue(123));
    span.setAttribute("NullString", (String) null);
    span.setAttribute("EmptyString", "");
    span.setAttribute(
        "NullArrayString", AttributeValue.Factory.arrayAttributeValue((String[]) null));
    span.setAttribute(
        "NullArrayBoolean", AttributeValue.Factory.arrayAttributeValue((Boolean[]) null));
    span.setAttribute("NullArrayLong", AttributeValue.Factory.arrayAttributeValue((Long[]) null));
    span.setAttribute(
        "NullArrayDouble", AttributeValue.Factory.arrayAttributeValue((Double[]) null));
    span.setAttribute(null, (String) null);
    span.addEvent("event");
    span.addEvent("event", 0);
    span.addEvent(
        "event",
        Attributes.Factory.of(
            "MyBooleanAttributeKey", AttributeValue.Factory.booleanAttributeValue(true)));
    span.addEvent(
        "event",
        Attributes.Factory.of(
            "MyBooleanAttributeKey", AttributeValue.Factory.booleanAttributeValue(true)),
        0);
    span.addEvent(new TestEvent());
    span.addEvent(new TestEvent(), 0);
    span.addEvent((Event) null);
    span.setStatus(Status.OK);
    span.recordException(new IllegalStateException());
    span.end();
    span.end(EndSpanOptions.getDefault());
    span.end(null);
  }

  @Test
  void defaultSpan_ToString() {
    Span span = DefaultSpan.getInvalid();
    assertThat(span.toString()).isEqualTo("DefaultSpan");
  }

  static final class TestEvent implements Event {
    @Override
    public String getName() {
      return "name";
    }

    @Override
    public Attributes getAttributes() {
      return Attributes.Factory.empty();
    }
  }
}
