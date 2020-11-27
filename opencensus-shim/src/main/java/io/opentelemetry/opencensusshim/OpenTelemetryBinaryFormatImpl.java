/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.internal.Utils;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;

public class OpenTelemetryBinaryFormatImpl extends BinaryFormat {
  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  private static final byte VERSION_ID = 0;
  private static final int VERSION_ID_OFFSET = 0;
  // The version_id/field_id size in bytes.
  private static final byte ID_SIZE = 1;
  private static final byte TRACE_ID_FIELD_ID = 0;
  private static final int TRACE_FLAGS_SIZE = 1;

  @VisibleForTesting static final int TRACE_ID_FIELD_ID_OFFSET = VERSION_ID_OFFSET + ID_SIZE;

  private static final int TRACE_ID_OFFSET = TRACE_ID_FIELD_ID_OFFSET + ID_SIZE;
  private static final byte SPAN_ID_FIELD_ID = 1;

  @VisibleForTesting static final int SPAN_ID_FIELD_ID_OFFSET = TRACE_ID_OFFSET + TraceId.getSize();

  private static final int SPAN_ID_OFFSET = SPAN_ID_FIELD_ID_OFFSET + ID_SIZE;
  private static final byte TRACE_OPTION_FIELD_ID = 2;

  private static final int TRACE_OPTION_FIELD_ID_OFFSET = SPAN_ID_OFFSET + SpanId.getSize();

  private static final int TRACE_OPTIONS_OFFSET = TRACE_OPTION_FIELD_ID_OFFSET + ID_SIZE;
  /** Version, Trace and Span IDs are required fields. */
  private static final int REQUIRED_FORMAT_LENGTH =
      3 * ID_SIZE + TraceId.getSize() + SpanId.getSize();

  /** Use {@link TraceFlags#getDefault()} unless its optional field is present. */
  private static final int ALL_FORMAT_LENGTH = REQUIRED_FORMAT_LENGTH + ID_SIZE + TRACE_FLAGS_SIZE;

  @Override
  public byte[] toByteArray(SpanContext spanContext) {
    Utils.checkNotNull(spanContext, "spanContext");
    io.opentelemetry.api.trace.SpanContext otelSpanContext =
        SpanConverter.mapSpanContext(spanContext);
    byte[] bytes = new byte[ALL_FORMAT_LENGTH];
    bytes[VERSION_ID_OFFSET] = VERSION_ID;
    bytes[TRACE_ID_FIELD_ID_OFFSET] = TRACE_ID_FIELD_ID;
    spanContext.getTraceId().copyBytesTo(bytes, TRACE_ID_OFFSET);
    bytes[SPAN_ID_FIELD_ID_OFFSET] = SPAN_ID_FIELD_ID;
    spanContext.getSpanId().copyBytesTo(bytes, SPAN_ID_OFFSET);
    bytes[TRACE_OPTION_FIELD_ID_OFFSET] = TRACE_OPTION_FIELD_ID;
    bytes[TRACE_OPTIONS_OFFSET] = otelSpanContext.getTraceFlags();
    return bytes;
  }

  @Override
  public SpanContext fromByteArray(byte[] bytes) {
    Utils.checkNotNull(bytes, "bytes");
    if (bytes.length == 0 || bytes[0] != VERSION_ID) {
      throw new IllegalArgumentException("Unsupported version.");
    }
    if (bytes.length < REQUIRED_FORMAT_LENGTH) {
      throw new IllegalArgumentException("Invalid input: truncated");
    }
    io.opencensus.trace.TraceId traceId;
    io.opencensus.trace.SpanId spanId;
    byte traceFlags = TraceFlags.getDefault();
    int pos = 1;
    if (bytes[pos] == TRACE_ID_FIELD_ID) {
      traceId = io.opencensus.trace.TraceId.fromBytes(bytes, pos + ID_SIZE);
      pos += ID_SIZE + TraceId.getSize();
    } else {
      throw new IllegalArgumentException("Invalid input: expected trace ID at offset " + pos);
    }
    if (bytes[pos] == SPAN_ID_FIELD_ID) {
      spanId = io.opencensus.trace.SpanId.fromBytes(bytes, pos + ID_SIZE);
      pos += ID_SIZE + SpanId.getSize();
    } else {
      throw new IllegalArgumentException("Invalid input: expected span ID at offset " + pos);
    }
    if (bytes.length > pos && bytes[pos] == TRACE_OPTION_FIELD_ID) {
      if (bytes.length < ALL_FORMAT_LENGTH) {
        throw new IllegalArgumentException("Invalid input: truncated");
      }
      traceFlags = bytes[pos + ID_SIZE];
    }
    io.opentelemetry.api.trace.SpanContext otelSpanContext =
        io.opentelemetry.api.trace.SpanContext.create(
            traceId.toLowerBase16(), spanId.toLowerBase16(), traceFlags, TRACE_STATE_DEFAULT);
    return SpanConverter.mapSpanContext(otelSpanContext);
  }
}
