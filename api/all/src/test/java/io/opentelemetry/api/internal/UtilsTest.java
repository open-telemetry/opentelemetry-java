/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.logging.Level;
import java.util.logging.Logger;
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

  @Test
  void warnOnArgument() {
    Logger logger = mock(Logger.class);
    Utils.warnOnArgument(logger, false, TEST_MESSAGE);
    verify(logger, times(1)).log(Level.WARNING, TEST_MESSAGE);
  }
}
