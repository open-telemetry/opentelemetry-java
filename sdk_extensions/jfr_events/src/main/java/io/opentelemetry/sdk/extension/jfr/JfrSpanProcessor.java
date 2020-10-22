/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import static java.util.Objects.nonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.trace.SpanContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Span processor to create new JFR events for the Span as they are started, and commit on end.
 *
 * <p>NOTE: JfrSpanProcessor must be running synchronously to ensure that duration is correctly
 * captured.
 */
public class JfrSpanProcessor implements SpanProcessor {

  private final Map<SpanContext, SpanEvent> spanEvents = new ConcurrentHashMap<>();

  @Override
  public void onStart(ReadWriteSpan span, Context parentContext) {
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
    SpanEvent event = spanEvents.remove(rs.getSpanContext());
    if (nonNull(event) && event.shouldCommit()) {
      event.commit();
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public CompletableResultCode shutdown() {
    spanEvents.forEach((id, event) -> event.commit());
    spanEvents.clear();
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }
}
