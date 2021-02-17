/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.trace.SpanLimits;
import org.junit.jupiter.api.Test;

class SpanLimitsTest {

  @Test
  void defaultSpanLimits() {
    assertThat(SpanLimits.getDefault().getSpanAttributeLimit()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getSpanEventLimit()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getSpanLinkLimit()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getEventAttributeLimit()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getLinkAttributeLimit()).isEqualTo(128);
  }

  @Test
  void updateSpanLimits_All() {
    SpanLimits spanLimits =
        SpanLimits.builder()
            .setSpanAttributeLimit(8)
            .setSpanEventLimit(10)
            .setSpanLinkLimit(11)
            .setMaxNumberOfAttributesPerEvent(1)
            .setMaxNumberOfAttributesPerLink(2)
            .build();
    assertThat(spanLimits.getSpanAttributeLimit()).isEqualTo(8);
    assertThat(spanLimits.getSpanEventLimit()).isEqualTo(10);
    assertThat(spanLimits.getSpanLinkLimit()).isEqualTo(11);
    assertThat(spanLimits.getEventAttributeLimit()).isEqualTo(1);
    assertThat(spanLimits.getLinkAttributeLimit()).isEqualTo(2);

    // Preserves values
    SpanLimits spanLimitsDupe = spanLimits.toBuilder().build();
    // Use reflective comparison to catch when new fields are added.
    assertThat(spanLimitsDupe).usingRecursiveComparison().isEqualTo(spanLimits);
  }
}
