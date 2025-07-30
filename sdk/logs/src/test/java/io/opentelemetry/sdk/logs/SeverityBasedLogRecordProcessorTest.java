/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeverityBasedLogRecordProcessorTest {

  @Mock private LogRecordProcessor delegate;
  @Mock private ReadWriteLogRecord logRecord;

  private Context context;

  @BeforeEach
  void setUp() {
    context = Context.current();
    when(delegate.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(delegate.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void builder_RequiresMinimumSeverity() {
    assertThatThrownBy(() -> SeverityBasedLogRecordProcessor.builder(null, delegate))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("minimumSeverity");
  }

  @Test
  void builder_RequiresProcessor() {
    assertThatThrownBy(() -> SeverityBasedLogRecordProcessor.builder(Severity.INFO, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("delegate");
  }

  @Test
  void onEmit_SeverityMeetsMinimum_DelegatesToProcessor() {
    when(logRecord.getSeverity()).thenReturn(Severity.WARN);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN, delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_SeverityAboveMinimum_DelegatesToProcessor() {
    when(logRecord.getSeverity()).thenReturn(Severity.ERROR);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN, delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_SeverityBelowMinimum_DoesNotDelegate() {
    when(logRecord.getSeverity()).thenReturn(Severity.DEBUG);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN, delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate, never()).onEmit(any(), any());
  }

  @Test
  void onEmit_UndefinedSeverity_DoesNotDelegate() {
    when(logRecord.getSeverity()).thenReturn(Severity.UNDEFINED_SEVERITY_NUMBER);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO, delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate, never()).onEmit(any(), any());
  }

  @Test
  void onEmit_VariousSeverityLevels() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN, delegate).build();

    // Test all severity levels
    testSeverityLevel(processor, Severity.UNDEFINED_SEVERITY_NUMBER, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.TRACE, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.TRACE2, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.TRACE3, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.TRACE4, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.DEBUG, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.DEBUG2, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.DEBUG3, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.DEBUG4, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.INFO, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.INFO2, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.INFO3, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.INFO4, /* shouldDelegate= */ false);
    testSeverityLevel(processor, Severity.WARN, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.WARN2, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.WARN3, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.WARN4, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.ERROR, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.ERROR2, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.ERROR3, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.ERROR4, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.FATAL, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.FATAL2, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.FATAL3, /* shouldDelegate= */ true);
    testSeverityLevel(processor, Severity.FATAL4, /* shouldDelegate= */ true);
  }

  private void testSeverityLevel(
      SeverityBasedLogRecordProcessor processor, Severity severity, boolean shouldDelegate) {
    when(logRecord.getSeverity()).thenReturn(severity);

    processor.onEmit(context, logRecord);

    if (shouldDelegate) {
      verify(delegate).onEmit(same(context), same(logRecord));
    } else {
      verify(delegate, never()).onEmit(same(context), same(logRecord));
    }

    // Reset mock for next test
    org.mockito.Mockito.reset(delegate);
    when(delegate.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(delegate.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void shutdown_DelegatesToProcessor() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO, delegate).build();

    CompletableResultCode result = processor.shutdown();

    verify(delegate).shutdown();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_DelegatesToProcessor() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO, delegate).build();

    CompletableResultCode result = processor.forceFlush();

    verify(delegate).forceFlush();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void toString_Valid() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN, delegate).build();

    String toString = processor.toString();
    assertThat(toString).contains("SeverityBasedLogRecordProcessor");
    assertThat(toString).contains("minimumSeverity=WARN");
    assertThat(toString).contains("delegate=");
  }

  @Test
  void shutdown_ProcessorFailure() {
    when(delegate.shutdown()).thenReturn(CompletableResultCode.ofFailure());

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO, delegate).build();

    CompletableResultCode result = processor.shutdown();

    verify(delegate).shutdown();
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  void forceFlush_ProcessorFailure() {
    when(delegate.forceFlush()).thenReturn(CompletableResultCode.ofFailure());

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO, delegate).build();

    CompletableResultCode result = processor.forceFlush();

    verify(delegate).forceFlush();
    assertThat(result.isSuccess()).isFalse();
  }
}
