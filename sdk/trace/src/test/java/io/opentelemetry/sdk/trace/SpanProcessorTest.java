/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.context.Context;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SpanProcessorTest {

  @Test
  void startOnly() {
    AtomicReference<Context> seenContext = new AtomicReference<>();
    AtomicReference<ReadWriteSpan> seenSpan = new AtomicReference<>();
    Context context = mock(Context.class);
    ReadWriteSpan inputSpan = mock(ReadWriteSpan.class);

    SpanProcessor processor =
        SpanProcessor.startOnly(
            (ctx, span) -> {
              seenContext.set(ctx);
              seenSpan.set(span);
            });

    assertThat(processor.isStartRequired()).isTrue();
    assertThat(processor.isEndRequired()).isFalse();
    processor.onStart(context, inputSpan);
    assertThat(seenContext.get()).isSameAs(context);
    assertThat(seenSpan.get()).isSameAs(inputSpan);
  }

  @Test
  void endOnly() {
    AtomicReference<ReadableSpan> seenSpan = new AtomicReference<>();
    ReadWriteSpan inputSpan = mock(ReadWriteSpan.class);

    SpanProcessor processor = SpanProcessor.endOnly(seenSpan::set);

    assertThat(processor.isStartRequired()).isFalse();
    assertThat(processor.isEndRequired()).isTrue();
    processor.onEnd(inputSpan);
    assertThat(seenSpan.get()).isSameAs(inputSpan);
  }
}
