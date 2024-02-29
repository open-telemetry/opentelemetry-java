/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/** A SpanProcessor implementation that is only capable of processing spans when they end. */
public final class OnEndSpanProcessor implements SpanProcessor {
  private final OnEnd onEnd;

  private OnEndSpanProcessor(OnEnd onEnd) {
    this.onEnd = onEnd;
  }

  static SpanProcessor create(OnEnd onEnd) {
    return new OnEndSpanProcessor(onEnd);
  }

  @Override
  public void onEnd(ReadableSpan span) {
    onEnd.apply(span);
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    // nop
  }

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @FunctionalInterface
  public interface OnEnd {
    void apply(ReadableSpan span);
  }
}
