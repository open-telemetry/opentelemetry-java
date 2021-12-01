/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class LogDataBuilderTest {

  private final Resource resource = Resource.getDefault();
  private final InstrumentationLibraryInfo libraryInfo = InstrumentationLibraryInfo.empty();

  @Test
  void canSetClock() {
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(12L);
    LogDataBuilder builder = LogDataBuilder.create(resource, libraryInfo, clock);

    LogData result = builder.build();
    assertEquals(12L, result.getEpochNanos());
  }

  @Test
  void canSetSpanContext() {
    LogDataBuilder builder = LogDataBuilder.create(resource, libraryInfo);
    SpanContext spanContext = mock(SpanContext.class);
    LogData result = builder.setSpanContext(spanContext).build();
    assertSame(spanContext, result.getSpanContext());
  }

  @Test
  void setSpanContext_nullSafe() {
    LogDataBuilder builder = LogDataBuilder.create(resource, libraryInfo);
    LogData result = builder.setSpanContext(null).build();
    assertSame(SpanContext.getInvalid(), result.getSpanContext());
  }

  @Test
  void canSetSpanContextFromContext() {
    String traceId = "33333333333333333333333333333333";
    String spanId = "7777777777777777";
    SpanContext spanContext =
        SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
    Span span = Span.wrap(spanContext);

    Context context = mock(Context.class);
    when(context.get(any())).thenReturn(span);

    LogDataBuilder builder = LogDataBuilder.create(resource, libraryInfo);

    LogData result = builder.setContext(context).build();
    assertSame(spanContext, result.getSpanContext());
  }
}
