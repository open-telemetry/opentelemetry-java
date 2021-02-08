/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.api.trace.SpanContext;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("io.opentelemetry.context.Scope")
@Label("Scope")
@Category("Open Telemetry Tracing")
@Description(
    "Open Telemetry trace event corresponding to the span currently "
        + "in scope/active on this thread.")
class ScopeEvent extends Event {

  private final String traceId;
  private final String spanId;

  ScopeEvent(SpanContext spanContext) {
    this.traceId = spanContext.getTraceId();
    this.spanId = spanContext.getSpanId();
  }

  @Label("Trace Id")
  public String getTraceId() {
    return traceId;
  }

  @Label("Span Id")
  public String getSpanId() {
    return spanId;
  }
}
