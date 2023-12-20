/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OnEndSpanProcessorTest {

  @Test
  void endOnly() {
    AtomicReference<ReadableSpan> seenSpan = new AtomicReference<>();
    ReadWriteSpan inputSpan = mock(ReadWriteSpan.class);

    SpanProcessor processor = OnEndSpanProcessor.create(seenSpan::set);

    assertThat(processor.isStartRequired()).isFalse();
    assertThat(processor.isEndRequired()).isTrue();
    processor.onEnd(inputSpan);
    assertThat(seenSpan.get()).isSameAs(inputSpan);
  }
}
