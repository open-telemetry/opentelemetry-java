/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class ExemplarFilterTest {
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String SPAN_ID = "ff00000000000041";

  @Test
  void never_NeverReturnsTrue() {
    assertThat(
            ExemplarFilter.neverSample()
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void always_AlwaysReturnsTrue() {
    assertThat(
            ExemplarFilter.alwaysSample()
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isTrue();
  }

  @Test
  void withSampledTrace_ReturnsFalseOnNoContext() {
    assertThat(
            ExemplarFilter.sampleWithTraces()
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void withSampledTrace_sampleWithTrace() {
    Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault())));
    assertThat(
            ExemplarFilter.sampleWithTraces()
                .shouldSampleMeasurement(1, Attributes.empty(), context))
        .isTrue();
  }

  @Test
  void withSampledTrace_notSampleUnsampledTrace() {
    Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault())));
    assertThat(
            ExemplarFilter.sampleWithTraces()
                .shouldSampleMeasurement(1, Attributes.empty(), context))
        .isFalse();
  }

  @Test
  void setExemplarFilter() {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    ExemplarFilter.setExemplarFilter(builder, ExemplarFilter.alwaysSample());
    assertThat(builder)
        .extracting("exemplarFilter", as(InstanceOfAssertFactories.type(ExemplarFilter.class)))
        .isEqualTo(ExemplarFilter.alwaysSample());
  }
}
