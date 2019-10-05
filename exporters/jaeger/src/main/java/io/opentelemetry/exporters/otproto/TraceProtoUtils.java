/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.exporters.otproto;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;

/** Utilities for converting various objects to protobuf representations. */
public class TraceProtoUtils {
  private TraceProtoUtils() {}

  /**
   * Converts a SpanId into a protobuf ByteString.
   *
   * @param spanId the spanId to convert.
   * @return a ByteString representation.
   */
  @VisibleForTesting
  public static ByteString toProtoSpanId(SpanId spanId) {
    byte[] spanIdBytes = new byte[SpanId.getSize()];
    spanId.copyBytesTo(spanIdBytes, 0);
    return ByteString.copyFrom(spanIdBytes);
  }

  /**
   * Converts a TraceId into a protobuf ByteString.
   *
   * @param traceId the traceId to convert.
   * @return a ByteString representation.
   */
  @VisibleForTesting
  public static ByteString toProtoTraceId(TraceId traceId) {
    byte[] traceIdBytes = new byte[TraceId.getSize()];
    traceId.copyBytesTo(traceIdBytes, 0);
    return ByteString.copyFrom(traceIdBytes);
  }
}
