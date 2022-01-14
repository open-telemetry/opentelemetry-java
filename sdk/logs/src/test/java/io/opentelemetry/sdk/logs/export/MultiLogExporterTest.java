/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.util.TestUtil;
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
class MultiLogExporterTest {
  @Mock private LogExporter logExporter1;
  @Mock private LogExporter logExporter2;
  private static final List<LogData> LOG_LIST =
      Collections.singletonList(TestUtil.createLogData(Severity.DEBUG, "Message!"));

  @Test
  void empty() {
    LogExporter multiLogExporter = LogExporter.composite(Collections.emptyList());
    multiLogExporter.export(LOG_LIST);
    multiLogExporter.shutdown();
  }

  @Test
  void oneLogExporter() {
    LogExporter multiLogExporter = LogExporter.composite(Collections.singletonList(logExporter1));

    when(logExporter1.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.export(LOG_LIST).isSuccess()).isTrue();
    verify(logExporter1).export(same(LOG_LIST));

    when(logExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.flush().isSuccess()).isTrue();
    verify(logExporter1).flush();

    when(logExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiLogExporter.shutdown();
    verify(logExporter1).shutdown();
  }

  @Test
  void twoLogExporter() {
    LogExporter multiLogExporter = LogExporter.composite(Arrays.asList(logExporter1, logExporter2));

    when(logExporter1.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter2.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.export(LOG_LIST).isSuccess()).isTrue();
    verify(logExporter1).export(same(LOG_LIST));
    verify(logExporter2).export(same(LOG_LIST));

    when(logExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.flush().isSuccess()).isTrue();
    verify(logExporter1).flush();
    verify(logExporter2).flush();

    when(logExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiLogExporter.shutdown();
    verify(logExporter1).shutdown();
    verify(logExporter2).shutdown();
  }

  @Test
  void twoLogExporter_OneReturnFailure() {
    LogExporter multiLogExporter = LogExporter.composite(Arrays.asList(logExporter1, logExporter2));

    when(logExporter1.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter2.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiLogExporter.export(LOG_LIST).isSuccess()).isFalse();
    verify(logExporter1).export(same(LOG_LIST));
    verify(logExporter2).export(same(LOG_LIST));

    when(logExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter2.flush()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiLogExporter.flush().isSuccess()).isFalse();
    verify(logExporter1).flush();
    verify(logExporter2).flush();

    when(logExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logExporter2.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiLogExporter.shutdown().isSuccess()).isFalse();
    verify(logExporter1).shutdown();
    verify(logExporter2).shutdown();
  }

  @Test
  void twoLogExporter_FirstThrows() {
    LogExporter multiLogExporter = LogExporter.composite(Arrays.asList(logExporter1, logExporter2));

    Mockito.doThrow(new IllegalArgumentException("No export for you."))
        .when(logExporter1)
        .export(ArgumentMatchers.anyList());
    when(logExporter2.export(same(LOG_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.export(LOG_LIST).isSuccess()).isFalse();
    verify(logExporter1).export(same(LOG_LIST));
    verify(logExporter2).export(same(LOG_LIST));

    Mockito.doThrow(new IllegalArgumentException("No flush for you.")).when(logExporter1).flush();
    when(logExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.flush().isSuccess()).isFalse();
    verify(logExporter1).flush();
    verify(logExporter2).flush();

    Mockito.doThrow(new IllegalArgumentException("No shutdown for you."))
        .when(logExporter1)
        .shutdown();
    when(logExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiLogExporter.shutdown().isSuccess()).isFalse();
    verify(logExporter1).shutdown();
    verify(logExporter2).shutdown();
  }
}
