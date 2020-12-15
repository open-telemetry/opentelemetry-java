/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UtilsTest {
  private static final String TEST_MESSAGE = "test message";

  @Test
  void checkArgument() {
    Utils.checkArgument(true, TEST_MESSAGE);
    assertThrows(
        IllegalArgumentException.class,
        () -> Utils.checkArgument(false, TEST_MESSAGE),
        TEST_MESSAGE);
  }
}
