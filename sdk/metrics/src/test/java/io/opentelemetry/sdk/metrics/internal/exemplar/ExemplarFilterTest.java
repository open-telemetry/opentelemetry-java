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
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class ExemplarFilterTest {
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String SPAN_ID = "ff00000000000041";

  @Test
  void alwaysOff_NeverReturnsTrue() {
    assertThat(
            ExemplarFilter.alwaysOff()
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void alwaysOn_AlwaysReturnsTrue() {
    assertThat(
            ExemplarFilter.alwaysOn()
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isTrue();
  }

  @Test
  void withSampledTrace_ReturnsFalseOnNoContext() {
    assertThat(
            ExemplarFilter.traceBased()
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void traceBased_sampleWithTrace() {
    Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault())));
    assertThat(ExemplarFilter.traceBased().shouldSampleMeasurement(1, Attributes.empty(), context))
        .isTrue();
  }

  @Test
  void traceBased_notSampleUnsampledTrace() {
    Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault())));
    assertThat(ExemplarFilter.traceBased().shouldSampleMeasurement(1, Attributes.empty(), context))
        .isFalse();
  }

  @Test
  void setExemplarFilter() {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    SdkMeterProviderUtil.setExemplarFilter(builder, ExemplarFilter.alwaysOn());
    assertThat(builder)
        .extracting("exemplarFilter", as(InstanceOfAssertFactories.type(ExemplarFilter.class)))
        .isEqualTo(ExemplarFilter.alwaysOn());
  }
}
