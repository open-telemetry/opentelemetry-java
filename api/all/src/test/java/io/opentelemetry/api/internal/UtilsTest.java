/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UtilsTest {
  private static final String TEST_MESSAGE = "test message";

  @Test
  void checkArgument() {
    Utils.checkArgument(true, TEST_MESSAGE);
    assertThatThrownBy(() -> Utils.checkArgument(false, TEST_MESSAGE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(TEST_MESSAGE);
  }
}
