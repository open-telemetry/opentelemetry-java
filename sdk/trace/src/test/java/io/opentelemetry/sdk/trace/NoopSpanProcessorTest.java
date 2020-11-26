/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoopSpanProcessorTest {
  @Mock private ReadableSpan readableSpan;
  @Mock private ReadWriteSpan readWriteSpan;

  @Test
  void noCrash() {
    SpanProcessor noopSpanProcessor = NoopSpanProcessor.getInstance();
    noopSpanProcessor.onStart(Context.root(), readWriteSpan);
    assertThat(noopSpanProcessor.isStartRequired()).isFalse();
    noopSpanProcessor.onEnd(readableSpan);
    assertThat(noopSpanProcessor.isEndRequired()).isFalse();
    noopSpanProcessor.forceFlush();
    noopSpanProcessor.shutdown();
  }
}
