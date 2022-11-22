/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MultiLogRecordExporterTest {
  @Mock private LogRecordExporter logRecordExporter1;
  @Mock private LogRecordExporter logRecordExporter2;
  private static final List<LogRecordData> LOG_LIST =
      Collections.singletonList(
          TestLogRecordData.builder().setBody("Message!").setSeverity(Severity.DEBUG).build());

  @Test
  void empty() {
    LogRecordExporter multiLogRecordExporter = LogRecordExporter.composite(Collections.emptyList());
    multiLogRecordExporter.export(LOG_LIST);
    multiLogRecordExporter.shutdown();
  }

  @Test
  void oneLogRecordExporter() {
    LogRecordExporter multiLogRecordExporter =
        LogRecordExporter.composite(Collections.singletonList(logRecordExporter1));

    when(logRecordExporter1.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.export(LOG_LIST).isSuccess()).isTrue();
    verify(logRecordExporter1).export(same(LOG_LIST));

    when(logRecordExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.flush().isSuccess()).isTrue();
    verify(logRecordExporter1).flush();

    when(logRecordExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiLogRecordExporter.shutdown();
    verify(logRecordExporter1).shutdown();
  }

  @Test
  void twoLogRecordExporter() {
    LogRecordExporter multiLogRecordExporter =
        LogRecordExporter.composite(Arrays.asList(logRecordExporter1, logRecordExporter2));

    when(logRecordExporter1.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter2.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.export(LOG_LIST).isSuccess()).isTrue();
    verify(logRecordExporter1).export(same(LOG_LIST));
    verify(logRecordExporter2).export(same(LOG_LIST));

    when(logRecordExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.flush().isSuccess()).isTrue();
    verify(logRecordExporter1).flush();
    verify(logRecordExporter2).flush();

    when(logRecordExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiLogRecordExporter.shutdown();
    verify(logRecordExporter1).shutdown();
    verify(logRecordExporter2).shutdown();
  }

  @Test
  void twoLogRecordExporter_OneReturnFailure() {
    LogRecordExporter multiLogRecordExporter =
        LogRecordExporter.composite(Arrays.asList(logRecordExporter1, logRecordExporter2));

    when(logRecordExporter1.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter2.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiLogRecordExporter.export(LOG_LIST).isSuccess()).isFalse();
    verify(logRecordExporter1).export(same(LOG_LIST));
    verify(logRecordExporter2).export(same(LOG_LIST));

    when(logRecordExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter2.flush()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiLogRecordExporter.flush().isSuccess()).isFalse();
    verify(logRecordExporter1).flush();
    verify(logRecordExporter2).flush();

    when(logRecordExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordExporter2.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiLogRecordExporter.shutdown().isSuccess()).isFalse();
    verify(logRecordExporter1).shutdown();
    verify(logRecordExporter2).shutdown();
  }

  @Test
  @SuppressLogger(MultiLogRecordExporter.class)
  void twoLogRecordExporter_FirstThrows() {
    LogRecordExporter multiLogRecordExporter =
        LogRecordExporter.composite(Arrays.asList(logRecordExporter1, logRecordExporter2));

    Mockito.doThrow(new IllegalArgumentException("No export for you."))
        .when(logRecordExporter1)
        .export(ArgumentMatchers.anyList());
    when(logRecordExporter2.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.export(LOG_LIST).isSuccess()).isFalse();
    verify(logRecordExporter1).export(same(LOG_LIST));
    verify(logRecordExporter2).export(same(LOG_LIST));

    Mockito.doThrow(new IllegalArgumentException("No flush for you."))
        .when(logRecordExporter1)
        .flush();
    when(logRecordExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.flush().isSuccess()).isFalse();
    verify(logRecordExporter1).flush();
    verify(logRecordExporter2).flush();

    Mockito.doThrow(new IllegalArgumentException("No shutdown for you."))
        .when(logRecordExporter1)
        .shutdown();
    when(logRecordExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogRecordExporter.shutdown().isSuccess()).isFalse();
    verify(logRecordExporter1).shutdown();
    verify(logRecordExporter2).shutdown();
  }

  @Test
  void toString_Valid() {
    when(logRecordExporter1.toString()).thenReturn("LogRecordExporter1");
    when(logRecordExporter2.toString()).thenReturn("LogRecordExporter2");
    assertThat(LogRecordExporter.composite(logRecordExporter1, logRecordExporter2).toString())
        .isEqualTo(
            "MultiLogRecordExporter{logRecordExporters=[LogRecordExporter1, LogRecordExporter2]}");
  }
}
