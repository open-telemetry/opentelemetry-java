/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static io.opentelemetry.sdk.logs.data.Severity.DEBUG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.util.TestUtil;
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

  @Mock private LogExporter logExporter;

  private LogProcessor logProcessor;

  @BeforeEach
  void setUp() {
    logProcessor = SimpleLogProcessor.create(logExporter);
    when(logExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void create_NullExporter() {
    assertThatThrownBy(() -> SimpleLogProcessor.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("exporter");
  }

  @Test
  void addLogRecord() {
    LogData logData = TestUtil.createLogData(DEBUG, "Log message");
    logProcessor.emit(logData);
    verify(logExporter).export(Collections.singletonList(logData));
  }

  @Test
  void addLogRecord_ExporterError() {
    LogData logData = TestUtil.createLogData(DEBUG, "Log message");
    when(logExporter.export(any())).thenThrow(new RuntimeException("Exporter error!"));
    logProcessor.emit(logData);
    logProcessor.emit(logData);
    verify(logExporter, times(2)).export(Collections.singletonList(logData));
  }

  @Test
  void forceFlush() {
    CompletableResultCode export1 = new CompletableResultCode();
    CompletableResultCode export2 = new CompletableResultCode();

    when(logExporter.export(any())).thenReturn(export1, export2);

    LogData logData = TestUtil.createLogData(DEBUG, "Log message");
    logProcessor.emit(logData);
    logProcessor.emit(logData);

    verify(logExporter, times(2)).export(Collections.singletonList(logData));

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

    LogData logData = TestUtil.createLogData(DEBUG, "Log message");
    logProcessor.emit(logData);
    logProcessor.emit(logData);

    verify(logExporter, times(2)).export(Collections.singletonList(logData));

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
