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

package io.opentelemetry.sdk.trace.samplers;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.sdk.trace.TestUtils.generateRandomSpanId;
import static io.opentelemetry.sdk.trace.TestUtils.generateRandomTraceId;

import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Sampler.Decision;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ProbabilitySampler}. */
@RunWith(JUnit4.class)
public class ProbabilitySamplerTest {

  private static final String SPAN_NAME = "MySpanName";
  private static final int NUM_SAMPLE_TRIES = 1000;
  private final TraceId traceId = generateRandomTraceId();
  private final SpanId parentSpanId = generateRandomSpanId();
  private final Tracestate tracestate = Tracestate.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceOptions.builder().setIsSampled(true).build(), tracestate);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceOptions.getDefault(), tracestate);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void probabilitySampler_AlwaysSample() {
    ProbabilitySampler sampler = ProbabilitySampler.create(1);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  public void probabilitySampler_NeverSample() {
    ProbabilitySampler sampler = ProbabilitySampler.create(0);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MIN_VALUE);
  }

  @Test
  public void probabilitySampler_outOfRangeHighProbability() {
    thrown.expect(IllegalArgumentException.class);
    ProbabilitySampler.create(1.01);
  }

  @Test
  public void probabilitySampler_outOfRangeLowProbability() {
    thrown.expect(IllegalArgumentException.class);
    ProbabilitySampler.create(-0.00001);
  }

  @Test
  public void probabilitySampler_getDescription() {
    assertThat(ProbabilitySampler.create(0.5).getDescription())
        .isEqualTo(String.format("ProbabilitySampler{%.6f}", 0.5));
  }

  @Test
  public void probabilitySampler_ToString() {
    assertThat(ProbabilitySampler.create(0.5).toString()).contains("0.5");
  }

  // Applies the given sampler to NUM_SAMPLE_TRIES random traceId/spanId pairs.
  private static void assertSamplerSamplesWithProbability(
      Sampler sampler, SpanContext parent, List<SpanContext> parentLinks, double probability) {
    int count = 0; // Count of spans with sampling enabled
    for (int i = 0; i < NUM_SAMPLE_TRIES; i++) {
      if (sampler
          .shouldSample(
              parent,
              false,
              generateRandomTraceId(),
              generateRandomSpanId(),
              SPAN_NAME,
              parentLinks)
          .isSampled()) {
        count++;
      }
    }
    double proportionSampled = (double) count / NUM_SAMPLE_TRIES;
    // Allow for a large amount of slop (+/- 10%) in number of sampled traces, to avoid flakiness.
    assertThat(proportionSampled < probability + 0.1 && proportionSampled > probability - 0.1)
        .isTrue();
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_NotSampledParent() {
    final Sampler fiftyPercentSample = ProbabilitySampler.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, notSampledSpanContext, Collections.<SpanContext>emptyList(), 0.5);
    final Sampler twentyPercentSample = ProbabilitySampler.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, notSampledSpanContext, Collections.<SpanContext>emptyList(), 0.2);
    final Sampler twoThirdsSample = ProbabilitySampler.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Collections.<SpanContext>emptyList(), 2.0 / 3.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParent() {
    final Sampler fiftyPercentSample = ProbabilitySampler.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, sampledSpanContext, Collections.<SpanContext>emptyList(), 1.0);
    final Sampler twentyPercentSample = ProbabilitySampler.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, sampledSpanContext, Collections.<SpanContext>emptyList(), 1.0);
    final Sampler twoThirdsSample = ProbabilitySampler.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, sampledSpanContext, Collections.<SpanContext>emptyList(), 1.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParentLink() {
    final Sampler fiftyPercentSample = ProbabilitySampler.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, notSampledSpanContext, Arrays.asList(sampledSpanContext), 1.0);
    final Sampler twentyPercentSample = ProbabilitySampler.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, notSampledSpanContext, Arrays.asList(sampledSpanContext), 1.0);
    final Sampler twoThirdsSample = ProbabilitySampler.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Arrays.asList(sampledSpanContext), 1.0);
  }

  @Test
  public void probabilitySampler_SampleBasedOnTraceId() {
    final Sampler defaultProbability = ProbabilitySampler.create(0.0001);
    // This traceId will not be sampled by the ProbabilitySampler because the first 8 bytes as long
    // is not less than probability * Long.MAX_VALUE;
    TraceId notSampledtraceId =
        TraceId.fromBytes(
            new byte[] {
              (byte) 0x8F,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              0
            },
            0);
    Decision decision1 =
        defaultProbability.shouldSample(
            null,
            false,
            notSampledtraceId,
            generateRandomSpanId(),
            SPAN_NAME,
            Collections.<SpanContext>emptyList());
    assertThat(decision1.isSampled()).isFalse();
    assertThat(decision1.attributes()).isEmpty();
    // This traceId will be sampled by the ProbabilitySampler because the first 8 bytes as long
    // is less than probability * Long.MAX_VALUE;
    TraceId sampledtraceId =
        TraceId.fromBytes(
            new byte[] {
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              0
            },
            0);
    Decision decision2 =
        defaultProbability.shouldSample(
            null,
            false,
            sampledtraceId,
            generateRandomSpanId(),
            SPAN_NAME,
            Collections.<SpanContext>emptyList());
    assertThat(decision2.isSampled()).isTrue();
    assertThat(decision2.attributes()).isEmpty();
  }
}
