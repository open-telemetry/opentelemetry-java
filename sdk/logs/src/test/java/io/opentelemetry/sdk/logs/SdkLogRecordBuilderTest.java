/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
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
class SdkLogRecordBuilderTest {

  private static final Resource RESOURCE = Resource.empty();
  private static final InstrumentationScopeInfo SCOPE_INFO = InstrumentationScopeInfo.empty();

  @Mock LoggerSharedState loggerSharedState;
  @Mock Clock clock;

  private final AtomicReference<ReadWriteLogRecord> emittedLog = new AtomicReference<>();
  private SdkLogRecordBuilder builder;

  @BeforeEach
  void setup() {
    when(loggerSharedState.getLogLimits()).thenReturn(LogLimits.getDefault());
    when(loggerSharedState.getLogRecordProcessor())
        .thenReturn((context, logRecord) -> emittedLog.set(logRecord));
    when(loggerSharedState.getResource()).thenReturn(RESOURCE);
    when(loggerSharedState.getClock()).thenReturn(clock);

    builder = new SdkLogRecordBuilder(loggerSharedState, SCOPE_INFO);
  }

  @Test
  void emit_AllFields() {
    Instant timestamp = Instant.now();
    Instant observedTimestamp = Instant.now().plusNanos(100);

    String bodyStr = "body";
    String sevText = "sevText";
    Severity severity = Severity.DEBUG3;
    SpanContext spanContext =
        SpanContext.create(
            "33333333333333333333333333333333",
            "7777777777777777",
            TraceFlags.getSampled(),
            TraceState.getDefault());

    builder.setBody(bodyStr);
    builder.setTimestamp(123, TimeUnit.SECONDS);
    builder.setTimestamp(timestamp);
    builder.setObservedTimestamp(456, TimeUnit.SECONDS);
    builder.setObservedTimestamp(observedTimestamp);
    builder.setAttribute(null, null);
    builder.setAttribute(AttributeKey.stringKey("k1"), "v1");
    builder.setAllAttributes(Attributes.builder().put("k2", "v2").put("k3", "v3").build());
    builder.setContext(Span.wrap(spanContext).storeInContext(Context.root()));
    builder.setSeverity(severity);
    builder.setSeverityText(sevText);
    builder.emit();
    assertThat(emittedLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(SCOPE_INFO)
        .hasBody(bodyStr)
        .hasTimestamp(TimeUnit.SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano())
        .hasObservedTimestamp(
            TimeUnit.SECONDS.toNanos(observedTimestamp.getEpochSecond())
                + observedTimestamp.getNano())
        .hasAttributes(Attributes.builder().put("k1", "v1").put("k2", "v2").put("k3", "v3").build())
        .hasSpanContext(spanContext)
        .hasSeverity(severity)
        .hasSeverityText(sevText);
  }

  @Test
  void emit_NoFields() {
    when(clock.now()).thenReturn(10L);

    builder.emit();

    assertThat(emittedLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(SCOPE_INFO)
        .hasBody(Body.empty().asString())
        .hasTimestamp(0L)
        .hasObservedTimestamp(10L)
        .hasAttributes(Attributes.empty())
        .hasSpanContext(SpanContext.getInvalid())
        .hasSeverity(Severity.UNDEFINED_SEVERITY_NUMBER);
  }
}
