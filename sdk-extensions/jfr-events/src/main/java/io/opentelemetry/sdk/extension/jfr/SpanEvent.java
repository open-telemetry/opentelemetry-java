/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.sdk.trace.data.SpanData;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Label("Span")
@Name("io.opentelemetry.trace.Span")
@Category("Open Telemetry Tracing")
@Description("Open Telemetry trace event corresponding to a span.")
class SpanEvent extends Event {

  @Label("Operation Name")
  private final String operationName;

  @Label("Trace Id")
  private final String traceId;

  @Label("Span Id")
  private final String spanId;

  @Label("Parent Id")
  private final String parentId;

  SpanEvent(SpanData spanData) {
    this.operationName = spanData.getName();
    this.traceId = spanData.getTraceId();
    this.spanId = spanData.getSpanId();
    this.parentId = spanData.getParentSpanId();
  }

  public String getOperationName() {
    return operationName;
  }

  public String getTraceId() {
    return traceId;
  }

  public String getSpanId() {
    return spanId;
  }

  public String getParentId() {
    return parentId;
  }
}
