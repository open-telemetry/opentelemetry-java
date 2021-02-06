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

  private final String operationName;
  private final String traceId;
  private final String spanId;
  private final String parentId;

  SpanEvent(SpanData spanData) {
    this.operationName = spanData.getName();
    this.traceId = spanData.getTraceIdHex();
    this.spanId = spanData.getSpanIdHex();
    this.parentId = spanData.getParentSpanIdHex();
  }

  @Label("Operation Name")
  public String getOperationName() {
    return operationName;
  }

  @Label("Trace Id")
  public String getTraceId() {
    return traceId;
  }

  @Label("Span Id")
  public String getSpanId() {
    return spanId;
  }

  @Label("Parent Id")
  public String getParentId() {
    return parentId;
  }
}
