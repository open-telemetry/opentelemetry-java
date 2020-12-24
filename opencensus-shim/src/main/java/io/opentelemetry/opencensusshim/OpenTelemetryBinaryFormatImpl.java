/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opencensus.internal.Utils.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.SpanContextParseException;

/**
 * A copy of io.opencensus.implcore.trace.propagation.BinaryFormatImpl. OpenCensus's code for
 * BinaryFormat is in the implementation, not API. Since we don't want to depend on the
 * implementation for shim users, we reimplement it here.
 */
class OpenTelemetryBinaryFormatImpl extends BinaryFormat {
  private static final Tracestate TRACESTATE_DEFAULT = Tracestate.builder().build();
  private static final byte VERSION_ID = 0;
  private static final int VERSION_ID_OFFSET = 0;
  // The version_id/field_id size in bytes.
  private static final byte ID_SIZE = 1;
  private static final byte TRACE_ID_FIELD_ID = 0;

  @VisibleForTesting static final int TRACE_ID_FIELD_ID_OFFSET = VERSION_ID_OFFSET + ID_SIZE;

  private static final int TRACE_ID_OFFSET = TRACE_ID_FIELD_ID_OFFSET + ID_SIZE;
  private static final byte SPAN_ID_FIELD_ID = 1;

  @VisibleForTesting static final int SPAN_ID_FIELD_ID_OFFSET = TRACE_ID_OFFSET + TraceId.SIZE;

  private static final int SPAN_ID_OFFSET = SPAN_ID_FIELD_ID_OFFSET + ID_SIZE;
  private static final byte TRACE_OPTION_FIELD_ID = 2;

  @VisibleForTesting static final int TRACE_OPTION_FIELD_ID_OFFSET = SPAN_ID_OFFSET + SpanId.SIZE;

  private static final int TRACE_OPTIONS_OFFSET = TRACE_OPTION_FIELD_ID_OFFSET + ID_SIZE;
  /** Version, Trace and Span IDs are required fields. */
  private static final int REQUIRED_FORMAT_LENGTH = 3 * ID_SIZE + TraceId.SIZE + SpanId.SIZE;
  /** Use {@link TraceOptions#DEFAULT} unless its optional field is present. */
  private static final int ALL_FORMAT_LENGTH = REQUIRED_FORMAT_LENGTH + ID_SIZE + TraceOptions.SIZE;

  @Override
  public byte[] toByteArray(SpanContext spanContext) {
    checkNotNull(spanContext, "spanContext");
    byte[] bytes = new byte[ALL_FORMAT_LENGTH];
    bytes[VERSION_ID_OFFSET] = VERSION_ID;
    bytes[TRACE_ID_FIELD_ID_OFFSET] = TRACE_ID_FIELD_ID;
    spanContext.getTraceId().copyBytesTo(bytes, TRACE_ID_OFFSET);
    bytes[SPAN_ID_FIELD_ID_OFFSET] = SPAN_ID_FIELD_ID;
    spanContext.getSpanId().copyBytesTo(bytes, SPAN_ID_OFFSET);
    bytes[TRACE_OPTION_FIELD_ID_OFFSET] = TRACE_OPTION_FIELD_ID;
    spanContext.getTraceOptions().copyBytesTo(bytes, TRACE_OPTIONS_OFFSET);
    return bytes;
  }

  @Override
  public SpanContext fromByteArray(byte[] bytes) throws SpanContextParseException {
    checkNotNull(bytes, "bytes");
    if (bytes.length == 0 || bytes[0] != VERSION_ID) {
      throw new SpanContextParseException("Unsupported version.");
    }
    if (bytes.length < REQUIRED_FORMAT_LENGTH) {
      throw new SpanContextParseException("Invalid input: truncated");
    }
    TraceId traceId;
    SpanId spanId;
    TraceOptions traceOptions = TraceOptions.DEFAULT;
    int pos = 1;
    if (bytes[pos] == TRACE_ID_FIELD_ID) {
      traceId = TraceId.fromBytes(bytes, pos + ID_SIZE);
      pos += ID_SIZE + TraceId.SIZE;
    } else {
      throw new SpanContextParseException("Invalid input: expected trace ID at offset " + pos);
    }
    if (bytes[pos] == SPAN_ID_FIELD_ID) {
      spanId = SpanId.fromBytes(bytes, pos + ID_SIZE);
      pos += ID_SIZE + SpanId.SIZE;
    } else {
      throw new SpanContextParseException("Invalid input: expected span ID at offset " + pos);
    }
    // Check to see if we are long enough to include an options field, and also that the next field
    // is an options field. Per spec we simply stop parsing at first unknown field instead of
    // failing.
    if (bytes.length > pos && bytes[pos] == TRACE_OPTION_FIELD_ID) {
      if (bytes.length < ALL_FORMAT_LENGTH) {
        throw new SpanContextParseException("Invalid input: truncated");
      }
      traceOptions = TraceOptions.fromByte(bytes[pos + ID_SIZE]);
    }
    return SpanContext.create(traceId, spanId, traceOptions, TRACESTATE_DEFAULT);
  }
}
