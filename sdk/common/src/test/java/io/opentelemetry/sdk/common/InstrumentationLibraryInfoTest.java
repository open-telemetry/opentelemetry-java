/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Tests for {@link io.opentelemetry.sdk.common.InstrumentationLibraryInfo}. */
@SuppressWarnings("deprecation") // Testing deprecated code
class InstrumentationLibraryInfoTest {

  @Test
  void emptyLibraryInfo() {
    assertThat(io.opentelemetry.sdk.common.InstrumentationLibraryInfo.empty().getName()).isEmpty();
    assertThat(io.opentelemetry.sdk.common.InstrumentationLibraryInfo.empty().getVersion())
        .isNull();
  }

  @Test
  void nullName() {
    assertThatThrownBy(
            () -> io.opentelemetry.sdk.common.InstrumentationLibraryInfo.create(null, "1.0.0"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }
}
