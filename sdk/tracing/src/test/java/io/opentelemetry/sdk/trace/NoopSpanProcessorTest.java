/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link NoopSpanProcessorTest}. */
class NoopSpanProcessorTest {
  @Mock private ReadableSpan readableSpan;
  @Mock private ReadWriteSpan readWriteSpan;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void noCrash() {
    SpanProcessor noopSpanProcessor = NoopSpanProcessor.getInstance();
    noopSpanProcessor.onStart(readWriteSpan, Context.root());
    assertThat(noopSpanProcessor.isStartRequired()).isFalse();
    noopSpanProcessor.onEnd(readableSpan);
    assertThat(noopSpanProcessor.isEndRequired()).isFalse();
    noopSpanProcessor.forceFlush();
    noopSpanProcessor.shutdown();
  }
}
