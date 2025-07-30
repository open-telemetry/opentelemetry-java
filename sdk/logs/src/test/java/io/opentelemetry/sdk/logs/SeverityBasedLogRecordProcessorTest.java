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

  @Mock private LogRecordProcessor processor1;
  @Mock private LogRecordProcessor processor2;
  @Mock private ReadWriteLogRecord logRecord;

  private Context context;

  @BeforeEach
  void setUp() {
    context = Context.current();
    when(processor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void builder_RequiresMinimumSeverity() {
    assertThatThrownBy(() -> SeverityBasedLogRecordProcessor.builder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("minimumSeverity");
  }

  @Test
  void builder_RequiresAtLeastOneProcessor() {
    assertThatThrownBy(() -> SeverityBasedLogRecordProcessor.builder(Severity.INFO).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("At least one processor must be added");
  }

  @Test
  void builder_NullProcessor() {
    assertThatThrownBy(
            () ->
                SeverityBasedLogRecordProcessor.builder(Severity.INFO)
                    .addProcessors((LogRecordProcessor) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("processor");
  }

  @Test
  void builder_NullProcessorArray() {
    assertThatThrownBy(
            () ->
                SeverityBasedLogRecordProcessor.builder(Severity.INFO)
                    .addProcessors((LogRecordProcessor[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("processors");
  }

  @Test
  void builder_NullProcessorIterable() {
    assertThatThrownBy(
            () ->
                SeverityBasedLogRecordProcessor.builder(Severity.INFO)
                    .addProcessors((Iterable<LogRecordProcessor>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("processors");
  }

  @Test
  void onEmit_SeverityMeetsMinimum_DelegatesToAllProcessors() {
    when(logRecord.getSeverity()).thenReturn(Severity.WARN);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN)
            .addProcessors(processor1, processor2)
            .build();

    processor.onEmit(context, logRecord);

    verify(processor1).onEmit(same(context), same(logRecord));
    verify(processor2).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_SeverityAboveMinimum_DelegatesToAllProcessors() {
    when(logRecord.getSeverity()).thenReturn(Severity.ERROR);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN)
            .addProcessors(processor1, processor2)
            .build();

    processor.onEmit(context, logRecord);

    verify(processor1).onEmit(same(context), same(logRecord));
    verify(processor2).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_SeverityBelowMinimum_DoesNotDelegate() {
    when(logRecord.getSeverity()).thenReturn(Severity.DEBUG);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN)
            .addProcessors(processor1, processor2)
            .build();

    processor.onEmit(context, logRecord);

    verify(processor1, never()).onEmit(any(), any());
    verify(processor2, never()).onEmit(any(), any());
  }

  @Test
  void onEmit_UndefinedSeverity_DoesNotDelegate() {
    when(logRecord.getSeverity()).thenReturn(Severity.UNDEFINED_SEVERITY_NUMBER);

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO).addProcessors(processor1).build();

    processor.onEmit(context, logRecord);

    verify(processor1, never()).onEmit(any(), any());
  }

  @Test
  void onEmit_VariousSeverityLevels() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN).addProcessors(processor1).build();

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
      verify(processor1).onEmit(same(context), same(logRecord));
    } else {
      verify(processor1, never()).onEmit(same(context), same(logRecord));
    }

    // Reset mock for next test
    org.mockito.Mockito.reset(processor1);
    when(processor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void shutdown_DelegatesToAllProcessors() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO)
            .addProcessors(processor1, processor2)
            .build();

    CompletableResultCode result = processor.shutdown();

    verify(processor1).shutdown();
    verify(processor2).shutdown();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_DelegatesToAllProcessors() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO)
            .addProcessors(processor1, processor2)
            .build();

    CompletableResultCode result = processor.forceFlush();

    verify(processor1).forceFlush();
    verify(processor2).forceFlush();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void toString_Valid() {
    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.WARN).addProcessors(processor1).build();

    String toString = processor.toString();
    assertThat(toString).contains("SeverityBasedLogRecordProcessor");
    assertThat(toString).contains("minimumSeverity=WARN");
    assertThat(toString).contains("delegate=");
  }

  @Test
  void shutdown_ProcessorFailure() {
    when(processor1.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    when(processor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO)
            .addProcessors(processor1, processor2)
            .build();

    CompletableResultCode result = processor.shutdown();

    verify(processor1).shutdown();
    verify(processor2).shutdown();
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  void forceFlush_ProcessorFailure() {
    when(processor1.forceFlush()).thenReturn(CompletableResultCode.ofFailure());
    when(processor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());

    SeverityBasedLogRecordProcessor processor =
        SeverityBasedLogRecordProcessor.builder(Severity.INFO)
            .addProcessors(processor1, processor2)
            .build();

    CompletableResultCode result = processor.forceFlush();

    verify(processor1).forceFlush();
    verify(processor2).forceFlush();
    assertThat(result.isSuccess()).isFalse();
  }
}
