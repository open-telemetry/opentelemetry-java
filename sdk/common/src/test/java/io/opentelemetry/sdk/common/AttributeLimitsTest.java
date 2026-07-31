/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class AttributeLimitsTest {

  @Test
  void builder_defaultsToSpecRecommended() {
    AttributeLimits limits = AttributeLimits.builder().build();
    assertThat(limits.getCountLimit()).isEqualTo(128);
    assertThat(limits.getValueLengthLimit()).isEqualTo(Integer.MAX_VALUE);
    assertThat(limits.getValueDepthLimit()).isEqualTo(64);
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
    AttributeLimits limits =
        AttributeLimits.builder().setCountLimit(0).setValueLengthLimit(0).build();
    assertThat(limits.getCountLimit()).isEqualTo(0);
    assertThat(limits.getValueLengthLimit()).isEqualTo(0);
  }

  @Test
  void builder_rejectsDepthBelowOne() {
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
}
