/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
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
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
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
    LogProcessor logProcessor = seenLog::set;
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(5L);

    when(state.getResource()).thenReturn(Resource.getDefault());
    when(state.getLogProcessor()).thenReturn(logProcessor);
    when(state.getClock()).thenReturn(clock);

    SdkLogger logger = new SdkLogger(state, info);
    LogRecordBuilder logRecordBuilder = logger.logRecordBuilder();
    logRecordBuilder.setBody("foo");

    // Have to test through the builder
    logRecordBuilder.emit();
    assertThat(seenLog.get().toLogData()).hasBody("foo").hasEpochNanos(5);
  }

  @Test
  void logRecordBuilder_maxAttributeLength() {
    int maxLength = 25;
    AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogProcessor(seenLog::set)
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

    Attributes attributes = seenLog.get().toLogData().getAttributes();

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
            .addLogProcessor(seenLog::set)
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

    assertThat(seenLog.get().toLogData())
        .hasAttributes(expectedAttributes.build())
        .hasTotalAttributeCount(maxNumberOfAttrs * 2);
  }

  @Test
  void logRecordBuilder_AfterShutdown() {
    LogProcessor logProcessor = mock(LogProcessor.class);
    when(logProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder().addLogProcessor(logProcessor).build();

    loggerProvider.shutdown().join(10, TimeUnit.SECONDS);
    loggerProvider.get("test").logRecordBuilder().emit();

    verify(logProcessor, never()).onEmit(any());
  }
}
