/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.Test;

class LongExemplarDataTest {

  @Test
  void create() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    LongExemplarData exemplar =
        LongExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    assertThat(exemplar.getFilteredAttributes()).isEqualTo(attributes);
    assertThat(exemplar.getValue()).isEqualTo(1L);
    assertThat(exemplar.getSpanContext()).isNotNull();
  }
}
