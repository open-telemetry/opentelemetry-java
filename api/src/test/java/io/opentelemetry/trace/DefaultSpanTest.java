/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    span.setAttribute("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true));
    span.setAttribute("MyLongAttributeKey", AttributeValue.longAttributeValue(123));
    span.setAttribute("NullString", (String) null);
    span.setAttribute("EmptyString", "");
    span.setAttribute("NullArrayString", AttributeValue.arrayAttributeValue((String[]) null));
    span.setAttribute("NullArrayBoolean", AttributeValue.arrayAttributeValue((Boolean[]) null));
    span.setAttribute("NullArrayLong", AttributeValue.arrayAttributeValue((Long[]) null));
    span.setAttribute("NullArrayDouble", AttributeValue.arrayAttributeValue((Double[]) null));
    span.setAttribute(null, (String) null);
    span.addEvent("event");
    span.addEvent("event", 0);
    span.addEvent(
        "event",
        Attributes.of("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true)));
    span.addEvent(
        "event",
        Attributes.of("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true)),
        0);
    span.addEvent(new TestEvent());
    span.addEvent(new TestEvent(), 0);
    span.addEvent((Event) null);
    span.setStatus(Status.OK);
    span.recordException(new IllegalStateException());
    span.recordException(new IllegalStateException(), Attributes.empty());
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
      return Attributes.empty();
    }
  }
}
