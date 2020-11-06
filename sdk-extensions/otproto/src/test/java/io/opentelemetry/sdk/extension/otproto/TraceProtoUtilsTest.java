/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.otproto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.trace.v1.ConstantSampler;
import io.opentelemetry.proto.trace.v1.ConstantSampler.ConstantDecision;
import io.opentelemetry.proto.trace.v1.TraceIdRatioBased;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceProtoUtils}. */
class TraceProtoUtilsTest {
  private static final io.opentelemetry.proto.trace.v1.TraceConfig TRACE_CONFIG_PROTO =
      io.opentelemetry.proto.trace.v1.TraceConfig.newBuilder()
          .setConstantSampler(
              ConstantSampler.newBuilder().setDecision(ConstantDecision.ALWAYS_ON).build())
          .setMaxNumberOfAttributes(10)
          .setMaxNumberOfTimedEvents(9)
          .setMaxNumberOfLinks(8)
          .setMaxNumberOfAttributesPerTimedEvent(2)
          .setMaxNumberOfAttributesPerLink(1)
          .build();

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 0, 0, 0, 'b'};

  @Test
  void toProtoTraceId() {
    ByteString expected = ByteString.copyFrom(TRACE_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoTraceId(TraceId.bytesToHex(TRACE_ID_BYTES)))
        .isEqualTo(expected);
  }

  @Test
  void toProtoSpanId() {
    ByteString expected = ByteString.copyFrom(SPAN_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoSpanId(SpanId.bytesToHex(SPAN_ID_BYTES))).isEqualTo(expected);
  }

  @Test
  void traceConfigFromProto() {
    TraceConfig traceConfig = TraceProtoUtils.traceConfigFromProto(TRACE_CONFIG_PROTO);
    assertThat(traceConfig.getSampler()).isEqualTo(Sampler.alwaysOn());
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(10);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(9);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(8);
    assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(2);
    assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(1);
  }

  @Test
  void traceConfigFromProto_AlwaysOffSampler() {
    TraceConfig traceConfig =
        TraceProtoUtils.traceConfigFromProto(
            io.opentelemetry.proto.trace.v1.TraceConfig.newBuilder()
                .setConstantSampler(
                    ConstantSampler.newBuilder().setDecision(ConstantDecision.ALWAYS_OFF).build())
                .setMaxNumberOfAttributes(10)
                .setMaxNumberOfTimedEvents(9)
                .setMaxNumberOfLinks(8)
                .setMaxNumberOfAttributesPerTimedEvent(2)
                .setMaxNumberOfAttributesPerLink(1)
                .build());
    assertThat(traceConfig.getSampler()).isEqualTo(Sampler.alwaysOff());
  }

  @Test
  void traceConfigFromProto_ProbabilitySampler() {
    TraceConfig traceConfig =
        TraceProtoUtils.traceConfigFromProto(
            io.opentelemetry.proto.trace.v1.TraceConfig.newBuilder()
                .setTraceIdRatioBased(TraceIdRatioBased.newBuilder().setSamplingRatio(0.1).build())
                .setMaxNumberOfAttributes(10)
                .setMaxNumberOfTimedEvents(9)
                .setMaxNumberOfLinks(8)
                .setMaxNumberOfAttributesPerTimedEvent(2)
                .setMaxNumberOfAttributesPerLink(1)
                .build());
    assertThat(traceConfig.getSampler()).isEqualTo(Sampler.traceIdRatioBased(0.1));
  }
}
