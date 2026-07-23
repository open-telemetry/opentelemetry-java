/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class AttributeLimitsTest {

  @Test
  void noLimits_returnsUnbounded() {
    AttributeLimits limits = AttributeLimits.noLimits();
    assertThat(limits.getCountLimit()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getValueLengthLimit()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getValueDepthLimit()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void noLimits_isCached() {
    assertThat(AttributeLimits.noLimits()).isSameAs(AttributeLimits.noLimits());
  }

  @Test
  void builder_defaultsToUnbounded() {
    AttributeLimits limits = AttributeLimits.builder().build();
    assertThat(limits.getCountLimit()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getValueLengthLimit()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getValueDepthLimit()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void builder_setsFields() {
    AttributeLimits limits =
        AttributeLimits.builder()
            .setCountLimit(128)
            .setValueLengthLimit(1024)
            .setValueDepthLimit(64)
            .build();
    assertThat(limits.getCountLimit()).isEqualTo(128);
    assertThat(limits.getValueLengthLimit()).isEqualTo(1024);
    assertThat(limits.getValueDepthLimit()).isEqualTo(64);
  }

  @Test
  void builder_rejectsNegativeCount() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> AttributeLimits.builder().setCountLimit(-1));
  }

  @Test
  void builder_rejectsNegativeValueLength() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> AttributeLimits.builder().setValueLengthLimit(-1));
  }

  @Test
  void builder_acceptsZeroForCountAndLength() {
    // Spec-consistent: count=0 drops everything, length=0 truncates all strings to empty.
    AttributeLimits limits =
        AttributeLimits.builder().setCountLimit(0).setValueLengthLimit(0).build();
    assertThat(limits.getCountLimit()).isEqualTo(0);
    assertThat(limits.getValueLengthLimit()).isEqualTo(0);
  }

  @Test
  void builder_rejectsDepthBelowOne() {
    // Depth is 1-indexed (top-level = depth 1), so a limit below 1 is meaningless.
    assertThatIllegalArgumentException()
        .isThrownBy(() -> AttributeLimits.builder().setValueDepthLimit(0));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> AttributeLimits.builder().setValueDepthLimit(-1));
  }

  @Test
  void builder_acceptsDepthOne() {
    AttributeLimits limits = AttributeLimits.builder().setValueDepthLimit(1).build();
    assertThat(limits.getValueDepthLimit()).isEqualTo(1);
  }

  @Test
  void toBuilder_preservesFields() {
    AttributeLimits original =
        AttributeLimits.builder()
            .setCountLimit(64)
            .setValueLengthLimit(512)
            .setValueDepthLimit(8)
            .build();
    AttributeLimits copy = original.toBuilder().build();
    assertThat(copy.getCountLimit()).isEqualTo(64);
    assertThat(copy.getValueLengthLimit()).isEqualTo(512);
    assertThat(copy.getValueDepthLimit()).isEqualTo(8);
  }

  @Test
  void equals_valueBased() {
    AttributeLimits a =
        AttributeLimits.builder()
            .setCountLimit(10)
            .setValueLengthLimit(20)
            .setValueDepthLimit(3)
            .build();
    AttributeLimits b =
        AttributeLimits.builder()
            .setCountLimit(10)
            .setValueLengthLimit(20)
            .setValueDepthLimit(3)
            .build();
    AttributeLimits c =
        AttributeLimits.builder()
            .setCountLimit(10)
            .setValueLengthLimit(21)
            .setValueDepthLimit(3)
            .build();
    AttributeLimits d =
        AttributeLimits.builder()
            .setCountLimit(10)
            .setValueLengthLimit(20)
            .setValueDepthLimit(4)
            .build();
    assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    assertThat(a).isNotEqualTo(c);
    assertThat(a).isNotEqualTo(d);
  }
}
