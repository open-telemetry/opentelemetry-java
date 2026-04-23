/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Context;
import java.util.Objects;

final class NoopSpanProcessor implements SpanProcessor {
  private static final NoopSpanProcessor INSTANCE = new NoopSpanProcessor();

  static SpanProcessor getInstance() {
    return INSTANCE;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    Objects.requireNonNull(parentContext, "parentContext");
    Objects.requireNonNull(span, "span");
  }

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    Objects.requireNonNull(span, "span");
  }

  @Override
  public boolean isEndRequired() {
    return false;
  }

  private NoopSpanProcessor() {}

  @Override
  public String toString() {
    return "NoopSpanProcessor{}";
  }
}
