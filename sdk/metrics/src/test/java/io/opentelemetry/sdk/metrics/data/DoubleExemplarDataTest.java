/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.ImmutableSpanContext;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.Test;

class DoubleExemplarDataTest {

  private static final String TRACE_ID = "00000000000000000000000000000061";
  private static final String SPAN_ID = "0000000000000061";

  @Test
  void create() {
    SpanContext spanContext =
        ImmutableSpanContext.create(
            TRACE_ID,
            SPAN_ID,
            TraceFlags.getDefault(),
            TraceState.getDefault(),
            /* remote= */ false,
            /* skipIdValidation= */ false);
    DoubleExemplarData exemplarData =
        DoubleExemplarData.create(
            Attributes.builder().put("key", "value1").build(), // attributes
            1, // epochNanos
            /* spanContext= */ spanContext,
            /* value= */ 2.0);
    assertThat(exemplarData.getValue()).isEqualTo(2.0);
    assertThat(exemplarData.getEpochNanos()).isEqualTo(1);
  }
}
