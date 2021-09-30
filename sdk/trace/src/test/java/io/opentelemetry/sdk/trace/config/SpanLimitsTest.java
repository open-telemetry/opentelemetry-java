/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.trace.SpanLimits;
import org.junit.jupiter.api.Test;

class SpanLimitsTest {

  @Test
  void defaultSpanLimits() {
    assertThat(SpanLimits.getDefault().getMaxNumberOfAttributes()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getMaxNumberOfEvents()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getMaxNumberOfLinks()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(128);
    assertThat(SpanLimits.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(128);
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

  @Test
  void invalidSpanLimits() {
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfAttributes(0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfAttributes(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfEvents(0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfEvents(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfLinks(0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfLinks(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfAttributesPerEvent(0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfAttributesPerEvent(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfAttributesPerLink(0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxNumberOfAttributesPerLink(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpanLimits.builder().setMaxAttributeValueLength(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
