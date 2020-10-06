/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
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
  private final String traceId = idsGenerator.generateTraceId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final TraceState traceState = TraceState.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), traceState);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), traceState);
  private final SpanContext invalidSpanContext = SpanContext.getInvalid();
  private final Link sampledParentLink = Link.create(sampledSpanContext);
  private final SpanContext sampledRemoteSpanContext =
      SpanContext.createFromRemoteParent(
          traceId, parentSpanId, TraceFlags.getSampled(), traceState);
  private final SpanContext notSampledRemoteSpanContext =
      SpanContext.createFromRemoteParent(
          traceId, parentSpanId, TraceFlags.getDefault(), traceState);

  @Test
  void emptySamplingDecision() {
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLE))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLE));
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.DROP))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.DROP));

    assertThat(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLE).getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
    assertThat(
            Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLE)
                .getAttributes()
                .isEmpty())
        .isTrue();
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.DROP).getDecision())
        .isEqualTo(Decision.DROP);
    assertThat(Samplers.emptySamplingResult(Sampler.Decision.DROP).getAttributes().isEmpty())
        .isTrue();
  }

  @Test
  void samplingDecisionEmpty() {
    assertThat(Samplers.samplingResult(Sampler.Decision.RECORD_AND_SAMPLE, Attributes.empty()))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.RECORD_AND_SAMPLE));
    assertThat(Samplers.samplingResult(Sampler.Decision.DROP, Attributes.empty()))
        .isSameAs(Samplers.emptySamplingResult(Sampler.Decision.DROP));
  }

  @Test
  void samplingDecisionAttrs() {
    final Attributes attrs = Attributes.of(longKey("foo"), 42L, stringKey("bar"), "baz");
    final SamplingResult sampledSamplingResult =
        Samplers.samplingResult(Sampler.Decision.RECORD_AND_SAMPLE, attrs);
    assertThat(sampledSamplingResult.getDecision()).isEqualTo(Decision.RECORD_AND_SAMPLE);
    assertThat(sampledSamplingResult.getAttributes()).isEqualTo(attrs);

    final SamplingResult notSampledSamplingResult =
        Samplers.samplingResult(Sampler.Decision.DROP, attrs);
    assertThat(notSampledSamplingResult.getDecision()).isEqualTo(Decision.DROP);
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
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

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
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

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
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
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
        .isEqualTo(Decision.DROP);

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
        .isEqualTo(Decision.DROP);

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
        .isEqualTo(Decision.DROP);
  }

  @Test
  void alwaysOffSampler_GetDescription() {
    assertThat(Samplers.alwaysOff().getDescription()).isEqualTo("AlwaysOffSampler");
  }

  @Test
  void parentBasedSampler_AlwaysOn() {
    // Sampled parent.
    assertThat(
            Samplers.parentBased(Samplers.alwaysOn())
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    // Not sampled parent.
    assertThat(
            Samplers.parentBased(Samplers.alwaysOn())
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);
  }

  @Test
  void parentBasedSampler_AlwaysOff() {
    // Sampled parent.
    assertThat(
            Samplers.parentBased(Samplers.alwaysOff())
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    // Not sampled parent.
    assertThat(
            Samplers.parentBased(Samplers.alwaysOff())
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);
  }

  @Test
  void parentBasedSampler_NotSampled_Remote_Parent() {
    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setRemoteParentNotSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    notSampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setRemoteParentNotSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    notSampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setRemoteParentNotSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    notSampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setRemoteParentNotSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    notSampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
  }

  @Test
  void parentBasedSampler_NotSampled_NotRemote_Parent() {

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setLocalParentNotSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setLocalParentNotSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setLocalParentNotSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setLocalParentNotSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
  }

  @Test
  void parentBasedSampler_Sampled_Remote_Parent() {
    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setRemoteParentSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    sampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setRemoteParentSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    sampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setRemoteParentSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    sampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setRemoteParentSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    sampledRemoteSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);
  }

  @Test
  void parentBasedSampler_Sampled_NotRemote_Parent() {
    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setLocalParentSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setLocalParentSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setLocalParentSampled(Samplers.alwaysOff())
                .build()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOn())
                .setLocalParentSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
  }

  @Test
  void parentBasedSampler_invalid_Parent() {
    assertThat(
            Samplers.parentBased(Samplers.alwaysOff())
                .shouldSample(
                    invalidSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBased(Samplers.alwaysOff())
                .shouldSample(
                    invalidSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBasedBuilder(Samplers.alwaysOff())
                .setRemoteParentSampled(Samplers.alwaysOn())
                .setRemoteParentNotSampled(Samplers.alwaysOn())
                .setLocalParentSampled(Samplers.alwaysOn())
                .setLocalParentNotSampled(Samplers.alwaysOn())
                .build()
                .shouldSample(
                    invalidSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);

    assertThat(
            Samplers.parentBased(Samplers.alwaysOn())
                .shouldSample(
                    invalidSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
  }

  @Test
  void parentBasedSampler_GetDescription() {
    assertThat(Samplers.parentBased(Samplers.alwaysOn()).getDescription())
        .isEqualTo(
            "ParentBased{root:AlwaysOnSampler,remoteParentSampled:AlwaysOnSampler,"
                + "remoteParentNotSampled:AlwaysOffSampler,localParentSampled:AlwaysOnSampler,"
                + "localParentNotSampled:AlwaysOffSampler}");
  }

  @Test
  void traceIdRatioBased_AlwaysSample() {
    Samplers.TraceIdRatioBased sampler = Samplers.TraceIdRatioBased.create(1);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  void traceIdRatioBased_NeverSample() {
    Samplers.TraceIdRatioBased sampler = Samplers.TraceIdRatioBased.create(0);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MIN_VALUE);
  }

  @Test
  void traceIdRatioBased_outOfRangeHighProbability() {
    assertThrows(IllegalArgumentException.class, () -> Samplers.TraceIdRatioBased.create(1.01));
  }

  @Test
  void traceIdRatioBased_outOfRangeLowProbability() {
    assertThrows(IllegalArgumentException.class, () -> Samplers.TraceIdRatioBased.create(-0.00001));
  }

  @Test
  void traceIdRatioBased_getDescription() {
    assertThat(Samplers.TraceIdRatioBased.create(0.5).getDescription())
        .isEqualTo(String.format("TraceIdRatioBased{%.6f}", 0.5));
  }

  // Applies the given sampler to NUM_SAMPLE_TRIES random traceId.
  private void assertSamplerSamplesWithProbability(
      Sampler sampler, SpanContext parent, List<SpanData.Link> parentLinks, double probability) {
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
  void traceIdRatioBased_DifferentProbabilities_NotSampledParent() {
    assertTraceIdRatioBased_NotSampledParent(Samplers.TraceIdRatioBased.create(0.5), 0.5);
    assertTraceIdRatioBased_NotSampledParent(Samplers.TraceIdRatioBased.create(0.2), 0.2);
    assertTraceIdRatioBased_NotSampledParent(
        Samplers.TraceIdRatioBased.create(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. NOT sampling, if wrapped
    // around ParentBased
    assertTraceIdRatioBased_NotSampledParent(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.5)), 0);
    assertTraceIdRatioBased_NotSampledParent(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2)), 0);
    assertTraceIdRatioBased_NotSampledParent(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2 / 0.3)), 0);
  }

  private void assertTraceIdRatioBased_NotSampledParent(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, notSampledSpanContext, Collections.emptyList(), probability);
  }

  @Test
  void traceIdRatioBased_DifferentProbabilities_SampledParent() {
    assertTraceIdRatioBased_SampledParent(Samplers.TraceIdRatioBased.create(0.5), 0.5);
    assertTraceIdRatioBased_SampledParent(Samplers.TraceIdRatioBased.create(0.2), 0.2);
    assertTraceIdRatioBased_SampledParent(Samplers.TraceIdRatioBased.create(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. sampling, if wrapped around
    // ParentBased
    assertTraceIdRatioBased_SampledParent(Samplers.parentBased(Samplers.traceIdRatioBased(0.5)), 1);
    assertTraceIdRatioBased_SampledParent(Samplers.parentBased(Samplers.traceIdRatioBased(0.2)), 1);
    assertTraceIdRatioBased_SampledParent(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2 / 0.3)), 1);
  }

  private void assertTraceIdRatioBased_SampledParent(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, sampledSpanContext, Collections.emptyList(), probability);
  }

  @Test
  void traceIdRatioBased_DifferentProbabilities_SampledParentLink() {
    // Parent NOT sampled
    assertTraceIdRatioBased_SampledParentLink(Samplers.TraceIdRatioBased.create(0.5), 0.5);
    assertTraceIdRatioBased_SampledParentLink(Samplers.TraceIdRatioBased.create(0.2), 0.2);
    assertTraceIdRatioBased_SampledParentLink(
        Samplers.TraceIdRatioBased.create(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. NOT sampling, if wrapped
    // around ParentBased
    assertTraceIdRatioBased_SampledParentLink(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.5)), 0);
    assertTraceIdRatioBased_SampledParentLink(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2)), 0);
    assertTraceIdRatioBased_SampledParentLink(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2 / 0.3)), 0);

    // Parent Sampled
    assertTraceIdRatioBased_SampledParentLinkContext(Samplers.TraceIdRatioBased.create(0.5), 0.5);
    assertTraceIdRatioBased_SampledParentLinkContext(Samplers.TraceIdRatioBased.create(0.2), 0.2);
    assertTraceIdRatioBased_SampledParentLinkContext(
        Samplers.TraceIdRatioBased.create(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. sampling, if wrapped around
    // ParentBased
    assertTraceIdRatioBased_SampledParentLinkContext(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.5)), 1);
    assertTraceIdRatioBased_SampledParentLinkContext(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2)), 1);
    assertTraceIdRatioBased_SampledParentLinkContext(
        Samplers.parentBased(Samplers.traceIdRatioBased(0.2 / 0.3)), 1);
  }

  private void assertTraceIdRatioBased_SampledParentLink(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, notSampledSpanContext, Collections.singletonList(sampledParentLink), probability);
  }

  private void assertTraceIdRatioBased_SampledParentLinkContext(
      Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, sampledSpanContext, Collections.singletonList(sampledParentLink), probability);
  }

  @Test
  void traceIdRatioBased_SampleBasedOnTraceId() {
    final Sampler defaultProbability = Samplers.TraceIdRatioBased.create(0.0001);
    // This traceId will not be sampled by the Probability Sampler because the last 8 bytes as long
    // is not less than probability * Long.MAX_VALUE;
    String notSampledTraceId =
        TraceId.bytesToHex(
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
            });
    SamplingResult samplingResult1 =
        defaultProbability.shouldSample(
            invalidSpanContext,
            notSampledTraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult1.getDecision()).isEqualTo(Decision.DROP);
    assertThat(samplingResult1.getAttributes())
        .isEqualTo(Attributes.of(Samplers.SAMPLING_PROBABILITY, 0.0001));
    // This traceId will be sampled by the Probability Sampler because the last 8 bytes as long
    // is less than probability * Long.MAX_VALUE;
    String sampledTraceId =
        TraceId.bytesToHex(
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
            });
    SamplingResult samplingResult2 =
        defaultProbability.shouldSample(
            invalidSpanContext,
            sampledTraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult2.getDecision()).isEqualTo(Decision.RECORD_AND_SAMPLE);
    assertThat(samplingResult1.getAttributes())
        .isEqualTo(Attributes.of(Samplers.SAMPLING_PROBABILITY, 0.0001));
  }

  @Test
  void isSampled() {
    assertThat(Samplers.isSampled(Decision.DROP)).isFalse();
    assertThat(Samplers.isSampled(Decision.RECORD_ONLY)).isFalse();
    assertThat(Samplers.isSampled(Decision.RECORD_AND_SAMPLE)).isTrue();
  }

  @Test
  void isRecording() {
    assertThat(Samplers.isRecording(Decision.DROP)).isFalse();
    assertThat(Samplers.isRecording(Decision.RECORD_ONLY)).isTrue();
    assertThat(Samplers.isRecording(Decision.RECORD_AND_SAMPLE)).isTrue();
  }
}
