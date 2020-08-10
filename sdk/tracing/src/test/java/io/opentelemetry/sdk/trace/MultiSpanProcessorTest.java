/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    when(spanProcessor1.isStartRequired()).thenReturn(true);
    when(spanProcessor1.isEndRequired()).thenReturn(true);
    when(spanProcessor2.isStartRequired()).thenReturn(true);
    when(spanProcessor2.isEndRequired()).thenReturn(true);
  }

  @Test
  void empty() {
    SpanProcessor multiSpanProcessor = MultiSpanProcessor.create(Collections.emptyList());
    multiSpanProcessor.onStart(readableSpan);
    multiSpanProcessor.onEnd(readableSpan);
    multiSpanProcessor.shutdown();
  }

  @Test
  void oneSpanProcessor() {
    SpanProcessor multiSpanProcessor =
        MultiSpanProcessor.create(Collections.singletonList(spanProcessor1));
    multiSpanProcessor.onStart(readableSpan);
    verify(spanProcessor1).onStart(same(readableSpan));

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
        MultiSpanProcessor.create(Collections.singletonList(spanProcessor1));

    verify(spanProcessor1).isStartRequired();
    verify(spanProcessor1).isEndRequired();

    assertThat(multiSpanProcessor.isStartRequired()).isFalse();
    assertThat(multiSpanProcessor.isEndRequired()).isFalse();

    multiSpanProcessor.onStart(readableSpan);
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
        MultiSpanProcessor.create(Arrays.asList(spanProcessor1, spanProcessor2));
    multiSpanProcessor.onStart(readableSpan);
    verify(spanProcessor1).onStart(same(readableSpan));
    verify(spanProcessor2).onStart(same(readableSpan));

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
        MultiSpanProcessor.create(Arrays.asList(spanProcessor1, spanProcessor2));

    assertThat(multiSpanProcessor.isStartRequired()).isTrue();
    assertThat(multiSpanProcessor.isEndRequired()).isTrue();

    multiSpanProcessor.onStart(readableSpan);
    verify(spanProcessor1).onStart(same(readableSpan));
    verify(spanProcessor2, times(0)).onStart(any(ReadableSpan.class));

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
