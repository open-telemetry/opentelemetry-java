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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
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
  private final SpanId parentSpanId = idsGenerator.generateSpanId();
  private final TraceState traceState = TraceState.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceFlags.builder().setIsSampled(true).build(), traceState);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), traceState);
  private final io.opentelemetry.trace.Link sampledParentLink = Link.create(sampledSpanContext);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void emptySamplingDecision() {
    assertThat(Samplers.emptyDecision(true)).isSameInstanceAs(Samplers.emptyDecision(true));
    assertThat(Samplers.emptyDecision(false)).isSameInstanceAs(Samplers.emptyDecision(false));

    assertThat(Samplers.emptyDecision(true).isSampled()).isTrue();
    assertThat(Samplers.emptyDecision(true).getAttributes().isEmpty()).isTrue();
    assertThat(Samplers.emptyDecision(false).isSampled()).isFalse();
    assertThat(Samplers.emptyDecision(false).getAttributes().isEmpty()).isTrue();
  }

  @Test
  public void samplingDecisionEmpty() {
    assertThat(Samplers.decision(/*isSampled=*/ true, Attributes.empty()))
        .isSameInstanceAs(Samplers.emptyDecision(true));
    assertThat(Samplers.decision(/*isSampled=*/ false, Attributes.empty()))
        .isSameInstanceAs(Samplers.emptyDecision(false));
  }

  @Test
  public void samplingDecisionAttrs() {
    final Attributes attrs =
        Attributes.of(
            "foo", AttributeValue.longAttributeValue(42),
            "bar", AttributeValue.stringAttributeValue("baz"));
    final Decision sampledDecision = Samplers.decision(/*isSampled=*/ true, attrs);
    assertThat(sampledDecision.isSampled()).isTrue();
    assertThat(sampledDecision.getAttributes()).isEqualTo(attrs);

    final Decision notSampledDecision = Samplers.decision(/*isSampled=*/ false, attrs);
    assertThat(notSampledDecision.isSampled()).isFalse();
    assertThat(notSampledDecision.getAttributes()).isEqualTo(attrs);
  }

  @Test
  public void alwaysOnSampler() {
    // Sampled parent.
    assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isTrue();

    // Not sampled parent.
    assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isTrue();

    // Null parent.
    assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    null,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isTrue();
  }

  @Test
  public void alwaysOnSampler_GetDescription() {
    assertThat(Samplers.alwaysOn().getDescription()).isEqualTo("AlwaysOnSampler");
  }

  @Test
  public void alwaysOffSampler() {
    // Sampled parent.
    assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isFalse();

    // Not sampled parent.
    assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isFalse();

    // Null parent.
    assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    null,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isFalse();
  }

  @Test
  public void alwaysOffSampler_GetDescription() {
    assertThat(Samplers.alwaysOff().getDescription()).isEqualTo("AlwaysOffSampler");
  }

  @Test
  public void parentOrElseSampler_AlwaysOn() {
    // Sampled parent.
    assertThat(
            Samplers.parentOrElse(Samplers.alwaysOn())
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isTrue();

    // Not sampled parent.
    assertThat(
        Samplers.parentOrElse(Samplers.alwaysOn())
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isFalse();

    // Null parent.
    assertThat(
        Samplers.parentOrElse(Samplers.alwaysOn())
                .shouldSample(
                    null,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .isSampled())
        .isTrue();
  }

  @Test
  public void parentOrElseSampler_AlwaysOff() {
    // Sampled parent.
    assertThat(
        Samplers.parentOrElse(Samplers.alwaysOff())
            .shouldSample(
                sampledSpanContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList())
            .isSampled())
        .isTrue();

    // Not sampled parent.
    assertThat(
        Samplers.parentOrElse(Samplers.alwaysOff())
            .shouldSample(
                notSampledSpanContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList())
            .isSampled())
        .isFalse();

    // Null parent.
    assertThat(
        Samplers.parentOrElse(Samplers.alwaysOff())
            .shouldSample(
                null,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList())
            .isSampled())
        .isFalse();
  }

  @Test
  public void parentOrElseSampler_GetDescription() {
    assertThat(Samplers.parentOrElse(Samplers.alwaysOn()).getDescription()).isEqualTo("ParentOrElseSampler-AlwaysOnSampler");
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

  // Applies the given sampler to NUM_SAMPLE_TRIES random traceId.
  private void assertSamplerSamplesWithProbability(
      Sampler sampler,
      SpanContext parent,
      List<io.opentelemetry.trace.Link> parentLinks,
      double probability) {
    int count = 0; // Count of spans with sampling enabled
    for (int i = 0; i < NUM_SAMPLE_TRIES; i++) {
      if (sampler
          .shouldSample(
              parent,
              idsGenerator.generateTraceId(),
              SPAN_NAME,
              SPAN_KIND,
              Attributes.empty(),
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
        fiftyPercentSample, notSampledSpanContext, Collections.emptyList(), 0.5);
    final Sampler twentyPercentSample = Samplers.Probability.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, notSampledSpanContext, Collections.emptyList(), 0.2);
    final Sampler twoThirdsSample = Samplers.Probability.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Collections.emptyList(), 2.0 / 3.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParent() {
    final Sampler fiftyPercentSample = Samplers.Probability.create(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, sampledSpanContext, Collections.emptyList(), 1.0);
    final Sampler twentyPercentSample = Samplers.Probability.create(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, sampledSpanContext, Collections.emptyList(), 1.0);
    final Sampler twoThirdsSample = Samplers.Probability.create(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, sampledSpanContext, Collections.emptyList(), 1.0);
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
    // This traceId will not be sampled by the Probability Sampler because the last 8 bytes as long
    // is not less than probability * Long.MAX_VALUE;
    TraceId notSampledtraceId =
        TraceId.fromBytes(
            new byte[] {
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              (byte) 0x8F,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF
            },
            0);
    Decision decision1 =
        defaultProbability.shouldSample(
            null,
            notSampledtraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(decision1.isSampled()).isFalse();
    assertThat(decision1.getAttributes())
        .isEqualTo(
            Attributes.of(Samplers.SAMPLING_PROBABILITY.key(), doubleAttributeValue(0.0001)));
    // This traceId will be sampled by the Probability Sampler because the last 8 bytes as long
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
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(decision2.isSampled()).isTrue();
    assertThat(decision1.getAttributes())
        .isEqualTo(
            Attributes.of(Samplers.SAMPLING_PROBABILITY.key(), doubleAttributeValue(0.0001)));
  }
}
