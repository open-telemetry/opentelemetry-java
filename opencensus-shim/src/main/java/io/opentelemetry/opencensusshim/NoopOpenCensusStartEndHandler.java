/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;

/**
 * This exists to override the existing {@link io.opencensus.implcore.trace.StartEndHandlerImpl}
 * used by OpenCensus {@link RecordEventsSpanImpl}, which adds the span event to an event queue, and
 * adds the span to an exporter. As we do not want OpenCensus spans to be exported, we are
 * overriding the methods here to do nothing.
 */
class NoopOpenCensusStartEndHandler implements StartEndHandler {
  public NoopOpenCensusStartEndHandler() {}

  @Override
  public void onStart(RecordEventsSpanImpl ocSpan) {}

  @Override
  public void onEnd(RecordEventsSpanImpl ocSpan) {}
}
