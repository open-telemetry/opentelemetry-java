/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    sdkLogEmitterProvider =
        SdkLogEmitterProvider.builder().addLogProcessor(logProcessor).build();
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
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("test", "version", "http://url");
    assertThat(
            ((SdkLogEmitter)
                    sdkLogEmitterProvider
                        .logEmitterBuilder("test")
                        .setInstrumentationVersion("version")
                        .setSchemaUrl("http://url")
                        .build())
                .getInstrumentationLibraryInfo())
        .isEqualTo(expected);
  }

  @Test
  void logEmitterBuilder_DefaultEmitterName() {
    assertThat(
            ((SdkLogEmitter) sdkLogEmitterProvider.logEmitterBuilder(null).build())
                .getInstrumentationLibraryInfo()
                .getName())
        .isEqualTo(SdkLogEmitterProvider.DEFAULT_EMITTER_NAME);

    assertThat(
            ((SdkLogEmitter) sdkLogEmitterProvider.logEmitterBuilder("").build())
                .getInstrumentationLibraryInfo()
                .getName())
        .isEqualTo(SdkLogEmitterProvider.DEFAULT_EMITTER_NAME);
  }

  @Test
  void forceFlush() {
    sdkLogEmitterProvider.forceFlush();
    verify(logProcessor).forceFlush();
  }

  @Test
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
    List<LogData> seenLogs = new LinkedList<>();
    logProcessor = seenLogs::add;
    sdkLogEmitterProvider =
        SdkLogEmitterProvider.builder().setClock(clock).addLogProcessor(logProcessor).build();
    sdkLogEmitterProvider.logEmitterBuilder(null).build().logBuilder().emit();
    assertThat(seenLogs.size()).isEqualTo(1);
    assertThat(seenLogs.get(0).getEpochNanos()).isEqualTo(now);
  }
}
