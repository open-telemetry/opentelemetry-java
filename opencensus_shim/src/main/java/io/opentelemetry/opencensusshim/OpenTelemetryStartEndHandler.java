/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.trace.export.SpanData;
import io.opentelemetry.api.trace.Span;

public class OpenTelemetryStartEndHandler implements StartEndHandler {

  private final SpanCache spanCache;

  public OpenTelemetryStartEndHandler() {
    this.spanCache = SpanCache.getInstance();
  }

  @Override
  public void onStart(RecordEventsSpanImpl ocSpan) {
    spanCache.toOtelSpan(ocSpan);
  }

  @Override
  public void onEnd(RecordEventsSpanImpl ocSpan) {
    Span span = spanCache.toOtelSpan(ocSpan);
    SpanData spanData = ocSpan.toSpanData();
    spanCache.removeFromCache(ocSpan);
    SpanConverter.mapAndAddAnnotations(span, spanData.getAnnotations().getEvents());
    SpanConverter.mapAndAddTimedEvents(span, spanData.getMessageEvents().getEvents());
    span.end();
  }
}
