/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Context;
import java.util.function.Consumer;

/** A SpanProcessor implementation that is only capable of processing spans when they end. */
public final class OnEndSpanProcessor implements SpanProcessor {
  private final Consumer<ReadableSpan> onEnd;

  private OnEndSpanProcessor(Consumer<ReadableSpan> onEnd) {
    this.onEnd = onEnd;
  }

  static SpanProcessor create(Consumer<ReadableSpan> onEnd) {
    return new OnEndSpanProcessor(onEnd);
  }

  @Override
  public void onEnd(ReadableSpan span) {
    onEnd.accept(span);
  }

  @Override
  public boolean isEndRequired() {
    return false;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    // nop
  }

  @Override
  public boolean isStartRequired() {
    return false;
  }
}
