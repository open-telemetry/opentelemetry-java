/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

public class ExemplarFilterTest {
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String SPAN_ID = "ff00000000000041";

  @Test
  void never_NeverReturnsTrue() {
    assertThat(
            ExemplarFilter.ALWAYS_OFF.shouldSampleLongMeasurement(
                1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void withSampledTrace_ReturnsFalseOnNoContext() {
    assertThat(
            ExemplarFilter.WITH_TRACES.shouldSampleLongMeasurement(
                1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void withSampledTrace_sampleWithTrace() {
    final Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault())));
    assertThat(
            ExemplarFilter.WITH_TRACES.shouldSampleLongMeasurement(1, Attributes.empty(), context))
        .isTrue();
  }

  @Test
  void withSampledTrace_notSampleUnsampledTrace() {
    final Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault())));
    assertThat(
            ExemplarFilter.WITH_TRACES.shouldSampleDoubleMeasurement(
                1, Attributes.empty(), context))
        .isFalse();
  }
}
