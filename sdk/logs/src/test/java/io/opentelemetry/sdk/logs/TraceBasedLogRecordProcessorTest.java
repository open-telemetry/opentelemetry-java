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

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
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
class TraceBasedLogRecordProcessorTest {

  @Mock private LogRecordProcessor processor1;
  @Mock private LogRecordProcessor processor2;
  @Mock private ReadWriteLogRecord logRecord;

  private Context context;
  private SpanContext sampledSpanContext;
  private SpanContext notSampledSpanContext;
  private SpanContext invalidSpanContext;

  @BeforeEach
  void setUp() {
    context = Context.current();
    when(processor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(processor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());

    // Create sampled span context
    sampledSpanContext =
        SpanContext.create(
            TraceId.fromLongs(1, 2),
            SpanId.fromLong(3),
            TraceFlags.getSampled(),
            TraceState.getDefault());

    // Create not sampled span context
    notSampledSpanContext =
        SpanContext.create(
            TraceId.fromLongs(1, 2),
            SpanId.fromLong(3),
            TraceFlags.getDefault(),
            TraceState.getDefault());

    // Create invalid span context
    invalidSpanContext = SpanContext.getInvalid();
  }

  @Test
  void builder_RequiresAtLeastOneProcessor() {
    assertThatThrownBy(() -> TraceBasedLogRecordProcessor.builder().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("At least one processor must be added");
  }

  @Test
  void builder_NullProcessor() {
    assertThatThrownBy(
            () -> TraceBasedLogRecordProcessor.builder().addProcessors((LogRecordProcessor) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("processor");
  }

  @Test
  void builder_NullProcessorArray() {
    assertThatThrownBy(
            () -> TraceBasedLogRecordProcessor.builder().addProcessors((LogRecordProcessor[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("processors");
  }

  @Test
  void builder_NullProcessorIterable() {
    assertThatThrownBy(
            () ->
                TraceBasedLogRecordProcessor.builder()
                    .addProcessors((Iterable<LogRecordProcessor>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("processors");
  }

  @Test
  void onEmit_SampledSpanContext_DelegatesToAllProcessors() {
    when(logRecord.getSpanContext()).thenReturn(sampledSpanContext);

    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    processor.onEmit(context, logRecord);

    verify(processor1).onEmit(same(context), same(logRecord));
    verify(processor2).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_NotSampledSpanContext_DoesNotDelegate() {
    when(logRecord.getSpanContext()).thenReturn(notSampledSpanContext);

    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    processor.onEmit(context, logRecord);

    verify(processor1, never()).onEmit(any(), any());
    verify(processor2, never()).onEmit(any(), any());
  }

  @Test
  void onEmit_InvalidSpanContext_DelegatesToProcessor() {
    when(logRecord.getSpanContext()).thenReturn(invalidSpanContext);

    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    processor.onEmit(context, logRecord);

    verify(processor1).onEmit(same(context), same(logRecord));
    verify(processor2).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_VariousSpanContexts() {
    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1).build();

    // Test sampled span context
    testSpanContext(processor, sampledSpanContext, /* shouldDelegate= */ true);

    // Test not sampled span context
    testSpanContext(processor, notSampledSpanContext, /* shouldDelegate= */ false);

    // Test invalid span context
    testSpanContext(processor, invalidSpanContext, /* shouldDelegate= */ true);
  }

  private void testSpanContext(
      TraceBasedLogRecordProcessor processor, SpanContext spanContext, boolean shouldDelegate) {
    when(logRecord.getSpanContext()).thenReturn(spanContext);

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
    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    CompletableResultCode result = processor.shutdown();

    verify(processor1).shutdown();
    verify(processor2).shutdown();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_DelegatesToAllProcessors() {
    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    CompletableResultCode result = processor.forceFlush();

    verify(processor1).forceFlush();
    verify(processor2).forceFlush();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void toString_Valid() {
    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1).build();

    String toString = processor.toString();
    assertThat(toString).contains("TraceBasedLogRecordProcessor");
    assertThat(toString).contains("delegate=");
  }

  @Test
  void shutdown_ProcessorFailure() {
    when(processor1.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    when(processor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    CompletableResultCode result = processor.shutdown();

    verify(processor1).shutdown();
    verify(processor2).shutdown();
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  void forceFlush_ProcessorFailure() {
    when(processor1.forceFlush()).thenReturn(CompletableResultCode.ofFailure());
    when(processor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());

    TraceBasedLogRecordProcessor processor =
        TraceBasedLogRecordProcessor.builder().addProcessors(processor1, processor2).build();

    CompletableResultCode result = processor.forceFlush();

    verify(processor1).forceFlush();
    verify(processor2).forceFlush();
    assertThat(result.isSuccess()).isFalse();
  }
}
