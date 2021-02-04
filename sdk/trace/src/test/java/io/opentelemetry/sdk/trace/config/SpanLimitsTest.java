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
    assertThat(SpanLimits.getDefault().getMaxNumberOfAttributes()).isEqualTo(1000);
    assertThat(SpanLimits.getDefault().getMaxNumberOfEvents()).isEqualTo(1000);
    assertThat(SpanLimits.getDefault().getMaxNumberOfLinks()).isEqualTo(1000);
    assertThat(SpanLimits.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(32);
    assertThat(SpanLimits.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(32);
  }

  @Test
  void updateSpanLimits_All() {
    SpanLimits spanLimits =
        SpanLimits.builder()
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .setMaxNumberOfAttributesPerEvent(1)
            .setMaxNumberOfAttributesPerLink(2)
            .build();
    assertThat(spanLimits.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(spanLimits.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(spanLimits.getMaxNumberOfLinks()).isEqualTo(11);
    assertThat(spanLimits.getMaxNumberOfAttributesPerEvent()).isEqualTo(1);
    assertThat(spanLimits.getMaxNumberOfAttributesPerLink()).isEqualTo(2);

    // Preserves values
    SpanLimits spanLimitsDupe = spanLimits.toBuilder().build();
    // Use reflective comparison to catch when new fields are added.
    assertThat(spanLimitsDupe).usingRecursiveComparison().isEqualTo(spanLimits);
  }
}
