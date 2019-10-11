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

package io.opentelemetry.exporters.otprotocol;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
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
  public static ByteString toProtoTraceId(TraceId traceId) {
    byte[] traceIdBytes = new byte[TraceId.getSize()];
    traceId.copyBytesTo(traceIdBytes, 0);
    return ByteString.copyFrom(traceIdBytes);
  }

  /**
   * Converts a opentelemetry Timestamp into a protobuf Timestamp.
   *
   * @param timestamp the opentelemetry Timestamp to convert.
   * @return the protobuf Timestamp representation.
   */
  public static Timestamp toProtoTimestamp(io.opentelemetry.common.Timestamp timestamp) {
    return Timestamp.newBuilder()
        .setNanos(timestamp.getNanos())
        .setSeconds(timestamp.getSeconds())
        .build();
  }
}
