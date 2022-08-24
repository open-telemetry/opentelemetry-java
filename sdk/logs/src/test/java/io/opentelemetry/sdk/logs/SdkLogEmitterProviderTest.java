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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
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
class SdkLogEmitterProviderTest {

  @Mock private LogProcessor logProcessor;

  private SdkLogEmitterProvider sdkLogEmitterProvider;

  @BeforeEach
  void setup() {
    sdkLogEmitterProvider = SdkLogEmitterProvider.builder().addLogProcessor(logProcessor).build();
    when(logProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void builder_defaultResource() {
    assertThat(SdkLogEmitterProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getResource)
        .isEqualTo(Resource.getDefault());
  }

  @Test
  void builder_resourceProvided() {
    Resource resource = Resource.create(Attributes.builder().put("key", "value").build());

    assertThat(SdkLogEmitterProvider.builder().setResource(resource).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getResource)
        .isEqualTo(resource);
  }

  @Test
  void builder_noProcessor() {
    assertThat(SdkLogEmitterProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getLogProcessor)
        .isSameAs(NoopLogProcessor.getInstance());
  }

  @Test
  void builder_defaultLogLimits() {
    assertThat(SdkLogEmitterProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getLogLimits)
        .isSameAs(LogLimits.getDefault());
  }

  @Test
  void builder_logLimitsProvided() {
    LogLimits logLimits =
        LogLimits.builder().setMaxNumberOfAttributes(1).setMaxAttributeValueLength(1).build();
    assertThat(SdkLogEmitterProvider.builder().setLogLimits(() -> logLimits).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getLogLimits)
        .isSameAs(logLimits);
  }

  @Test
  void builder_defaultClock() {
    assertThat(SdkLogEmitterProvider.builder().build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getClock)
        .isSameAs(Clock.getDefault());
  }

  @Test
  void builder_clockProvided() {
    Clock clock = mock(Clock.class);
    assertThat(SdkLogEmitterProvider.builder().setClock(clock).build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getClock)
        .isSameAs(clock);
  }

  @Test
  void builder_multipleProcessors() {
    assertThat(
            SdkLogEmitterProvider.builder()
                .addLogProcessor(logProcessor)
                .addLogProcessor(logProcessor)
                .build())
        .extracting("sharedState", as(InstanceOfAssertFactories.type(LogEmitterSharedState.class)))
        .extracting(LogEmitterSharedState::getLogProcessor)
        .satisfies(
            activeLogProcessor -> {
              assertThat(activeLogProcessor).isInstanceOf(MultiLogProcessor.class);
              assertThat(activeLogProcessor)
                  .extracting(
                      "logProcessors", as(InstanceOfAssertFactories.list(LogProcessor.class)))
                  .hasSize(2);
            });
  }

  @Test
  void logEmitterBuilder_SameName() {
    assertThat(sdkLogEmitterProvider.logEmitterBuilder("test").build())
        .isSameAs(sdkLogEmitterProvider.get("test"))
        .isSameAs(sdkLogEmitterProvider.logEmitterBuilder("test").build())
        .isNotSameAs(
            sdkLogEmitterProvider
                .logEmitterBuilder("test")
                .setInstrumentationVersion("version")
                .build());
  }

  @Test
  void logEmitterBuilder_SameNameAndVersion() {
    assertThat(
            sdkLogEmitterProvider
                .logEmitterBuilder("test")
                .setInstrumentationVersion("version")
                .build())
        .isSameAs(
            sdkLogEmitterProvider
                .logEmitterBuilder("test")
                .setInstrumentationVersion("version")
                .build())
        .isNotSameAs(
            sdkLogEmitterProvider
                .logEmitterBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void logEmitterBuilder_SameNameVersionAndSchema() {
    assertThat(
            sdkLogEmitterProvider
                .logEmitterBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build())
        .isSameAs(
            sdkLogEmitterProvider
                .logEmitterBuilder("test")
                .setInstrumentationVersion("version")
                .setSchemaUrl("http://url")
                .build());
  }

  @Test
  void logEmitterBuilder_PropagatesToEmitter() {
    InstrumentationScopeInfo expected =
        InstrumentationScopeInfo.builder("test")
            .setVersion("version")
            .setSchemaUrl("http://url")
            .build();
    assertThat(
            ((SdkLogEmitter)
                    sdkLogEmitterProvider
                        .logEmitterBuilder("test")
                        .setInstrumentationVersion("version")
                        .setSchemaUrl("http://url")
                        .build())
                .getInstrumentationScopeInfo())
        .isEqualTo(expected);
  }

  @Test
  void logEmitterBuilder_DefaultEmitterName() {
    assertThat(
            ((SdkLogEmitter) sdkLogEmitterProvider.logEmitterBuilder(null).build())
                .getInstrumentationScopeInfo()
                .getName())
        .isEqualTo(SdkLogEmitterProvider.DEFAULT_EMITTER_NAME);

    assertThat(
            ((SdkLogEmitter) sdkLogEmitterProvider.logEmitterBuilder("").build())
                .getInstrumentationScopeInfo()
                .getName())
        .isEqualTo(SdkLogEmitterProvider.DEFAULT_EMITTER_NAME);
  }

  @Test
  void logEmitterBuilder_NoProcessor_UsesNoop() {
    assertThat(SdkLogEmitterProvider.builder().build().logEmitterBuilder("test"))
        .isInstanceOf(NoopLogEmitterBuilder.class);
  }

  @Test
  void logEmitterBuilder_WithLogProcessor() {
    Resource resource = Resource.builder().put("r1", "v1").build();
    AtomicReference<LogData> logData = new AtomicReference<>();
    sdkLogEmitterProvider =
        SdkLogEmitterProvider.builder()
            .setResource(resource)
            .addLogProcessor(
                logRecord -> {
                  logRecord.setAttribute(null, null);
                  // Overwrite k1
                  logRecord.setAttribute(AttributeKey.stringKey("k1"), "new-v1");
                  // Add new attribute k3
                  logRecord.setAttribute(AttributeKey.stringKey("k3"), "v3");
                  logData.set(logRecord.toLogData());
                })
            .build();

    SpanContext spanContext =
        SpanContext.create(
            "33333333333333333333333333333333",
            "7777777777777777",
            TraceFlags.getSampled(),
            TraceState.getDefault());
    sdkLogEmitterProvider
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

    assertThat(logData.get())
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
    sdkLogEmitterProvider.forceFlush();
    verify(logProcessor).forceFlush();
  }

  @Test
  @SuppressLogger(SdkLogEmitterProvider.class)
  void shutdown() {
    sdkLogEmitterProvider.shutdown();
    sdkLogEmitterProvider.shutdown();
    verify(logProcessor, times(1)).shutdown();
  }

  @Test
  void close() {
    sdkLogEmitterProvider.close();
    verify(logProcessor).shutdown();
  }

  @Test
  void canSetClock() {
    long now = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(now);
    List<ReadWriteLogRecord> seenLogs = new ArrayList<>();
    logProcessor = seenLogs::add;
    sdkLogEmitterProvider =
        SdkLogEmitterProvider.builder().setClock(clock).addLogProcessor(logProcessor).build();
    sdkLogEmitterProvider.logEmitterBuilder(null).build().logRecordBuilder().emit();
    assertThat(seenLogs.size()).isEqualTo(1);
    assertThat(seenLogs.get(0).toLogData().getEpochNanos()).isEqualTo(now);
  }
}
