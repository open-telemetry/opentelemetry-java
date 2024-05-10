/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.oltp.data;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import javax.annotation.concurrent.Immutable;

/**
 * A connection from a profile Sample to a trace Span.
 *
 * @see "pprofextended.proto::Link"
 */
@Immutable
public interface LinkData {

  /**
   * Returns a unique identifier of a trace that this linked span is part of as 32 character
   * lowercase hex String.
   */
  String getTraceId();

  /** Returns the trace identifier as 16-byte array. */
  default byte[] getTraceIdBytes() {
    return OtelEncodingUtils.bytesFromBase16(getTraceId(), TraceId.getLength());
  }

  /** Returns a unique identifier for the linked span, as 16 character lowercase hex String. */
  String getSpanId();

  /** Returns a unique identifier for the linked span, as an 8-byte array. */
  default byte[] getSpanIdBytes() {
    return OtelEncodingUtils.bytesFromBase16(getSpanId(), SpanId.getLength());
  }
}
