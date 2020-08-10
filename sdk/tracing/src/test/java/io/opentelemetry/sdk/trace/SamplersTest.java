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

import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Samplers}. */
class SamplersTest {
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

  @Test
  void emptySamplingDecision() {
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLED))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLED));
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.NOT_RECORD))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.NOT_RECORD));

    assertThat(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLED).getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);
    assertThat(
            Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLED)
                .getAttributes()
                .isEmpty())
        .isTrue();
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.NOT_RECORD).getDecision())
        .isEqualTo(Decision.NOT_RECORD);
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.NOT_RECORD).getAttributes().isEmpty())
        .isTrue();
  }

  @Test
  void samplingDecisionEmpty() {
    assertThat(Samplers.samplingResult(Sampler.Decision.RECORD_AND_SAMPLED, Attributes.empty()))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLED));
    assertThat(Samplers.samplingResult(Sampler.Decision.NOT_RECORD, Attributes.empty()))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.NOT_RECORD));
  }

  @Test
  void samplingDecisionAttrs() {
    final Attributes attrs =
        Attributes.of(
            "foo", AttributeValue.longAttributeValue(42),
            "bar", AttributeValue.stringAttributeValue("baz"));
    final SamplingResult sampledSamplingResult =
        Samplers.samplingResult(Sampler.Decision.RECORD_AND_SAMPLED, attrs);
    assertThat(sampledSamplingResult.getDecision()).isEqualTo(Decision.RECORD_AND_SAMPLED);
    assertThat(sampledSamplingResult.getAttributes()).isEqualTo(attrs);

    final SamplingResult notSampledSamplingResult =
        Samplers.samplingResult(Sampler.Decision.NOT_RECORD, attrs);
    assertThat(notSampledSamplingResult.getDecision()).isEqualTo(Decision.NOT_RECORD);
    assertThat(notSampledSamplingResult.getAttributes()).isEqualTo(attrs);
  }

  @Test
  void alwaysOnSampler() {
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
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);

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
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);

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
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);
  }

  @Test
  void alwaysOnSampler_GetDescription() {
    assertThat(Samplers.alwaysOn().getDescription()).isEqualTo("AlwaysOnSampler");
  }

  @Test
  void alwaysOffSampler() {
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
                .getDecision())
        .isEqualTo(Decision.NOT_RECORD);

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
                .getDecision())
        .isEqualTo(Decision.NOT_RECORD);

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
                .getDecision())
        .isEqualTo(Decision.NOT_RECORD);
  }

  @Test
  void alwaysOffSampler_GetDescription() {
    assertThat(Samplers.alwaysOff().getDescription()).isEqualTo("AlwaysOffSampler");
  }

  @Test
  void parentOrElseSampler_AlwaysOn() {
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
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);

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
                .getDecision())
        .isEqualTo(Decision.NOT_RECORD);

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
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);
  }

  @Test
  void parentOrElseSampler_AlwaysOff() {
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
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLED);

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
                .getDecision())
        .isEqualTo(Decision.NOT_RECORD);

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
                .getDecision())
        .isEqualTo(Decision.NOT_RECORD);
  }

  @Test
  void parentOrElseSampler_GetDescription() {
    assertThat(Samplers.parentOrElse(Samplers.alwaysOn()).getDescription())
        .isEqualTo("ParentOrElse{AlwaysOnSampler}");
  }

  @Test
  void probabilitySampler_AlwaysSample() {
    Samplers.Probability sampler = Samplers.Probability.create(1);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  void probabilitySampler_NeverSample() {
    Samplers.Probability sampler = Samplers.Probability.create(0);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MIN_VALUE);
  }

  @Test
  void probabilitySampler_outOfRangeHighProbability() {
    assertThrows(IllegalArgumentException.class, () -> Samplers.Probability.create(1.01));
  }

  @Test
  void probabilitySampler_outOfRangeLowProbability() {
    assertThrows(IllegalArgumentException.class, () -> Samplers.Probability.create(-0.00001));
  }

  @Test
  void probabilitySampler_getDescription() {
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
      if (Samplers.isSampled(
          sampler
              .shouldSample(
                  parent,
                  idsGenerator.generateTraceId(),
                  SPAN_NAME,
                  SPAN_KIND,
                  Attributes.empty(),
                  parentLinks)
              .getDecision())) {
        count++;
      }
    }
    double proportionSampled = (double) count / NUM_SAMPLE_TRIES;
    // Allow for a large amount of slop (+/- 10%) in number of sampled traces, to avoid flakiness.
    assertThat(proportionSampled < probability + 0.1 && proportionSampled > probability - 0.1)
        .isTrue();
  }

  @Test
  void probabilitySampler_DifferentProbabilities_NotSampledParent() {
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
  void probabilitySampler_DifferentProbabilities_SampledParent() {
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
  void probabilitySampler_DifferentProbabilities_SampledParentLink() {
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
  void probabilitySampler_SampleBasedOnTraceId() {
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
    SamplingResult samplingResult1 =
        defaultProbability.shouldSample(
            null,
            notSampledtraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult1.getDecision()).isEqualTo(Decision.NOT_RECORD);
    assertThat(samplingResult1.getAttributes())
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
    SamplingResult samplingResult2 =
        defaultProbability.shouldSample(
            null,
            sampledtraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult2.getDecision()).isEqualTo(Decision.RECORD_AND_SAMPLED);
    assertThat(samplingResult1.getAttributes())
        .isEqualTo(
            Attributes.of(Samplers.SAMPLING_PROBABILITY.key(), doubleAttributeValue(0.0001)));
  }

  @Test
  void isSampled() {
    assertThat(Samplers.isSampled(Decision.NOT_RECORD)).isFalse();
    assertThat(Samplers.isSampled(Decision.RECORD)).isFalse();
    assertThat(Samplers.isSampled(Decision.RECORD_AND_SAMPLED)).isTrue();
  }

  @Test
  void isRecording() {
    assertThat(Samplers.isRecording(Decision.NOT_RECORD)).isFalse();
    assertThat(Samplers.isRecording(Decision.RECORD)).isTrue();
    assertThat(Samplers.isRecording(Decision.RECORD_AND_SAMPLED)).isTrue();
  }
}
