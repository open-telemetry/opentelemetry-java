/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * Span processor to create new JFR events for the Span as they are started, and commit on end.
 *
 * <p>NOTE: The JfrSpanProcessor measures the timing of spans, avoid if possible to wrap it with any
 * other SpanProcessor which may affect timings. When possible, register it first before any other
 * processors to allow the most accurate measurements.
 */
public class JfrSpanProcessor implements SpanProcessor {

  private final WeakConcurrentMap<SpanContext, SpanEvent> spanEvents =
      new WeakConcurrentMap.WithInlinedExpunction<>();

  private volatile boolean closed;

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    if (closed) {
      return;
    }
    if (span.getSpanContext().isValid()) {
      SpanEvent event = new SpanEvent(span.toSpanData());
      event.begin();
      spanEvents.put(span.getSpanContext(), event);
    }
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan rs) {
    if (closed) {
      return;
    }
    SpanEvent event = spanEvents.remove(rs.getSpanContext());
    if (event != null && event.shouldCommit()) {
      event.commit();
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public CompletableResultCode shutdown() {
    closed = true;
    return CompletableResultCode.ofSuccess();
  }
}
