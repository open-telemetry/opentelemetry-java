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

package io.opentelemetry.sdk.extensions.otproto;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.trace.v1.ConstantSampler;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;

/** Utilities for converting various objects to protobuf representations. */
public final class TraceProtoUtils {
  private TraceProtoUtils() {}

  /**
   * Converts a SpanId into a protobuf ByteString.
   *
   * @param spanId the spanId to convert.
   * @return a ByteString representation.
   */
  public static ByteString toProtoSpanId(String spanId) {
    return ByteString.copyFrom(SpanId.bytesFromHex(spanId, 0));
  }

  /**
   * Converts a TraceId into a protobuf ByteString.
   *
   * @param traceId the traceId to convert.
   * @return a ByteString representation.
   */
  public static ByteString toProtoTraceId(String traceId) {
    return ByteString.copyFrom(TraceId.bytesFromHex(traceId, 0));
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
          return Samplers.alwaysOn();
        case ALWAYS_OFF:
          return Samplers.alwaysOff();
        case ALWAYS_PARENT:
          // TODO: add support.
        case UNRECOGNIZED:
          throw new IllegalArgumentException("unrecognized constant sampling samplingResult");
      }
    }
    if (traceConfigProto.hasTraceIdRatioBased()) {
      return Samplers.traceIdRatioBased(traceConfigProto.getTraceIdRatioBased().getSamplingRatio());
    }
    if (traceConfigProto.hasRateLimitingSampler()) {
      // TODO: add support for RateLimiting Sampler
    }
    throw new IllegalArgumentException("unknown sampler in the trace config proto");
  }
}
