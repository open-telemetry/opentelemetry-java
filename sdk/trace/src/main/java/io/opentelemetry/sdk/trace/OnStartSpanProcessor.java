/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Context;
import java.util.function.BiConsumer;

/** A SpanProcessor that only handles onStart(). */
public final class OnStartSpanProcessor implements SpanProcessor {

  private final BiConsumer<Context, ReadWriteSpan> onStart;

  private OnStartSpanProcessor(BiConsumer<Context, ReadWriteSpan> onStart) {
    this.onStart = onStart;
  }

  public static SpanProcessor create(BiConsumer<Context, ReadWriteSpan> onStart) {
    return new OnStartSpanProcessor(onStart);
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    this.onStart.accept(parentContext, span);
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
}
