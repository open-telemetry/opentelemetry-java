/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemporaryBuffersTest {

  @Test
  void chars() {
    TemporaryBuffers.clearChars();
    char[] buffer10 = TemporaryBuffers.chars(10);
    assertThat(buffer10).hasSize(10);
    char[] buffer8 = TemporaryBuffers.chars(8);
    // Buffer was reused even though smaller.
    assertThat(buffer8).isSameAs(buffer10);
    char[] buffer20 = TemporaryBuffers.chars(20);
    assertThat(buffer20).hasSize(20);
  }
}
