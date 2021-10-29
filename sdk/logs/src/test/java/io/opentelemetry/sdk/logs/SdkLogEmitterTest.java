/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkLogEmitterTest {

  private static final String INSTRUMENTATION_LIBRARY_NAME = SdkLogEmitter.class.getName();
  private static final String INSTRUMENTATION_LIBRARY_VERSION = "0.0.1";
  private static final String SCHEMA_URL = "http://schemaurl";

  @Mock private LogProcessor logProcessor;
  private SdkLogEmitterProvider sdkLogEmitterProvider;
  private LogEmitter sdkLogEmitter;

  @BeforeEach
  void setup() {
    when(logProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    sdkLogEmitterProvider = SdkLogEmitterProvider.builder().addLogProcessor(logProcessor).build();
    sdkLogEmitter =
        sdkLogEmitterProvider
            .logEmitterBuilder(INSTRUMENTATION_LIBRARY_NAME)
            .setInstrumentationVersion(INSTRUMENTATION_LIBRARY_VERSION)
            .setSchemaUrl(SCHEMA_URL)
            .build();
  }

  @Test
  void emit() {
    long epochMillis = System.currentTimeMillis();
    Body body = Body.stringBody("message");
    sdkLogEmitter.logBuilder().setEpoch(epochMillis, TimeUnit.MILLISECONDS).setBody(body).emit();

    ArgumentCaptor<LogData> captor = ArgumentCaptor.forClass(LogData.class);
    verify(logProcessor).emit(captor.capture());

    LogData logData = captor.getValue();
    assertThat(logData.getResource()).isEqualTo(Resource.getDefault());
    assertThat(logData.getInstrumentationLibraryInfo())
        .isEqualTo(
            InstrumentationLibraryInfo.create(
                INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION, SCHEMA_URL));
    assertThat(logData.getEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(epochMillis));
    assertThat(logData.getBody()).isEqualTo(body);
  }

  @Test
  void emit_AfterShutdown() {
    sdkLogEmitterProvider.shutdown().join(10, TimeUnit.SECONDS);

    sdkLogEmitter.logBuilder().setEpoch(Instant.now()).setBody("message").emit();
    verify(logProcessor, never()).emit(any());
  }
}
