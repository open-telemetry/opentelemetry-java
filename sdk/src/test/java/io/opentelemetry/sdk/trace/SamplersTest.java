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

package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;

import com.google.common.truth.Truth;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final Span.Kind SPAN_KIND = Span.Kind.INTERNAL;
  private static final int NUM_SAMPLE_TRIES = 1000;
  private final IdsGenerator idsGenerator = new RandomIdsGenerator();
  private final TraceId traceId = idsGenerator.generateTraceId();
  private final SpanId spanId = idsGenerator.generateSpanId();
  private final SpanId parentSpanId = idsGenerator.generateSpanId();
  private final TraceState traceState = TraceState.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceFlags.builder().setIsSampled(true).build(), traceState);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), traceState);
  private final Link sampledParentLink = SpanData.Link.create(sampledSpanContext);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void alwaysOnSampler_AlwaysReturnTrue() {
    // Sampled parent.
    Truth.assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    spanId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Collections.<String, AttributeValue>emptyMap(),
                    Collections.<Link>emptyList())
                .isSampled())
        .isTrue();
    // Not sampled parent.
    Truth.assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    spanId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Collections.<String, AttributeValue>emptyMap(),
                    Collections.<Link>emptyList())
                .isSampled())
        .isTrue();
  }

  @Test
  public void alwaysOnSampler_ToString() {
    Truth.assertThat(Samplers.alwaysOn().toString()).isEqualTo("AlwaysOnSampler");
  }

  @Test
  public void alwaysOffSampler_AlwaysReturnFalse() {
    // Sampled parent.
    Truth.assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    spanId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Collections.<String, AttributeValue>emptyMap(),
                    Collections.<Link>emptyList())
                .isSampled())
        .isFalse();
    // Not sampled parent.
    Truth.assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    spanId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Collections.<String, AttributeValue>emptyMap(),
                    Collections.<Link>emptyList())
                .isSampled())
        .isFalse();
  }

  @Test
  public void alwaysOffSampler_ToString() {
    Truth.assertThat(Samplers.alwaysOff().toString()).isEqualTo("AlwaysOffSampler");
  }

  @Test
  public void probabilitySampler_AlwaysSample() {
    Samplers.Probability sampler = Samplers.Probability.create(1);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  public void probabilitySampler_NeverSample() {
    Samplers.Probability sampler = Samplers.Probability.create(0);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MIN_VALUE);
  }

  @Test
  public void probabilitySampler_outOfRangeHighProbability() {
    thrown.expect(IllegalArgumentException.class);
    Samplers.Probability.create(1.01);
  }

  @Test
  public void probabilitySampler_outOfRangeLowProbability() {
    thrown.expect(IllegalArgumentException.class);
    Samplers.Probability.create(-0.00001);
  }

  @Test
  public void probabilitySampler_getDescription() {
    assertThat(Samplers.Probability.create(0.5).getDescription())
        .isEqualTo(String.format("ProbabilitySampler{%.6f}", 0.5));
  }

  @Test
  public void probabilitySampler_ToString() {
    assertThat(Samplers.Probability.create(0.5).toString()).contains("0.5");
  }

  // Applies the given sampler to NUM_SAMPLE_TRIES random traceId/spanId pairs.
  private void assertSamplerSamplesWithProbability(
      Sampler sampler, SpanContext parent, List<Link> parentLinks, double probability) {
    int count = 0; // Count of spans with sampling enabled
    for (int i = 0; i < NUM_SAMPLE_TRIES; i++) {
      if (sampler
          .shouldSample(
              parent,
              idsGenerator.generateTraceId(),
              idsGenerator.generateSpanId(),
              SPAN_NAME,
              SPAN_KIND,
              Collections.<String, AttributeValue>emptyMap(),
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
    final Sampler fiftyPercentSample = Samplers.Probability.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, notSampledSpanContext, Collections.<Link>emptyList(), 0.5);
    final Sampler twentyPercentSample = Samplers.Probability.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, notSampledSpanContext, Collections.<Link>emptyList(), 0.2);
    final Sampler twoThirdsSample = Samplers.Probability.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Collections.<Link>emptyList(), 2.0 / 3.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParent() {
    final Sampler fiftyPercentSample = Samplers.Probability.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, sampledSpanContext, Collections.<Link>emptyList(), 1.0);
    final Sampler twentyPercentSample = Samplers.Probability.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, sampledSpanContext, Collections.<Link>emptyList(), 1.0);
    final Sampler twoThirdsSample = Samplers.Probability.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, sampledSpanContext, Collections.<Link>emptyList(), 1.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParentLink() {
    final Sampler fiftyPercentSample = Samplers.Probability.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample,
        notSampledSpanContext,
        Collections.singletonList(sampledParentLink),
        1.0);
    final Sampler twentyPercentSample = Samplers.Probability.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample,
        notSampledSpanContext,
        Collections.singletonList(sampledParentLink),
        1.0);
    final Sampler twoThirdsSample = Samplers.Probability.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Collections.singletonList(sampledParentLink), 1.0);
  }

  @Test
  public void probabilitySampler_SampleBasedOnTraceId() {
    final Sampler defaultProbability = Samplers.Probability.create(0.0001);
    // This traceId will not be sampled by the Probability Sampler because the first 8 bytes as long
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
            notSampledtraceId,
            idsGenerator.generateSpanId(),
            SPAN_NAME,
            SPAN_KIND,
            Collections.<String, AttributeValue>emptyMap(),
            Collections.<Link>emptyList());
    assertThat(decision1.isSampled()).isFalse();
    assertThat(decision1.getAttributes())
        .containsExactly(Samplers.SAMPLING_PROBABILITY.key(), doubleAttributeValue(0.0001));
    // This traceId will be sampled by the Probability Sampler because the first 8 bytes as long
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
            sampledtraceId,
            idsGenerator.generateSpanId(),
            SPAN_NAME,
            SPAN_KIND,
            Collections.<String, AttributeValue>emptyMap(),
            Collections.<Link>emptyList());
    assertThat(decision2.isSampled()).isTrue();
    assertThat(decision1.getAttributes())
        .containsExactly(Samplers.SAMPLING_PROBABILITY.key(), doubleAttributeValue(0.0001));
  }
}
