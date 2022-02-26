/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InstrumentationScopeInfoTest {

  @Test
  void emptyScopeInfo() {
    assertThat(InstrumentationScopeInfo.empty().getName()).isEmpty();
    assertThat(InstrumentationScopeInfo.empty().getVersion()).isNull();
  }

  @Test
  void nullName() {
    assertThatThrownBy(() -> InstrumentationScopeInfo.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }
}
