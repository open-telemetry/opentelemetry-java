/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class PropagatedSpanTest {

  @Test
  void hasInvalidContextAndDefaultSpanOptions() {
    SpanContext context = Span.getInvalid().getSpanContext();
    assertThat(context.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(context.getTraceState()).isEqualTo(TraceState.getDefault());
  }

  @Test
  void doNotCrash() {
    Span span = Span.getInvalid();
    span.setAttribute(stringKey("MyStringAttributeKey"), "MyStringAttributeValue");
    span.setAttribute(booleanKey("MyBooleanAttributeKey"), true);
    span.setAttribute(longKey("MyLongAttributeKey"), 123L);
    span.setAttribute(longKey("MyLongAttributeKey"), 123);
    span.setAttribute("NullString", null);
    span.setAttribute("EmptyString", "");
    span.setAttribute(stringArrayKey("NullArrayString"), null);
    span.setAttribute(booleanArrayKey("NullArrayBoolean"), null);
    span.setAttribute(longArrayKey("NullArrayLong"), null);
    span.setAttribute(doubleArrayKey("NullArrayDouble"), null);
    span.setAttribute((String) null, null);
    span.addEvent("event");
    span.addEvent("event", 0, TimeUnit.NANOSECONDS);
    span.addEvent("event", Instant.EPOCH);
    span.addEvent("event", Attributes.of(booleanKey("MyBooleanAttributeKey"), true));
    span.addEvent(
        "event", Attributes.of(booleanKey("MyBooleanAttributeKey"), true), 0, TimeUnit.NANOSECONDS);
    span.setStatus(StatusCode.OK);
    span.setStatus(StatusCode.OK, "null");
    span.recordException(new IllegalStateException());
    span.recordException(new IllegalStateException(), Attributes.empty());
    span.end();
    span.end(0, TimeUnit.NANOSECONDS);
    span.end(Instant.EPOCH);
  }

  @Test
  void defaultSpan_ToString() {
    Span span = Span.getInvalid();
    assertThat(span.toString()).isEqualTo("DefaultSpan");
  }
}
