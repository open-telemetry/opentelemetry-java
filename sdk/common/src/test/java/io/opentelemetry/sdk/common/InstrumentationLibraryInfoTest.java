/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for {@link InstrumentationLibraryInfo}. */
class InstrumentationLibraryInfoTest {

  @Test
  void emptyLibraryInfo() {
    assertThat(InstrumentationLibraryInfo.getEmpty().getName()).isEmpty();
    assertThat(InstrumentationLibraryInfo.getEmpty().getVersion()).isNull();
  }

  @Test
  void nullName() {
    assertThrows(
        NullPointerException.class, () -> InstrumentationLibraryInfo.create(null, "1.0.0"), "name");
  }
}
