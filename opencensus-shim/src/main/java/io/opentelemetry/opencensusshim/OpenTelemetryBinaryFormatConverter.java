/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opencensusshim;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;

/**
 * Converts the OpenCensus implementation of the {@link BinaryFormat} to the OpenTelemetry {@link
 * SpanContext}.
 *
 * <p>OpenCensus BinaryFormat format:
 *
 * <ul>
 *   <li>Binary value: &lt;version_id&gt;&lt;version_format&gt;
 *   <li>version_id: 1-byte representing the version id.
 *   <li>For version_id = 0:
 *       <ul>
 *         <li>version_format: &lt;field&gt;&lt;field&gt;
 *         <li>field_format: &lt;field_id&gt;&lt;field_format&gt;
 *         <li>Fields:
 *             <ul>
 *               <li>TraceId: (field_id = 0, len = 16, default = &#34;0000000000000000&#34;) -
 *                   16-byte array representing the trace_id.
 *               <li>SpanId: (field_id = 1, len = 8, default = &#34;00000000&#34;) - 8-byte array
 *                   representing the span_id.
 *               <li>TraceOptions: (field_id = 2, len = 1, default = &#34;0&#34;) - 1-byte array
 *                   representing the trace_options.
 *             </ul>
 *         <li>Fields MUST be encoded using the field id order (smaller to higher).
 *         <li>Valid value example:
 *             <ul>
 *               <li>{0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97,
 *                   98, 99, 100, 101, 102, 103, 104, 2, 1}
 *               <li>version_id = 0;
 *               <li>trace_id = {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}
 *               <li>span_id = {97, 98, 99, 100, 101, 102, 103, 104};
 *               <li>trace_options = {1};
 *             </ul>
 *       </ul>
 * </ul>
 */
public class OpenTelemetryBinaryFormatConverter {
  private static final byte VERSION_ID = 0;
  // The version_id/field_id size in bytes.
  private static final byte ID_SIZE = 1;
  private static final byte TRACE_ID_FIELD_ID = 0;

  private static final byte SPAN_ID_FIELD_ID = 1;
  private static final byte TRACE_OPTION_FIELD_ID = 2;

  /** Version, Trace and Span IDs are required fields. */
  private static final int REQUIRED_FORMAT_LENGTH = 3 * ID_SIZE + TraceId.SIZE + SpanId.SIZE;
  /** Use {@link TraceOptions#DEFAULT} unless its optional field is present. */
  private static final int ALL_FORMAT_LENGTH = REQUIRED_FORMAT_LENGTH + ID_SIZE + TraceOptions.SIZE;

  /**
   * Converts the OpenCensus implementation of the {@link BinaryFormat} to the OpenTelemetry {@link
   * SpanContext}.
   *
   * @param bytes byte representation of {@link io.opencensus.trace.SpanContext}
   * @return OpenTelemetry {@link SpanContext}
   */
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
    return SpanContext.create(
        traceId.toLowerBase16(),
        spanId.toLowerBase16(),
        traceOptions.isSampled() ? TraceFlags.getSampled() : TraceFlags.getDefault(),
        TraceState.getDefault());
  }
}
