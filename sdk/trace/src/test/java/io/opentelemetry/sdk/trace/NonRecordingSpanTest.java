/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.trace.internal.metrics.SpanMetrics;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NonRecordingSpanTest {

  private static final SpanContext DUMMY_CONTEXT =
      SpanContext.create(
          "a1a2a3a4a5a6a7a8b1b2b3b4b5b6b7b8",
          "c1c2c3c4c5c6c7c8",
          TraceFlags.getDefault(),
          TraceState.getDefault());

  @Mock SpanMetrics.Recording mockRecording;

  @Test
  void notRecording() {
    assertThat(new NonRecordingSpan(DUMMY_CONTEXT, mockRecording).isRecording()).isFalse();
  }

  @Test
  void recordingFinishedOnEnd() {
    Span span = new NonRecordingSpan(DUMMY_CONTEXT, mockRecording);
    span.end();
    verify(mockRecording, times(1)).recordSpanEnd();
    span.end(0, TimeUnit.NANOSECONDS);
    verify(mockRecording, times(2)).recordSpanEnd();
    span.end(Instant.EPOCH);
    verify(mockRecording, times(3)).recordSpanEnd();
  }

  @Test
  void doNotCrash() {
    Span span = new NonRecordingSpan(DUMMY_CONTEXT, mockRecording);
    span.setAttribute(stringKey("MyStringAttributeKey"), "MyStringAttributeValue");
    span.setAttribute(booleanKey("MyBooleanAttributeKey"), true);
    span.setAttribute(longKey("MyLongAttributeKey"), 123L);
    span.setAttribute(longKey("MyLongAttributeKey"), 123);
    span.setAttribute("NullString", null);
    span.setAttribute("EmptyString", "");
    span.setAttribute("long", 1);
    span.setAttribute("double", 1.0);
    span.setAttribute("boolean", true);
    span.setAttribute(stringArrayKey("NullArrayString"), null);
    span.setAttribute(booleanArrayKey("NullArrayBoolean"), null);
    span.setAttribute(longArrayKey("NullArrayLong"), null);
    span.setAttribute(doubleArrayKey("NullArrayDouble"), null);
    span.setAttribute((String) null, null);
    span.setAllAttributes(null);
    span.setAllAttributes(Attributes.empty());
    span.setAllAttributes(
        Attributes.of(stringKey("MyStringAttributeKey"), "MyStringAttributeValue"));
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
    span.updateName("name");
  }

  @Test
  void verifyToString() {
    Span span = new NonRecordingSpan(DUMMY_CONTEXT, mockRecording);
    assertThat(span.toString())
        .isEqualTo(
            "NonRecordingSpan{ImmutableSpanContext{traceId=a1a2a3a4a5a6a7a8b1b2b3b4b5b6b7b8, "
                + "spanId=c1c2c3c4c5c6c7c8, traceFlags=00, "
                + "traceState=ArrayBasedTraceState{entries=[]}, remote=false, valid=true}}");
  }
}
