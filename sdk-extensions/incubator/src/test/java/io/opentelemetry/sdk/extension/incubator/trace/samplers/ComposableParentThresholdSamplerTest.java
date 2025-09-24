/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ComposableParentThresholdSamplerTest {
  @Test
  void testDescription() {
    assertThat(ComposableSampler.parentThreshold(ComposableSampler.alwaysOn()).getDescription())
        .isEqualTo("ComposableParentThresholdSampler{rootSampler=ComposableAlwaysOnSampler}");
    assertThat(ComposableSampler.parentThreshold(ComposableSampler.alwaysOn()))
        .hasToString("ComposableParentThresholdSampler{rootSampler=ComposableAlwaysOnSampler}");
  }

  @Test
  void rootSpan() {
    assertThat(
            ComposableSampler.parentThreshold(ComposableSampler.alwaysOn())
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
  void parentWithThreshold() {
    SpanContext parentSpanContext =
        SpanContext.create(
            TraceId.fromLongs(1, 2),
            SpanId.fromLong(3),
            TraceFlags.getSampled(),
            TraceState.builder()
                .put("ot", new OtelTraceState(1, 10, Collections.emptyList()).serialize())
                .build());
    assertThat(
            ComposableSampler.parentThreshold(ComposableSampler.alwaysOn())
                .getSamplingIntent(
                    Context.root().with(Span.wrap(parentSpanContext)),
                    parentSpanContext.getTraceId(),
                    "span",
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getThreshold())
        .isEqualTo(10);
  }

  @Test
  void parentWithoutThresholdSampled() {
    SpanContext parentSpanContext =
        SpanContext.create(
            TraceId.fromLongs(1, 2),
            SpanId.fromLong(3),
            TraceFlags.getSampled(),
            TraceState.getDefault());
    SamplingIntent intent =
        ComposableSampler.parentThreshold(ComposableSampler.alwaysOn())
            .getSamplingIntent(
                Context.root().with(Span.wrap(parentSpanContext)),
                parentSpanContext.getTraceId(),
                "span",
                SpanKind.SERVER,
                Attributes.empty(),
                Collections.emptyList());
    assertThat(intent.getThreshold()).isZero();
    assertThat(intent.isThresholdReliable()).isFalse();
  }

  @Test
  void parentWithoutThresholdNotSampled() {
    SpanContext parentSpanContext =
        SpanContext.create(
            TraceId.fromLongs(1, 2),
            SpanId.fromLong(3),
            TraceFlags.getDefault(),
            TraceState.getDefault());
    SamplingIntent intent =
        ComposableSampler.parentThreshold(ComposableSampler.alwaysOn())
            .getSamplingIntent(
                Context.root().with(Span.wrap(parentSpanContext)),
                parentSpanContext.getTraceId(),
                "span",
                SpanKind.SERVER,
                Attributes.empty(),
                Collections.emptyList());
    assertThat(intent.getThreshold()).isEqualTo(-1);
    assertThat(intent.isThresholdReliable()).isFalse();
  }
}
