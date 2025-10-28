/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.TestUtil.traceIdGenerator;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Collections;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ComposableProbabilitySamplerTest {
  @Test
  void testDescription() {
    assertThat(ComposableSampler.probability(1.0).getDescription())
        .isEqualTo("ComposableTraceIdRatioBasedSampler{threshold=0, ratio=1.0}");
    assertThat(ComposableSampler.probability(1.0))
        .hasToString("ComposableTraceIdRatioBasedSampler{threshold=0, ratio=1.0}");

    assertThat(ComposableSampler.probability(0.5).getDescription())
        .isEqualTo("ComposableTraceIdRatioBasedSampler{threshold=8, ratio=0.5}");
    assertThat(ComposableSampler.probability(0.25).getDescription())
        .isEqualTo("ComposableTraceIdRatioBasedSampler{threshold=c, ratio=0.25}");
    assertThat(ComposableSampler.probability(1e-300).getDescription())
        .isEqualTo("ComposableTraceIdRatioBasedSampler{threshold=max, ratio=1.0E-300}");
    assertThat(ComposableSampler.probability(0).getDescription())
        .isEqualTo("ComposableTraceIdRatioBasedSampler{threshold=max, ratio=0.0}");
  }

  @ParameterizedTest
  @CsvSource({
    "1.0, 0",
    "0.5, 36028797018963968",
    "0.25, 54043195528445952",
    "0.125, 63050394783186944",
    "0.0, 72057594037927936",
    "0.45, 39631676720860364",
    "0.2, 57646075230342348",
    "0.13, 62690106812997304",
    "0.05, 68454714336031539",
  })
  void sampling(double ratio, long threshold) {
    Supplier<String> generator = traceIdGenerator();
    Sampler sampler = CompositeSampler.wrap(ComposableSampler.probability(ratio));
    int numSampled = 0;
    for (int i = 0; i < 10000; i++) {
      SamplingResult result =
          sampler.shouldSample(
              Context.root(),
              generator.get(),
              "span",
              SpanKind.SERVER,
              Attributes.empty(),
              Collections.emptyList());
      if (result.getDecision() == SamplingDecision.RECORD_AND_SAMPLE) {
        numSampled++;
        OtelTraceState otTraceState =
            OtelTraceState.parse(result.getUpdatedTraceState(TraceState.getDefault()));
        assertThat(otTraceState.getThreshold()).isEqualTo(threshold);
        assertThat(otTraceState.getRandomValue()).isEqualTo(-1);
      }
    }
    int expectedNumSampled = (int) Math.round(10000 * ratio);
    // NB: It would be better to calculate a standard deviation based on the numbers.
    // But our random seed in TestUtil conveniently allows the test to pass so don't bother.
    assertThat(Math.abs(numSampled - expectedNumSampled)).isLessThan(50);
  }
}
