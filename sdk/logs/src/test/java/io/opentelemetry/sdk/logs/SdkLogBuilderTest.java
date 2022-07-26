/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkLogBuilderTest {

  private static final Resource RESOURCE = Resource.empty();
  private static final InstrumentationScopeInfo SCOPE_INFO = InstrumentationScopeInfo.empty();

  @Mock LogEmitterSharedState logEmitterSharedState;

  private final AtomicReference<LogData> emittedLog = new AtomicReference<>();
  private SdkLogBuilder builder;

  @BeforeEach
  void setup() {
    when(logEmitterSharedState.getLogLimits()).thenReturn(LogLimits.getDefault());
    when(logEmitterSharedState.getLogProcessor()).thenReturn(emittedLog::set);
    when(logEmitterSharedState.getResource()).thenReturn(RESOURCE);
    when(logEmitterSharedState.getClock()).thenReturn(Clock.getDefault());

    builder = new SdkLogBuilder(logEmitterSharedState, SCOPE_INFO);
  }

  @Test
  void emit_AllFields() {
    Instant now = Instant.now();
    String bodyStr = "body";
    String sevText = "sevText";
    Severity severity = Severity.DEBUG3;
    Attributes attrs = Attributes.empty();
    SpanContext spanContext =
        SpanContext.create(
            "33333333333333333333333333333333",
            "7777777777777777",
            TraceFlags.getSampled(),
            TraceState.getDefault());

    builder.setBody(bodyStr);
    builder.setEpoch(123, TimeUnit.SECONDS);
    builder.setEpoch(now);
    builder.setAttributes(attrs);
    builder.setContext(Span.wrap(spanContext).storeInContext(Context.root()));
    builder.setSeverity(severity);
    builder.setSeverityText(sevText);
    builder.emit();
    assertThat(emittedLog.get())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(SCOPE_INFO)
        .hasBody(bodyStr)
        .hasEpochNanos(TimeUnit.SECONDS.toNanos(now.getEpochSecond()) + now.getNano())
        .hasAttributes(attrs)
        .hasSpanContext(spanContext)
        .hasSeverity(severity)
        .hasSeverityText(sevText);
  }

  @Test
  void emit_NoFields() {
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(10L);
    when(logEmitterSharedState.getClock()).thenReturn(clock);

    builder.emit();

    assertThat(emittedLog.get())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(SCOPE_INFO)
        .hasBody(Body.empty().asString())
        .hasEpochNanos(10L)
        .hasAttributes(Attributes.empty())
        .hasSpanContext(SpanContext.getInvalid())
        .hasSeverity(Severity.UNDEFINED_SEVERITY_NUMBER);
  }

  @Test
  void emit_AfterShutdown() {
    when(logEmitterSharedState.hasBeenShutdown()).thenReturn(true);

    builder.emit();

    assertThat(emittedLog.get()).isNull();
  }
}
