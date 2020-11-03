/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UtilsTest {
  private static final String TEST_MESSAGE = "test message";
  private static final String TEST_MESSAGE_TEMPLATE = "I ate %s eggs.";
  private static final int TEST_MESSAGE_VALUE = 2;
  private static final String FORMATTED_SIMPLE_TEST_MESSAGE = "I ate 2 eggs.";
  private static final String FORMATTED_COMPLEX_TEST_MESSAGE = "I ate 2 eggs. [2]";

  @Test
  void checkArgument() {
    Utils.checkArgument(true, TEST_MESSAGE);
    assertThrows(
        IllegalArgumentException.class,
        () -> Utils.checkArgument(false, TEST_MESSAGE),
        TEST_MESSAGE);
  }

  @Test
  void checkArgument_WithSimpleFormat() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Utils.checkArgument(false, TEST_MESSAGE_TEMPLATE, TEST_MESSAGE_VALUE),
        FORMATTED_SIMPLE_TEST_MESSAGE);
  }

  @Test
  void checkArgument_WithComplexFormat() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Utils.checkArgument(
                false, TEST_MESSAGE_TEMPLATE, TEST_MESSAGE_VALUE, TEST_MESSAGE_VALUE),
        FORMATTED_COMPLEX_TEST_MESSAGE);
  }

  @Test
  void checkState() {
    assertThrows(
        IllegalStateException.class, () -> Utils.checkState(false, TEST_MESSAGE), TEST_MESSAGE);
  }
}
