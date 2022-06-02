/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class LogDataBuilderTest {

  private final Resource resource = Resource.getDefault();
  private final InstrumentationScopeInfo scopeInfo = InstrumentationScopeInfo.empty();

  @Test
  void canSetClock() {
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(12L);
    LogDataBuilder builder = LogDataBuilder.create(resource, scopeInfo, clock);

    LogData result = builder.build();
    assertThat(result.getEpochNanos()).isEqualTo(12L);
  }

  @Test
  void canSetSpanContext() {
    LogDataBuilder builder = LogDataBuilder.create(resource, scopeInfo);
    SpanContext spanContext = mock(SpanContext.class);
    LogData result = builder.setSpanContext(spanContext).build();
    assertThat(result.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void setSpanContext_nullSafe() {
    LogDataBuilder builder = LogDataBuilder.create(resource, scopeInfo);
    LogData result = builder.setSpanContext(null).build();
    assertThat(result.getSpanContext()).isSameAs(SpanContext.getInvalid());
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

    LogDataBuilder builder = LogDataBuilder.create(resource, scopeInfo);

    LogData result = builder.setContext(context).build();
    assertThat(result.getSpanContext()).isSameAs(spanContext);
  }
}
