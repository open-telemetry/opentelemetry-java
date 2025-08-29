/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.TestUtil.traceIdGenerator;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Collections;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ComposableAlwaysOnSamplerTest {
  @Test
  void testDescription() {
    assertThat(ComposableSampler.alwaysOn().getDescription())
        .isEqualTo("ComposableAlwaysOnSampler");
    assertThat(ComposableSampler.alwaysOn()).hasToString("ComposableAlwaysOnSampler");
  }

  @Test
  void testThreshold() {
    assertThat(
            ComposableSampler.alwaysOn()
                .getSamplingIntent(
                    Context.root(),
                    TraceId.getInvalid(),
                    "span",
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getThreshold())
        .isEqualTo(0);
  }

  @Test
  void sampling() {
    Supplier<String> generator = traceIdGenerator();
    Sampler sampler = CompositeSampler.wrap(ComposableSampler.alwaysOn());
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
      }
    }
    assertThat(numSampled).isEqualTo(10000);
  }
}
