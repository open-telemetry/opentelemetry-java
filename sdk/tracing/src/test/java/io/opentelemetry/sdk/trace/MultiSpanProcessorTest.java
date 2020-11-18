/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MultiSpanProcessorTest {
  @Mock private SpanProcessor spanProcessor1;
  @Mock private SpanProcessor spanProcessor2;
  @Mock private ReadableSpan readableSpan;
  @Mock private ReadWriteSpan readWriteSpan;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    when(spanProcessor1.isStartRequired()).thenReturn(true);
    when(spanProcessor1.isEndRequired()).thenReturn(true);
    when(spanProcessor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanProcessor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanProcessor2.isStartRequired()).thenReturn(true);
    when(spanProcessor2.isEndRequired()).thenReturn(true);
    when(spanProcessor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanProcessor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void empty() {
    SpanProcessor multiSpanProcessor = SpanProcessor.delegating(Collections.emptyList());
    multiSpanProcessor.onStart(Context.root(), readWriteSpan);
    multiSpanProcessor.onEnd(readableSpan);
    multiSpanProcessor.shutdown();
  }

  @Test
  void oneSpanProcessor() {
    SpanProcessor multiSpanProcessor =
        SpanProcessor.delegating(Collections.singletonList(spanProcessor1));
    multiSpanProcessor.onStart(Context.root(), readWriteSpan);
    verify(spanProcessor1).onStart(same(Context.root()), same(readWriteSpan));

    multiSpanProcessor.onEnd(readableSpan);
    verify(spanProcessor1).onEnd(same(readableSpan));

    multiSpanProcessor.forceFlush();
    verify(spanProcessor1).forceFlush();

    multiSpanProcessor.shutdown();
    verify(spanProcessor1).shutdown();
  }

  @Test
  void oneSpanProcessor_NoRequirements() {
    when(spanProcessor1.isStartRequired()).thenReturn(false);
    when(spanProcessor1.isEndRequired()).thenReturn(false);
    SpanProcessor multiSpanProcessor =
        SpanProcessor.delegating(Collections.singletonList(spanProcessor1));

    verify(spanProcessor1).isStartRequired();
    verify(spanProcessor1).isEndRequired();

    assertThat(multiSpanProcessor.isStartRequired()).isFalse();
    assertThat(multiSpanProcessor.isEndRequired()).isFalse();

    multiSpanProcessor.onStart(Context.root(), readWriteSpan);
    verifyNoMoreInteractions(spanProcessor1);

    multiSpanProcessor.onEnd(readableSpan);
    verifyNoMoreInteractions(spanProcessor1);

    multiSpanProcessor.forceFlush();
    verify(spanProcessor1).forceFlush();

    multiSpanProcessor.shutdown();
    verify(spanProcessor1).shutdown();
  }

  @Test
  void twoSpanProcessor() {
    SpanProcessor multiSpanProcessor =
        SpanProcessor.delegating(Arrays.asList(spanProcessor1, spanProcessor2));
    multiSpanProcessor.onStart(Context.root(), readWriteSpan);
    verify(spanProcessor1).onStart(same(Context.root()), same(readWriteSpan));
    verify(spanProcessor2).onStart(same(Context.root()), same(readWriteSpan));

    multiSpanProcessor.onEnd(readableSpan);
    verify(spanProcessor1).onEnd(same(readableSpan));
    verify(spanProcessor2).onEnd(same(readableSpan));

    multiSpanProcessor.forceFlush();
    verify(spanProcessor1).forceFlush();
    verify(spanProcessor2).forceFlush();

    multiSpanProcessor.shutdown();
    verify(spanProcessor1).shutdown();
    verify(spanProcessor2).shutdown();
  }

  @Test
  void twoSpanProcessor_DifferentRequirements() {
    when(spanProcessor1.isEndRequired()).thenReturn(false);
    when(spanProcessor2.isStartRequired()).thenReturn(false);
    SpanProcessor multiSpanProcessor =
        SpanProcessor.delegating(Arrays.asList(spanProcessor1, spanProcessor2));

    assertThat(multiSpanProcessor.isStartRequired()).isTrue();
    assertThat(multiSpanProcessor.isEndRequired()).isTrue();

    multiSpanProcessor.onStart(Context.root(), readWriteSpan);
    verify(spanProcessor1).onStart(same(Context.root()), same(readWriteSpan));
    verify(spanProcessor2, times(0)).onStart(any(Context.class), any(ReadWriteSpan.class));

    multiSpanProcessor.onEnd(readableSpan);
    verify(spanProcessor1, times(0)).onEnd(any(ReadableSpan.class));
    verify(spanProcessor2).onEnd(same(readableSpan));

    multiSpanProcessor.forceFlush();
    verify(spanProcessor1).forceFlush();
    verify(spanProcessor2).forceFlush();

    multiSpanProcessor.shutdown();
    verify(spanProcessor1).shutdown();
    verify(spanProcessor2).shutdown();
  }
}
