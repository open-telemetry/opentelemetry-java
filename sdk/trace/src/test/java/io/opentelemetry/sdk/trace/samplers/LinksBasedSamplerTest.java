/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.LinkData;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LinksBasedSamplerTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final SpanKind SPAN_KIND = SpanKind.INTERNAL;
  private final IdGenerator idsGenerator = IdGenerator.random();
  private final String traceId = idsGenerator.generateTraceId();
  private final String parentSpanId = idsGenerator.generateSpanId();

  private final SpanContext sampledSpanContext1 =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), TraceState.getDefault());

  private final SpanContext sampledSpanContext2 =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), TraceState.getDefault());

  private final SpanContext unsampledSpanContext1 =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault());

  private final SpanContext unsampledSpanContext2 =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault());

  private final Context sampledParentContext = Context.root().with(Span.wrap(sampledSpanContext1));

  @Test
  void testEmptyAlwaysTrueRoot() {
    assertThat(
        Sampler.linksBased(Sampler.alwaysOn())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList())
            .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void testEmptyAlwaysFalseRoot() {
    assertThat(
        Sampler.linksBased(Sampler.alwaysOff())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList())
            .getDecision())
        .isEqualTo(SamplingDecision.DROP);
  }

  @Test
  void testOneSampled() {
    List<LinkData> linkData = new ArrayList<>();
    linkData.add(LinkData.create(sampledSpanContext1));

    assertThat(
        Sampler.linksBased(Sampler.alwaysOff())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                linkData)
            .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void testOneNotSampled() {
    List<LinkData> linkData = new ArrayList<>();
    linkData.add(LinkData.create(unsampledSpanContext1));

    assertThat(
        Sampler.linksBased(Sampler.alwaysOn())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                linkData)
            .getDecision())
        .isEqualTo(SamplingDecision.DROP);
  }

  @Test
  void testTwoSampledAndNotSampled() {
    List<LinkData> linkData = new ArrayList<>();
    linkData.add(LinkData.create(sampledSpanContext1));
    linkData.add(LinkData.create(unsampledSpanContext1));

    assertThat(
        Sampler.linksBased(Sampler.alwaysOff())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                linkData)
            .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void testTwoNotSampledAndSampled() {
    List<LinkData> linkData = new ArrayList<>();
    linkData.add(LinkData.create(unsampledSpanContext1));
    linkData.add(LinkData.create(sampledSpanContext1));

    assertThat(
        Sampler.linksBased(Sampler.alwaysOff())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                linkData)
            .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void testTwoSampled() {
    List<LinkData> linkData = new ArrayList<>();
    linkData.add(LinkData.create(sampledSpanContext1));
    linkData.add(LinkData.create(sampledSpanContext2));

    assertThat(
        Sampler.linksBased(Sampler.alwaysOff())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                linkData)
            .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void testTwoUnsampled() {
    List<LinkData> linkData = new ArrayList<>();
    linkData.add(LinkData.create(unsampledSpanContext1));
    linkData.add(LinkData.create(unsampledSpanContext2));

    assertThat(
        Sampler.linksBased(Sampler.alwaysOn())
            .shouldSample(
                sampledParentContext,
                traceId,
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                linkData)
            .getDecision())
        .isEqualTo(SamplingDecision.DROP);
  }
}
