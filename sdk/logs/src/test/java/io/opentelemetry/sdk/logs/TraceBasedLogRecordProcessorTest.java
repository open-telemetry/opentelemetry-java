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

  @Mock private LogRecordProcessor delegate;
  @Mock private ReadWriteLogRecord logRecord;

  private Context context;
  private SpanContext sampledSpanContext;
  private SpanContext notSampledSpanContext;
  private SpanContext invalidSpanContext;

  @BeforeEach
  void setUp() {
    context = Context.current();
    when(delegate.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(delegate.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());

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
  void builder_RequiresProcessor() {
    assertThatThrownBy(() -> TraceBasedLogRecordProcessor.builder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("delegate");
  }

  @Test
  void onEmit_SampledSpanContext_DelegatesToProcessor() {
    when(logRecord.getSpanContext()).thenReturn(sampledSpanContext);

    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_NotSampledSpanContext_DoesNotDelegate() {
    when(logRecord.getSpanContext()).thenReturn(notSampledSpanContext);

    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate, never()).onEmit(any(), any());
  }

  @Test
  void onEmit_InvalidSpanContext_DelegatesToProcessor() {
    when(logRecord.getSpanContext()).thenReturn(invalidSpanContext);

    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    processor.onEmit(context, logRecord);

    verify(delegate).onEmit(same(context), same(logRecord));
  }

  @Test
  void onEmit_VariousSpanContexts() {
    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

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
    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    CompletableResultCode result = processor.shutdown();

    verify(delegate).shutdown();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void forceFlush_DelegatesToProcessor() {
    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    CompletableResultCode result = processor.forceFlush();

    verify(delegate).forceFlush();
    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void toString_Valid() {
    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    String toString = processor.toString();
    assertThat(toString).contains("TraceBasedLogRecordProcessor");
    assertThat(toString).contains("delegate=");
  }

  @Test
  void shutdown_ProcessorFailure() {
    when(delegate.shutdown()).thenReturn(CompletableResultCode.ofFailure());

    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    CompletableResultCode result = processor.shutdown();

    verify(delegate).shutdown();
    assertThat(result.isSuccess()).isFalse();
  }

  @Test
  void forceFlush_ProcessorFailure() {
    when(delegate.forceFlush()).thenReturn(CompletableResultCode.ofFailure());

    TraceBasedLogRecordProcessor processor = TraceBasedLogRecordProcessor.builder(delegate).build();

    CompletableResultCode result = processor.forceFlush();

    verify(delegate).forceFlush();
    assertThat(result.isSuccess()).isFalse();
  }
}
