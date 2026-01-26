/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link AlwaysRecordSampler}. */
class AlwaysRecordSamplerTest {

  // Mocks
  private Sampler mockSampler;

  private AlwaysRecordSampler sampler;

  @BeforeEach
  void setUpSamplers() {
    mockSampler = mock(Sampler.class);
    sampler = AlwaysRecordSampler.create(mockSampler);
  }

  @Test
  void getDescription() {
    when(mockSampler.getDescription()).thenReturn("mockDescription");
    assertThat(sampler.getDescription()).isEqualTo("AlwaysRecordSampler{mockDescription}");
  }

  private static Stream<Arguments> expectedSamplingDecisionArgs() {
    return Stream.of(
        Arguments.of(SamplingDecision.RECORD_AND_SAMPLE, SamplingDecision.RECORD_AND_SAMPLE),
        Arguments.of(SamplingDecision.RECORD_ONLY, SamplingDecision.RECORD_ONLY),
        Arguments.of(SamplingDecision.DROP, SamplingDecision.RECORD_ONLY));
  }

  @ParameterizedTest
  @MethodSource("expectedSamplingDecisionArgs")
  void shouldSampleReturnsExpectedDecision(
      SamplingDecision rootDecision, SamplingDecision expectedDecision) {
    SamplingResult rootResult = buildRootSamplingResult(rootDecision);
    when(mockSampler.shouldSample(any(), anyString(), anyString(), any(), any(), any()))
        .thenReturn(rootResult);
    SamplingResult actualResult =
        sampler.shouldSample(
            Context.current(),
            TraceId.fromLongs(1, 2),
            "name",
            SpanKind.CLIENT,
            Attributes.empty(),
            Collections.emptyList());

    if (rootDecision.equals(expectedDecision)) {
      assertThat(actualResult).isEqualTo(rootResult);
      assertThat(actualResult.getDecision()).isEqualTo(rootDecision);
    } else {
      assertThat(actualResult).isNotEqualTo(rootResult);
      assertThat(actualResult.getDecision()).isEqualTo(expectedDecision);
    }

    assertThat(actualResult.getAttributes()).isEqualTo(rootResult.getAttributes());
    TraceState traceState = TraceState.builder().build();
    assertThat(actualResult.getUpdatedTraceState(traceState))
        .isEqualTo(rootResult.getUpdatedTraceState(traceState));
  }

  private static SamplingResult buildRootSamplingResult(SamplingDecision samplingDecision) {
    return new SamplingResult() {
      @Override
      public SamplingDecision getDecision() {
        return samplingDecision;
      }

      @Override
      public Attributes getAttributes() {
        return Attributes.of(AttributeKey.stringKey("key"), samplingDecision.name());
      }

      @Override
      public TraceState getUpdatedTraceState(TraceState parentTraceState) {
        return TraceState.builder().put("key", samplingDecision.name()).build();
      }
    };
  }
}
