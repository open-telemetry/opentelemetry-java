/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.otproto;

import com.google.protobuf.ByteString;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;

/** Utilities for converting various objects to protobuf representations. */
final class TraceProtoUtils {
  private TraceProtoUtils() {}

  /**
   * Converts a SpanId into a protobuf ByteString.
   *
   * @param spanId the spanId to convert.
   * @return a ByteString representation.
   */
  static ByteString toProtoSpanId(String spanId) {
    return ByteString.copyFrom(SpanId.bytesFromHex(spanId, 0));
  }

  /**
   * Converts a TraceId into a protobuf ByteString.
   *
   * @param traceId the traceId to convert.
   * @return a ByteString representation.
   */
  static ByteString toProtoTraceId(String traceId) {
    return ByteString.copyFrom(TraceId.bytesFromHex(traceId, 0));
  }
}
