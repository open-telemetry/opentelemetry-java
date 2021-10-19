/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.data.ReadableLogRecord;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogEmitterTest {

  private static final String INSTRUMENTATION_LIBRARY_NAME = LogEmitter.class.getName();
  private static final String INSTRUMENTATION_LIBRARY_VERSION = "0.0.1";
  private static final String SCHEMA_URL = "http://schemaurl";

  @Mock private LogProcessor logProcessor;
  private LogEmitter logEmitter;

  @BeforeEach
  void setup() {
    logEmitter =
        LogEmitterProvider.builder()
            .addLogProcessor(logProcessor)
            .build()
            .logEmitterBuilder(INSTRUMENTATION_LIBRARY_NAME)
            .setInstrumentationVersion(INSTRUMENTATION_LIBRARY_VERSION)
            .setSchemaUrl(SCHEMA_URL)
            .build();
  }

  @Test
  void emit() {
    LogRecord logRecord =
        ReadableLogRecord.builder()
            .setEpochMillis(System.currentTimeMillis())
            .setBody(Body.stringBody("message"))
            .build();
    logEmitter.emit(logRecord);

    ArgumentCaptor<LogData> captor = ArgumentCaptor.forClass(LogData.class);
    verify(logProcessor).emit(captor.capture());

    LogData logData = captor.getValue();
    assertThat(logData.getResource()).isEqualTo(Resource.getDefault());
    assertThat(logData.getInstrumentationLibraryInfo())
        .isEqualTo(
            InstrumentationLibraryInfo.create(
                INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION, SCHEMA_URL));
    assertThat(logData.getEpochNanos()).isEqualTo(logRecord.getEpochNanos());
    assertThat(logData.getBody()).isEqualTo(logRecord.getBody());
  }
}
