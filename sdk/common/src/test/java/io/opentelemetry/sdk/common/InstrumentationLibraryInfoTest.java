/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Tests for {@link InstrumentationLibraryInfo}. */
class InstrumentationLibraryInfoTest {

  @Test
  void emptyLibraryInfo() {
    assertThat(InstrumentationLibraryInfo.empty().getName()).isEmpty();
    assertThat(InstrumentationLibraryInfo.empty().getVersion()).isNull();
  }

  @Test
  void nullName() {
    assertThatThrownBy(() -> InstrumentationLibraryInfo.create(null, "1.0.0"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }
}
