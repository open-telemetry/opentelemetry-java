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

import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
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
class SimpleLogRecordProcessorTest {

  private static final LogRecordData LOG_RECORD_DATA = TestLogRecordData.builder().build();

  @Mock private LogRecordExporter logRecordExporter;
  @Mock private ReadWriteLogRecord readWriteLogRecord;

  private LogRecordProcessor logRecordProcessor;

  @BeforeEach
  void setUp() {
    logRecordProcessor = SimpleLogRecordProcessor.create(logRecordExporter);
    when(logRecordExporter.export(anyCollection())).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(readWriteLogRecord.toLogRecordData()).thenReturn(LOG_RECORD_DATA);
  }

  @Test
  void create_NullExporter() {
    assertThatThrownBy(() -> SimpleLogRecordProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  void onEmit() {
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    verify(logRecordExporter).export(Collections.singletonList(LOG_RECORD_DATA));
  }

  @Test
  @SuppressLogger(SimpleLogRecordProcessor.class)
  void onEmit_ExporterError() {
    when(logRecordExporter.export(any())).thenThrow(new RuntimeException("Exporter error!"));
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    verify(logRecordExporter, times(2)).export(anyList());
  }

  @Test
  void forceFlush() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logRecordExporter.export(any())).thenReturn(export1, export2);

    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);

    verify(logRecordExporter, times(2)).export(Collections.singletonList(LOG_RECORD_DATA));

    CompletableResultCode flush = logRecordProcessor.forceFlush();
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

    when(logRecordExporter.export(any())).thenReturn(export1, export2);

    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);
    logRecordProcessor.onEmit(Context.current(), readWriteLogRecord);

    verify(logRecordExporter, times(2)).export(Collections.singletonList(LOG_RECORD_DATA));

    CompletableResultCode shutdown = logRecordProcessor.shutdown();
    assertThat(shutdown.isDone()).isFalse();

    export1.succeed();
    assertThat(shutdown.isDone()).isFalse();
    verify(logRecordExporter, never()).shutdown();

    export2.succeed();
    assertThat(shutdown.isDone()).isTrue();
    assertThat(shutdown.isSuccess()).isTrue();
    verify(logRecordExporter).shutdown();
  }

  @Test
  void toString_Valid() {
    when(logRecordExporter.toString()).thenReturn("MockLogRecordExporter");
    assertThat(logRecordProcessor.toString())
        .isEqualTo("SimpleLogRecordProcessor{logRecordExporter=MockLogRecordExporter}");
  }
}
