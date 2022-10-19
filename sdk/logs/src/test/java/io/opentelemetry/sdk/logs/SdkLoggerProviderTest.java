/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static org.assertj.core.api.Assertions.as;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkLoggerProviderTest {

  @Mock private LogRecordProcessor logRecordProcessor;

  private SdkLoggerProvider sdkLoggerProvider;

  @BeforeEach
  void setup() {
    sdkLoggerProvider =
        SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).build();
    when(logRecordProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void builder_defaultResource() {
    assertThat(SdkLoggerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getResource)
        .isEqualTo(Resource.getDefault());
  }

  @Test
  void builder_resourceProvided() {
    Resource resource = Resource.create(Attributes.builder().put("key", "value").build());

    assertThat(SdkLoggerProvider.builder().setResource(resource).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getResource)
        .isEqualTo(resource);
  }

  @Test
  void builder_noProcessor() {
    assertThat(SdkLoggerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getLogRecordProcessor)
        .isSameAs(NoopLogRecordProcessor.getInstance());
  }

  @Test
  void builder_defaultLogLimits() {
    assertThat(SdkLoggerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getLogLimits)
        .isSameAs(LogLimits.getDefault());
  }

  @Test
  void builder_logLimitsProvided() {
    LogLimits logLimits =
        LogLimits.builder().setMaxNumberOfAttributes(1).setMaxAttributeValueLength(1).build();
    assertThat(SdkLoggerProvider.builder().setLogLimits(() -> logLimits).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getLogLimits)
        .isSameAs(logLimits);
  }

  @Test
  void builder_defaultClock() {
    assertThat(SdkLoggerProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getClock)
        .isSameAs(Clock.getDefault());
  }

  @Test
  void builder_clockProvided() {
    Clock clock = mock(Clock.class);
    assertThat(SdkLoggerProvider.builder().setClock(clock).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getClock)
        .isSameAs(clock);
  }

  @Test
  void builder_multipleProcessors() {
    assertThat(
            SdkLoggerProvider.builder()
                .addLogRecordProcessor(logRecordProcessor)
                .addLogRecordProcessor(logRecordProcessor)
                .build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LoggerSharedState.class)))
        .extracting(LoggerSharedState::getLogRecordProcessor)
        .satisfies(
            activeLogRecordProcessor -> {
              assertThat(activeLogRecordProcessor).isInstanceOf(MultiLogRecordProcessor.class);
              assertThat(activeLogRecordProcessor)
                  .extracting(
                      "logRecordProcessors",
                      as(InstanceOfAssertFactories.list(LogRecordProcessor.class)))
                  .hasSize(2);
            });
  }

  @Test
  void loggerBuilder_SameName() {
    assertThat(sdkLoggerProvider.loggerBuilder("test").build())
        .isSameAs(sdkLoggerProvider.get("test"))
        .isSameAs(sdkLoggerProvider.loggerBuilder("test").build())
        .isNotSameAs(
            sdkLoggerProvider.loggerBuilder("test").setInstrumentationVersion("version").build());
  }

  @Test
  void loggerBuilder_SameNameAndVersion() {
    assertThat(sdkLoggerProvider.loggerBuilder("test").setInstrumentationVersion("version").build())
        .isSameAs(
            sdkLoggerProvider.loggerBuilder("test").setInstrumentationVersion("version").build())
        .isSameAs(
            sdkLoggerProvider
                .loggerBuilder("test")
                .setInstrumentationVersion("version")
                // Attributes are not part of logger identity
                .setAttributes(Attributes.builder().put("key", "value").build())
                .build())
        .isNotSameAs(
            sdkLoggerProvider
                .loggerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void loggerBuilder_SameNameVersionSchemaAttributes() {
    assertThat(
            sdkLoggerProvider
                .loggerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .setAttributes(Attributes.builder().put("key", "value").build())
                .build())
        .isSameAs(
            sdkLoggerProvider
                .loggerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .setAttributes(Attributes.builder().put("key", "value").build())
                .build())
        .isSameAs(
            sdkLoggerProvider
                .loggerBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .setAttributes(Attributes.builder().put("key", "another-value").build())
                .build());
  }

  @Test
  void loggerBuilder_PropagatesToLogger() {
    InstrumentationScopeInfo expected =
        InstrumentationScopeInfo.builder("test")
            .setVersion("version")
            .setSchemaUrl("http://url")
            .build();
    assertThat(
            ((SdkLogger)
                    sdkLoggerProvider
                        .loggerBuilder("test")
                        .setInstrumentationVersion("version")
                        .setSchemaUrl("http://url")
                        .build())
                .getInstrumentationScopeInfo())
        .isEqualTo(expected);
  }

  @Test
  void loggerBuilder_DefaultLoggerName() {
    assertThat(
            ((SdkLogger) sdkLoggerProvider.loggerBuilder(null).build())
                .getInstrumentationScopeInfo()
                .getName())
        .isEqualTo(SdkLoggerProvider.DEFAULT_LOGGER_NAME);

    assertThat(
            ((SdkLogger) sdkLoggerProvider.loggerBuilder("").build())
                .getInstrumentationScopeInfo()
                .getName())
        .isEqualTo(SdkLoggerProvider.DEFAULT_LOGGER_NAME);
  }

  @Test
  void loggerBuilder_NoProcessor_UsesNoop() {
    assertThat(SdkLoggerProvider.builder().build().loggerBuilder("test"))
        .isSameAs(LoggerProvider.noop().loggerBuilder("test"));
  }

  @Test
  void loggerBuilder_WithLogRecordProcessor() {
    Resource resource = Resource.builder().put("r1", "v1").build();
    AtomicReference<LogRecordData> logRecordData = new AtomicReference<>();
    sdkLoggerProvider =
        SdkLoggerProvider.builder()
            .setResource(resource)
            .addLogRecordProcessor(
                logRecord -> {
                  logRecord.setAttribute(null, null);
                  // Overwrite k1
                  logRecord.setAttribute(AttributeKey.stringKey("k1"), "new-v1");
                  // Add new attribute k3
                  logRecord.setAttribute(AttributeKey.stringKey("k3"), "v3");
                  logRecordData.set(logRecord.toLogRecordData());
                })
            .build();

    SpanContext spanContext =
        SpanContext.create(
            "33333333333333333333333333333333",
            "7777777777777777",
            TraceFlags.getSampled(),
            TraceState.getDefault());
    sdkLoggerProvider
        .get("test")
        .logRecordBuilder()
        .setEpoch(100, TimeUnit.NANOSECONDS)
        .setContext(Span.wrap(spanContext).storeInContext(Context.root()))
        .setSeverity(Severity.DEBUG)
        .setSeverityText("debug")
        .setBody("body")
        .setAttribute(AttributeKey.stringKey("k1"), "v1")
        .setAttribute(AttributeKey.stringKey("k2"), "v2")
        .emit();

    assertThat(logRecordData.get())
        .hasResource(resource)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test"))
        .hasEpochNanos(100)
        .hasSpanContext(spanContext)
        .hasSeverity(Severity.DEBUG)
        .hasSeverityText("debug")
        .hasBody("body")
        .hasAttributes(
            Attributes.builder().put("k1", "new-v1").put("k2", "v2").put("k3", "v3").build());
  }

  @Test
  void forceFlush() {
    sdkLoggerProvider.forceFlush();
    verify(logRecordProcessor).forceFlush();
  }

  @Test
  @SuppressLogger(SdkLoggerProvider.class)
  void shutdown() {
    sdkLoggerProvider.shutdown();
    sdkLoggerProvider.shutdown();
    verify(logRecordProcessor, times(1)).shutdown();
  }

  @Test
  void close() {
    sdkLoggerProvider.close();
    verify(logRecordProcessor).shutdown();
  }

  @Test
  void canSetClock() {
    long now = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(now);
    List<ReadWriteLogRecord> seenLogs = new ArrayList<>();
    logRecordProcessor = seenLogs::add;
    sdkLoggerProvider =
        SdkLoggerProvider.builder()
            .setClock(clock)
            .addLogRecordProcessor(logRecordProcessor)
            .build();
    sdkLoggerProvider.loggerBuilder(null).build().logRecordBuilder().emit();
    assertThat(seenLogs.size()).isEqualTo(1);
    assertThat(seenLogs.get(0).toLogRecordData().getEpochNanos()).isEqualTo(now);
  }
}
