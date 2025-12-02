/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkLoggerTest {

  @Test
  void logRecordBuilder() {
    LoggerSharedState state = mock(LoggerSharedState.class);
    InstrumentationScopeInfo info = InstrumentationScopeInfo.create("foo");
    AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
    LogRecordProcessor logRecordProcessor = (context, logRecord) -> seenLog.set(logRecord);
    Clock clock = mock(Clock.class);

    when(state.getResource()).thenReturn(Resource.getDefault());
    when(state.getLogRecordProcessor()).thenReturn(logRecordProcessor);
    when(state.getClock()).thenReturn(clock);

    SdkLogger logger = new SdkLogger(state, info, LoggerConfig.defaultConfig());
    LogRecordBuilder logRecordBuilder = logger.logRecordBuilder();
    logRecordBuilder.setBody("foo");

    // Have to test through the builder
    logRecordBuilder.emit();
    assertThat(seenLog.get().toLogRecordData()).hasBody("foo").hasTimestamp(0);
  }

  @Test
  void logRecordBuilder_maxAttributeLength() {
    int maxLength = 25;
    AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor((context, logRecord) -> seenLog.set(logRecord))
            .setLogLimits(() -> LogLimits.builder().setMaxAttributeValueLength(maxLength).build())
            .build();
    LogRecordBuilder logRecordBuilder = loggerProvider.get("test").logRecordBuilder();
    String strVal = StringUtils.padLeft("", maxLength);
    String tooLongStrVal = strVal + strVal;

    logRecordBuilder
        .setAllAttributes(
            Attributes.builder()
                .put("string", tooLongStrVal)
                .put("boolean", true)
                .put("long", 1L)
                .put("double", 1.0)
                .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
                .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
                .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
                .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
                .build())
        .emit();

    Attributes attributes = seenLog.get().toLogRecordData().getAttributes();

    assertThat(attributes)
        .containsEntry("string", strVal)
        .containsEntry("boolean", true)
        .containsEntry("long", 1L)
        .containsEntry("double", 1.0)
        .containsEntry("stringArray", strVal, strVal)
        .containsEntry("booleanArray", true, false)
        .containsEntry("longArray", 1L, 2L)
        .containsEntry("doubleArray", 1.0, 2.0);
  }

  @Test
  void logRecordBuilder_maxAttributes() {
    int maxNumberOfAttrs = 8;
    AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor((context, logRecord) -> seenLog.set(logRecord))
            .setLogLimits(
                () -> LogLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttrs).build())
            .build();

    LogRecordBuilder builder = loggerProvider.get("test").logRecordBuilder();
    AttributesBuilder expectedAttributes = Attributes.builder();
    for (int i = 0; i < 2 * maxNumberOfAttrs; i++) {
      AttributeKey<Long> key = AttributeKey.longKey("key" + i);
      builder.setAttribute(key, (long) i);
      if (i < maxNumberOfAttrs) {
        expectedAttributes.put(key, (long) i);
      }
    }
    builder.emit();

    assertThat(seenLog.get().toLogRecordData())
        .hasAttributes(expectedAttributes.build())
        .hasTotalAttributeCount(maxNumberOfAttrs * 2);
  }

  @Test
  void logRecordBuilder_AfterShutdown() {
    LogRecordProcessor logRecordProcessor = mock(LogRecordProcessor.class);
    when(logRecordProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).build();

    loggerProvider.shutdown().join(10, TimeUnit.SECONDS);
    loggerProvider.get("test").logRecordBuilder().emit();

    verify(logRecordProcessor, never()).onEmit(any(), any());
  }

  @Test
  void updateLoggerConfig() {
    LogRecordProcessor logRecordProcessor = mock(LogRecordProcessor.class);
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).build();
    SdkLogger logger = (SdkLogger) loggerProvider.get("test");

    // Start with default config
    assertThat(logger.loggerEnabled).isTrue();
    assertThat(logger.minimumSeverity).isEqualTo(Severity.UNDEFINED_SEVERITY_NUMBER);
    assertThat(logger.traceBased).isFalse();

    // Update to custom config
    LoggerConfig config =
        LoggerConfig.builder()
            .setEnabled(false)
            .setMinimumSeverity(Severity.WARN)
            .setTraceBased(true)
            .build();
    logger.updateLoggerConfig(config);

    assertThat(logger.loggerEnabled).isFalse();
    assertThat(logger.minimumSeverity).isEqualTo(Severity.WARN);
    assertThat(logger.traceBased).isTrue();
  }

  @Test
  void isEnabled_MinimumSeverity() {
    LogRecordProcessor logRecordProcessor = mock(LogRecordProcessor.class);
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).build();
    SdkLogger logger = (SdkLogger) loggerProvider.get("test");

    LoggerConfig config = LoggerConfig.builder().setMinimumSeverity(Severity.WARN).build();
    logger.updateLoggerConfig(config);

    // Below minimum severity - should be disabled
    assertThat(logger.isEnabled(Severity.INFO, Context.current())).isFalse();
    assertThat(logger.isEnabled(Severity.DEBUG, Context.current())).isFalse();

    // At or above minimum severity - should be enabled
    assertThat(logger.isEnabled(Severity.WARN, Context.current())).isTrue();
    assertThat(logger.isEnabled(Severity.ERROR, Context.current())).isTrue();

    // Undefined severity - should be enabled (bypasses minimum severity filter)
    assertThat(logger.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isTrue();
  }

  @Test
  void isEnabled_TraceBased() {
    LogRecordProcessor logRecordProcessor = mock(LogRecordProcessor.class);
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).build();
    SdkLogger logger = (SdkLogger) loggerProvider.get("test");

    LoggerConfig config = LoggerConfig.builder().setTraceBased(true).build();
    logger.updateLoggerConfig(config);

    // No trace context - should be enabled
    assertThat(logger.isEnabled(Severity.INFO, Context.current())).isTrue();

    // Sampled trace - should be enabled
    SpanContext sampledSpanContext =
        SpanContext.create(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "bbbbbbbbbbbbbbbb",
            TraceFlags.getSampled(),
            TraceState.getDefault());
    Context sampledContext = Span.wrap(sampledSpanContext).storeInContext(Context.root());
    assertThat(logger.isEnabled(Severity.INFO, sampledContext)).isTrue();

    // Unsampled trace - should be disabled
    SpanContext unsampledSpanContext =
        SpanContext.create(
            "cccccccccccccccccccccccccccccccc",
            "dddddddddddddddd",
            TraceFlags.getDefault(),
            TraceState.getDefault());
    Context unsampledContext = Span.wrap(unsampledSpanContext).storeInContext(Context.root());
    assertThat(logger.isEnabled(Severity.INFO, unsampledContext)).isFalse();
  }
}
