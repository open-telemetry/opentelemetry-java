/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/** A SpanProcessor that only handles onStart(). */
public final class OnStartSpanProcessor implements SpanProcessor {

  private final OnStart onStart;

  private OnStartSpanProcessor(OnStart onStart) {
    this.onStart = onStart;
  }

  public static SpanProcessor create(OnStart onStart) {
    return new OnStartSpanProcessor(onStart);
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    onStart.apply(parentContext, span);
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    // nop
  }

  @Override
  public boolean isEndRequired() {
    return false;
  }

  @FunctionalInterface
  public interface OnStart {
    void apply(Context context, ReadWriteSpan span);
  }
}
