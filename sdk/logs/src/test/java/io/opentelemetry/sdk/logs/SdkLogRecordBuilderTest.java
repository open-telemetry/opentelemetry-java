/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
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

    SdkLogger logger = new SdkLogger(loggerSharedState, SCOPE_INFO, LoggerConfig.enabled());
    builder = new SdkLogRecordBuilder(loggerSharedState, SCOPE_INFO, logger);
  }

  @Test
  void emit_AllFields() {
    Instant timestamp = Instant.now();
    Instant observedTimestamp = Instant.now().plusNanos(100);

    String eventName = "my.event.name";
    String bodyStr = "body";
    String sevText = "sevText";
    Severity severity = Severity.DEBUG3;
    SpanContext spanContext =
        SpanContext.create(
            "33333333333333333333333333333333",
            "7777777777777777",
            TraceFlags.getSampled(),
            TraceState.getDefault());

    builder.setEventName(eventName);
    builder.setBody(bodyStr);
    builder.setTimestamp(123, TimeUnit.SECONDS);
    builder.setTimestamp(timestamp);
    builder.setObservedTimestamp(456, TimeUnit.SECONDS);
    builder.setObservedTimestamp(observedTimestamp);
    builder.setAttribute((String) null, (String) null);
    builder.setAttribute(AttributeKey.stringKey("k1"), "v1");
    builder.setAllAttributes(Attributes.builder().put("k2", "v2").put("k3", "v3").build());
    builder.setContext(Span.wrap(spanContext).storeInContext(Context.root()));
    builder.setSeverity(severity);
    builder.setSeverityText(sevText);
    builder.emit();
    assertThat(emittedLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(SCOPE_INFO)
        .hasEventName(eventName)
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
        .hasBody((Value<?>) null)
        .hasTimestamp(0L)
        .hasObservedTimestamp(10L)
        .hasAttributes(Attributes.empty())
        .hasSpanContext(SpanContext.getInvalid())
        .hasSeverity(Severity.UNDEFINED_SEVERITY_NUMBER);
  }

  @Test
  void emit_WithMinimumSeverityConfiguration() {
    LoggerConfig config =
        LoggerConfig.builder().setMinimumSeverity(Severity.INFO.getSeverityNumber()).build();
    SdkLogger logger = new SdkLogger(loggerSharedState, SCOPE_INFO, config);
    builder = new SdkLogRecordBuilder(loggerSharedState, SCOPE_INFO, logger);

    builder.setBody("too-low").setSeverity(Severity.DEBUG).emit();
    assertThat(emittedLog.get()).isNull();

    builder.setBody("allowed").setSeverity(Severity.INFO).emit();
    assertThat(emittedLog.get().toLogRecordData()).hasBody("allowed");
  }

  @Test
  void emit_DropsUnsampledTraceWhenTraceBased() {
    LoggerConfig config = LoggerConfig.builder().setTraceBased(true).build();
    SdkLogger logger = new SdkLogger(loggerSharedState, SCOPE_INFO, config);
    builder = new SdkLogRecordBuilder(loggerSharedState, SCOPE_INFO, logger);

    SpanContext unsampledSpanContext =
        SpanContext.create(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "bbbbbbbbbbbbbbbb",
            TraceFlags.getDefault(),
            TraceState.getDefault());
    builder
        .setBody("unsampled")
        .setContext(Span.wrap(unsampledSpanContext).storeInContext(Context.root()))
        .emit();
    assertThat(emittedLog.get()).isNull();

    SpanContext sampledSpanContext =
        SpanContext.create(
            "cccccccccccccccccccccccccccccccc",
            "dddddddddddddddd",
            TraceFlags.getSampled(),
            TraceState.getDefault());
    builder
        .setBody("sampled")
        .setContext(Span.wrap(sampledSpanContext).storeInContext(Context.root()))
        .emit();
    assertThat(emittedLog.get().toLogRecordData())
        .hasSpanContext(sampledSpanContext)
        .hasBody("sampled");
  }

  @Test
  void emit_AllowsUndefinedSeverityWithMinimumSeverity() {
    LoggerConfig config =
        LoggerConfig.builder().setMinimumSeverity(Severity.WARN.getSeverityNumber()).build();
    SdkLogger logger = new SdkLogger(loggerSharedState, SCOPE_INFO, config);
    builder = new SdkLogRecordBuilder(loggerSharedState, SCOPE_INFO, logger);

    // Undefined severity should bypass the minimum severity filter
    builder.setBody("undefined-severity").setSeverity(Severity.UNDEFINED_SEVERITY_NUMBER).emit();
    assertThat(emittedLog.get().toLogRecordData())
        .hasBody("undefined-severity")
        .hasSeverity(Severity.UNDEFINED_SEVERITY_NUMBER);
  }

  @Test
  void emit_AllowsNoTraceContextWithTraceBased() {
    LoggerConfig config = LoggerConfig.builder().setTraceBased(true).build();
    SdkLogger logger = new SdkLogger(loggerSharedState, SCOPE_INFO, config);
    builder = new SdkLogRecordBuilder(loggerSharedState, SCOPE_INFO, logger);

    // No trace context should bypass the trace-based filter
    builder.setBody("no-trace-context").emit();
    assertThat(emittedLog.get().toLogRecordData()).hasBody("no-trace-context");
  }

  @Test
  void testConvenienceAttributeMethods() {
    builder
        .setAttribute("foo", "bar")
        .setAttribute("lk", 12L)
        .setAttribute("dk", 12.123)
        .setAttribute("bk", true)
        .setAttribute("ik", 13)
        .emit();
    assertThat(emittedLog.get().toLogRecordData())
        .hasAttributesSatisfyingExactly(
            equalTo(stringKey("foo"), "bar"),
            equalTo(longKey("lk"), 12L),
            equalTo(doubleKey("dk"), 12.123),
            equalTo(booleanKey("bk"), true),
            equalTo(longKey("ik"), 13L));
  }
}
