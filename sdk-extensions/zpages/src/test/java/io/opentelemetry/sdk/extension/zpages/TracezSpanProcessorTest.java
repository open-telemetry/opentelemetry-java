/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for {@link TracezSpanProcessor}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TracezSpanProcessorTest {
  private static final String SPAN_NAME = "span";
  private static final SpanContext SAMPLED_SPAN_CONTEXT =
      SpanContext.create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getSampled(),
          TraceState.getDefault());
  private static final SpanContext NOT_SAMPLED_SPAN_CONTEXT = SpanContext.getInvalid();
  private static final StatusData SPAN_STATUS = StatusData.error();

  private static void assertSpanCacheSizes(
      TracezSpanProcessor spanProcessor, int runningSpanCacheSize, int completedSpanCacheSize) {
    Collection<ReadableSpan> runningSpans = spanProcessor.getRunningSpans();
    Collection<ReadableSpan> completedSpans = spanProcessor.getCompletedSpans();
    assertThat(runningSpans.size()).isEqualTo(runningSpanCacheSize);
    assertThat(completedSpans.size()).isEqualTo(completedSpanCacheSize);
  }

  @Mock private ReadableSpan readableSpan;
  @Mock private ReadWriteSpan readWriteSpan;
  @Mock private SpanData spanData;

  @Test
  void onStart_sampledSpan_inCache() {
    TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
    /* Return a sampled span, which should be added to the running cache by default */
    when(readWriteSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    spanProcessor.onStart(Context.root(), readWriteSpan);
    assertSpanCacheSizes(spanProcessor, 1, 0);
  }

  @Test
  void onEnd_sampledSpan_inCache() {
    TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
    /* Return a sampled span, which should be added to the completed cache upon ending */
    when(readWriteSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readWriteSpan.getName()).thenReturn(SPAN_NAME);
    spanProcessor.onStart(Context.root(), readWriteSpan);

    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.getName()).thenReturn(SPAN_NAME);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    when(spanData.getStatus()).thenReturn(SPAN_STATUS);
    spanProcessor.onEnd(readableSpan);
    assertSpanCacheSizes(spanProcessor, 0, 1);
  }

  @Test
  void onStart_notSampledSpan_inCache() {
    TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
    /* Return a non-sampled span, which should not be added to the running cache by default */
    when(readWriteSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    spanProcessor.onStart(Context.root(), readWriteSpan);
    assertSpanCacheSizes(spanProcessor, 1, 0);
  }

  @Test
  void onEnd_notSampledSpan_notInCache() {
    TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
    /* Return a non-sampled span, which should not be added to the running cache by default */
    when(readWriteSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    spanProcessor.onStart(Context.root(), readWriteSpan);
    spanProcessor.onEnd(readableSpan);
    assertSpanCacheSizes(spanProcessor, 0, 0);
  }
}
