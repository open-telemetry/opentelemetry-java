/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Context;

final class NoopSpanProcessor implements SpanProcessor {
  private static final NoopSpanProcessor INSTANCE = new NoopSpanProcessor();

  static SpanProcessor getInstance() {
    return INSTANCE;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {}

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {}

  @Override
  public boolean isEndRequired() {
    return false;
  }

  private NoopSpanProcessor() {}
}
