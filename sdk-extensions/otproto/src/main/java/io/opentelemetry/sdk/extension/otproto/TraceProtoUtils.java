/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.otproto;

import com.google.protobuf.ByteString;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.trace.v1.ConstantSampler;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;

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
    return TraceConfig.builder()
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
          return Sampler.alwaysOn();
        case ALWAYS_OFF:
          return Sampler.alwaysOff();
        case ALWAYS_PARENT:
          // TODO: add support.
        case UNRECOGNIZED:
          throw new IllegalArgumentException("unrecognized constant sampling samplingResult");
      }
    }
    if (traceConfigProto.hasTraceIdRatioBased()) {
      return Sampler.traceIdRatioBased(traceConfigProto.getTraceIdRatioBased().getSamplingRatio());
    }
    if (traceConfigProto.hasRateLimitingSampler()) {
      // TODO: add support for RateLimiting Sampler
    }
    throw new IllegalArgumentException("unknown sampler in the trace config proto");
  }
}
