/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.trace.export.SpanData;
import io.opentelemetry.api.trace.Span;

class OpenTelemetryStartEndHandler implements StartEndHandler {

  private final SpanCache spanCache;

  public OpenTelemetryStartEndHandler() {
    this.spanCache = SpanCache.getInstance();
  }

  @Override
  public void onStart(RecordEventsSpanImpl ocSpan) {
    spanCache.addToCache(ocSpan);
  }

  @Override
  public void onEnd(RecordEventsSpanImpl ocSpan) {
    Span otelSpan = spanCache.removeFromCache(ocSpan);
    SpanData spanData = ocSpan.toSpanData();
    SpanConverter.mapAndAddAnnotations(otelSpan, spanData.getAnnotations().getEvents());
    SpanConverter.mapAndAddTimedEvents(otelSpan, spanData.getMessageEvents().getEvents());
    otelSpan.end();
  }
}
