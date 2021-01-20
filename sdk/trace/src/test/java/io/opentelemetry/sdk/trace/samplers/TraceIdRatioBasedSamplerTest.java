/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraceIdRatioBasedSamplerTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final Span.Kind SPAN_KIND = Span.Kind.INTERNAL;
  private static final int NUM_SAMPLE_TRIES = 1000;

  private static final IdGenerator idsGenerator = IdGenerator.random();

  private final String traceId = idsGenerator.generateTraceId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final TraceState traceState = TraceState.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), traceState);
  private final Context sampledParentContext = Context.root().with(Span.wrap(sampledSpanContext));
  private final Context notSampledParentContext =
      Context.root()
          .with(
              Span.wrap(
                  SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), traceState)));
  private final Context invalidParentContext = Context.root().with(Span.getInvalid());
  private final LinkData sampledParentLink = LinkData.create(sampledSpanContext);

  @Test
  void alwaysSample() {
    TraceIdRatioBasedSampler sampler = TraceIdRatioBasedSampler.create(1);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  void neverSample() {
    TraceIdRatioBasedSampler sampler = TraceIdRatioBasedSampler.create(0);
    assertThat(sampler.getIdUpperBound()).isEqualTo(Long.MIN_VALUE);
  }

  @Test
  void outOfRangeHighProbability() {
    assertThatThrownBy(() -> Sampler.traceIdRatioBased(1.01))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void outOfRangeLowProbability() {
    assertThatThrownBy(() -> Sampler.traceIdRatioBased(-0.00001))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getDescription() {
    assertThat(Sampler.traceIdRatioBased(0.5).getDescription())
        .isEqualTo(String.format("TraceIdRatioBased{%.6f}", 0.5));
  }

  @Test
  void differentProbabilities_NotSampledParent() {
    assert_NotSampledParent(Sampler.traceIdRatioBased(0.5), 0.5);
    assert_NotSampledParent(Sampler.traceIdRatioBased(0.2), 0.2);
    assert_NotSampledParent(Sampler.traceIdRatioBased(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. NOT sampling, if wrapped
    // around ParentBased
    assert_NotSampledParent(Sampler.parentBased(Sampler.traceIdRatioBased(0.5)), 0);
    assert_NotSampledParent(Sampler.parentBased(Sampler.traceIdRatioBased(0.2)), 0);
    assert_NotSampledParent(Sampler.parentBased(Sampler.traceIdRatioBased(0.2 / 0.3)), 0);
  }

  private void assert_NotSampledParent(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, notSampledParentContext, Collections.emptyList(), probability);
  }

  @Test
  void differentProbabilities_SampledParent() {
    assertSampledParent(Sampler.traceIdRatioBased(0.5), 0.5);
    assertSampledParent(Sampler.traceIdRatioBased(0.2), 0.2);
    assertSampledParent(Sampler.traceIdRatioBased(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. sampling, if wrapped around
    // ParentBased
    assertSampledParent(Sampler.parentBased(Sampler.traceIdRatioBased(0.5)), 1);
    assertSampledParent(Sampler.parentBased(Sampler.traceIdRatioBased(0.2)), 1);
    assertSampledParent(Sampler.parentBased(Sampler.traceIdRatioBased(0.2 / 0.3)), 1);
  }

  private void assertSampledParent(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, sampledParentContext, Collections.emptyList(), probability);
  }

  @Test
  void differentProbabilities_SampledParentLink() {
    // Parent NOT sampled
    assertSampledParentLink(Sampler.traceIdRatioBased(0.5), 0.5);
    assertSampledParentLink(Sampler.traceIdRatioBased(0.2), 0.2);
    assertSampledParentLink(Sampler.traceIdRatioBased(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. NOT sampling, if wrapped
    // around ParentBased
    assertSampledParentLink(Sampler.parentBased(Sampler.traceIdRatioBased(0.5)), 0);
    assertSampledParentLink(Sampler.parentBased(Sampler.traceIdRatioBased(0.2)), 0);
    assertSampledParentLink(Sampler.parentBased(Sampler.traceIdRatioBased(0.2 / 0.3)), 0);

    // Parent Sampled
    assertSampledParentLinkContext(Sampler.traceIdRatioBased(0.5), 0.5);
    assertSampledParentLinkContext(Sampler.traceIdRatioBased(0.2), 0.2);
    assertSampledParentLinkContext(Sampler.traceIdRatioBased(0.2 / 0.3), 0.2 / 0.3);
    // Probability sampler will respect parent sampling decision, i.e. sampling, if wrapped around
    // ParentBased
    assertSampledParentLinkContext(Sampler.parentBased(Sampler.traceIdRatioBased(0.5)), 1);
    assertSampledParentLinkContext(Sampler.parentBased(Sampler.traceIdRatioBased(0.2)), 1);
    assertSampledParentLinkContext(Sampler.parentBased(Sampler.traceIdRatioBased(0.2 / 0.3)), 1);
  }

  private void assertSampledParentLink(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler,
        notSampledParentContext,
        Collections.singletonList(sampledParentLink),
        probability);
  }

  private void assertSampledParentLinkContext(Sampler sampler, double probability) {
    assertSamplerSamplesWithProbability(
        sampler, sampledParentContext, Collections.singletonList(sampledParentLink), probability);
  }

  @Test
  void sampleBasedOnTraceId() {
    final Sampler defaultProbability = Sampler.traceIdRatioBased(0.0001);
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
            invalidParentContext,
            notSampledTraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult1.getDecision()).isEqualTo(SamplingDecision.DROP);
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
            invalidParentContext,
            sampledTraceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult2.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  // Applies the given sampler to NUM_SAMPLE_TRIES random traceId.
  private static void assertSamplerSamplesWithProbability(
      Sampler sampler, Context parent, List<LinkData> parentLinks, double probability) {
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
          .getDecision()
          .equals(SamplingDecision.RECORD_AND_SAMPLE)) {
        count++;
      }
    }
    double proportionSampled = (double) count / NUM_SAMPLE_TRIES;
    // Allow for a large amount of slop (+/- 10%) in number of sampled traces, to avoid flakiness.
    assertThat(proportionSampled < probability + 0.1 && proportionSampled > probability - 0.1)
        .isTrue();
  }
}
