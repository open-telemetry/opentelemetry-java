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
    assertThat(limits.getCapacity()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getLengthLimit()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void noLimits_isCached() {
    assertThat(AttributeLimits.noLimits()).isSameAs(AttributeLimits.noLimits());
  }

  @Test
  void builder_defaultsToUnbounded() {
    AttributeLimits limits = AttributeLimits.builder().build();
    assertThat(limits.getCapacity()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getLengthLimit()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void builder_setsFields() {
    AttributeLimits limits =
        AttributeLimits.builder().setCapacity(128).setLengthLimit(1024).build();
    assertThat(limits.getCapacity()).isEqualTo(128);
    assertThat(limits.getLengthLimit()).isEqualTo(1024);
  }

  @Test
  void builder_rejectsNegative() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> AttributeLimits.builder().setCapacity(-1));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> AttributeLimits.builder().setLengthLimit(-1));
  }

  @Test
  void toBuilder_preservesFields() {
    AttributeLimits original =
        AttributeLimits.builder().setCapacity(64).setLengthLimit(512).build();
    AttributeLimits copy = original.toBuilder().build();
    assertThat(copy.getCapacity()).isEqualTo(64);
    assertThat(copy.getLengthLimit()).isEqualTo(512);
  }

  @Test
  void equals_valueBased() {
    AttributeLimits a = AttributeLimits.builder().setCapacity(10).setLengthLimit(20).build();
    AttributeLimits b = AttributeLimits.builder().setCapacity(10).setLengthLimit(20).build();
    AttributeLimits c = AttributeLimits.builder().setCapacity(10).setLengthLimit(21).build();
    assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    assertThat(a).isNotEqualTo(c);
  }
}
