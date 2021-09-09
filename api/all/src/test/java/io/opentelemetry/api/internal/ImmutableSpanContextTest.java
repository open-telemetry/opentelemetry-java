/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.Test;

public class ImmutableSpanContextTest {
  private static final String TRACE_ID = "00000000000000000000000000000061";
  private static final String SPAN_ID = "0000000000000061";

  @Test
  public void testWithIdValidationAndValidIds() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault(), false, false);

    assertThat(spanContext.isValid()).isTrue();
  }

  @Test
  public void testWithIdValidationAndInvalidTraceId() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TraceId.getInvalid(),
            SPAN_ID,
            TraceFlags.getDefault(),
            TraceState.getDefault(),
            false,
            false);

    assertThat(spanContext.isValid()).isFalse();
  }

  @Test
  public void testWithIdValidationAndInvalidSpanId() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TRACE_ID,
            SpanId.getInvalid(),
            TraceFlags.getDefault(),
            TraceState.getDefault(),
            false,
            false);

    assertThat(spanContext.isValid()).isFalse();
  }

  @Test
  public void testSkipIdValidationAndValidIds() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault(), false, true);

    assertThat(spanContext.isValid()).isTrue();
  }

  @Test
  public void testSkipIdValidationAndInvalidTraceId() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TraceId.getInvalid(),
            SPAN_ID,
            TraceFlags.getDefault(),
            TraceState.getDefault(),
            false,
            true);

    assertThat(spanContext.isValid()).isTrue();
  }

  @Test
  public void testSkipIdValidationAndInvalidSpanId() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TRACE_ID,
            SpanId.getInvalid(),
            TraceFlags.getDefault(),
            TraceState.getDefault(),
            false,
            true);

    assertThat(spanContext.isValid()).isTrue();
  }
}
