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
import io.opentelemetry.proto.trace.v1.ConstantSampler;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.util.Samplers;

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

  /**
   * Returns a {@code TraceConfig} from the given proto.
   *
   * @param traceConfigProto proto format {@code TraceConfig}.
   * @return a {@code TraceConfig}.
   */
  public static TraceConfig traceConfigFromProto(
      io.opentelemetry.proto.trace.v1.TraceConfig traceConfigProto) {
    return TraceConfig.getDefault()
        .toBuilder()
        .setSampler(fromProtoSampler(traceConfigProto))
        .setMaxNumberOfAttributes((int) traceConfigProto.getMaxNumberOfAttributes())
        .setMaxNumberOfEvents((int) traceConfigProto.getMaxNumberOfTimedEvents())
        .setMaxNumberOfLinks((int) traceConfigProto.getMaxNumberOfLinks())
        .setMaxNumberOfAttributesPerEvent(
            (int) traceConfigProto.getMaxNumberOfAttributesPerTimedEvent())
        .setMaxNumberOfAttributesPerLink((int) traceConfigProto.getMaxNumberOfAttributesPerLink())
        .build();
  }

  private static Sampler fromProtoSampler(
      io.opentelemetry.proto.trace.v1.TraceConfig traceConfigProto) {
    if (traceConfigProto.hasConstantSampler()) {
      ConstantSampler constantSampler = traceConfigProto.getConstantSampler();
      switch (constantSampler.getDecision()) {
        case ALWAYS_ON:
          return Samplers.alwaysSample();
        case ALWAYS_OFF:
          return Samplers.neverSample();
        case ALWAYS_PARENT:
          // TODO: add support.
        case UNRECOGNIZED:
          throw new IllegalArgumentException("unrecognized constant sampling decision");
      }
    }
    if (traceConfigProto.hasProbabilitySampler()) {
      // TODO: add support for ProbabilitySampler
    }
    if (traceConfigProto.hasRateLimitingSampler()) {
      // TODO: add support for RateLimitingSampler
    }
    throw new IllegalArgumentException("unknown sampler in the trace config proto");
  }
}
