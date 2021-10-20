/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
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
        .extracting(LogEmitterSharedState::getActiveLogProcessor)
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
        .extracting(LogEmitterSharedState::getActiveLogProcessor)
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
  void get_SameName() {
    assertThat(sdkLogEmitterProvider.get("test"))
        .isSameAs(sdkLogEmitterProvider.get("test"))
        .isSameAs(sdkLogEmitterProvider.get("test", null))
        .isSameAs(sdkLogEmitterProvider.logEmitterBuilder("test").build())
        .isNotSameAs(sdkLogEmitterProvider.get("test", "version"));
  }

  @Test
  void get_SameNameAndVersion() {
    assertThat(sdkLogEmitterProvider.get("test", "version"))
        .isSameAs(sdkLogEmitterProvider.get("test", "version"))
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
}
