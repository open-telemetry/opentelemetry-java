/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal.asExemplarFilterInternal;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class ExemplarFilterTest {
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String SPAN_ID = "ff00000000000041";

  @Test
  void alwaysOff_NeverReturnsTrue() {
    assertThat(
            asExemplarFilterInternal(ExemplarFilter.alwaysOff())
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isFalse();
  }

  @Test
  void alwaysOn_AlwaysReturnsTrue() {
    assertThat(
            asExemplarFilterInternal(ExemplarFilter.alwaysOn())
                .shouldSampleMeasurement(1, Attributes.empty(), Context.root()))
        .isTrue();
  }

  @Test
  void withSampledTrace_ReturnsFalseOnNoContext() {
    assertThat(
            asExemplarFilterInternal(ExemplarFilter.traceBased())
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
    assertThat(
            asExemplarFilterInternal(ExemplarFilter.traceBased())
                .shouldSampleMeasurement(1, Attributes.empty(), context))
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
    assertThat(
            asExemplarFilterInternal(ExemplarFilter.traceBased())
                .shouldSampleMeasurement(1, Attributes.empty(), context))
        .isFalse();
  }

  @Test
  void setExemplarFilter() {
    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder().setExemplarFilter(ExemplarFilter.alwaysOn());
    assertThat(builder)
        .extracting(
            "exemplarFilter", as(InstanceOfAssertFactories.type(ExemplarFilterInternal.class)))
        .isEqualTo(ExemplarFilter.alwaysOn());
  }

  @Test
  void asExemplarFilterInternal_Valid() {
    assertThat(asExemplarFilterInternal(ExemplarFilter.traceBased()))
        .isSameAs(ExemplarFilter.traceBased());
    assertThat(asExemplarFilterInternal(ExemplarFilter.alwaysOff()))
        .isSameAs(ExemplarFilter.alwaysOff());
    assertThat(asExemplarFilterInternal(ExemplarFilter.alwaysOn()))
        .isSameAs(ExemplarFilter.alwaysOn());
    assertThatThrownBy(() -> asExemplarFilterInternal(new ExemplarFilter() {}))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void alwaysOff_toString() {
    assertThat(ExemplarFilter.alwaysOff().toString()).isEqualTo("AlwaysOffExemplarFilter");
  }

  @Test
  void alwaysOn_toString() {
    assertThat(ExemplarFilter.alwaysOn().toString()).isEqualTo("AlwaysOnExemplarFilter");
  }

  @Test
  void traceBased_toString() {
    assertThat(ExemplarFilter.traceBased().toString()).isEqualTo("TraceBasedExemplarFilter");
  }
}
