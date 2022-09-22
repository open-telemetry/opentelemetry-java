/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleLogProcessorTest {

  private static final LogRecordData LOG_RECORD_DATA = TestLogRecordData.builder().build();

  @Mock private LogExporter logExporter;
  @Mock private ReadWriteLogRecord readWriteLogRecord;

  private LogProcessor logProcessor;

  @BeforeEach
  void setUp() {
    logProcessor = SimpleLogProcessor.create(logExporter);
    when(logExporter.export(anyCollection())).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(readWriteLogRecord.toLogRecordData()).thenReturn(LOG_RECORD_DATA);
  }

  @Test
  void create_NullExporter() {
    assertThatThrownBy(() -> SimpleLogProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  void onEmit() {
    logProcessor.onEmit(readWriteLogRecord);
    verify(logExporter).export(Collections.singletonList(LOG_RECORD_DATA));
  }

  @Test
  @SuppressLogger(SimpleLogProcessor.class)
  void onEmit_ExporterError() {
    when(logExporter.export(any())).thenThrow(new RuntimeException("Exporter error!"));
    logProcessor.onEmit(readWriteLogRecord);
    logProcessor.onEmit(readWriteLogRecord);
    verify(logExporter, times(2)).export(anyList());
  }

  @Test
  void forceFlush() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logExporter.export(any())).thenReturn(export1, export2);

    logProcessor.onEmit(readWriteLogRecord);
    logProcessor.onEmit(readWriteLogRecord);

    verify(logExporter, times(2)).export(Collections.singletonList(LOG_RECORD_DATA));

    CompletableResultCode flush = logProcessor.forceFlush();
    assertThat(flush.isDone()).isFalse();

    export1.succeed();
    assertThat(flush.isDone()).isFalse();

    export2.succeed();
    assertThat(flush.isDone()).isTrue();
    assertThat(flush.isSuccess()).isTrue();
  }

  @Test
  void shutdown() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logExporter.export(any())).thenReturn(export1, export2);

    logProcessor.onEmit(readWriteLogRecord);
    logProcessor.onEmit(readWriteLogRecord);

    verify(logExporter, times(2)).export(Collections.singletonList(LOG_RECORD_DATA));

    CompletableResultCode shutdown = logProcessor.shutdown();
    assertThat(shutdown.isDone()).isFalse();

    export1.succeed();
    assertThat(shutdown.isDone()).isFalse();
    verify(logExporter, never()).shutdown();

    export2.succeed();
    assertThat(shutdown.isDone()).isTrue();
    assertThat(shutdown.isSuccess()).isTrue();
    verify(logExporter).shutdown();
  }
}
