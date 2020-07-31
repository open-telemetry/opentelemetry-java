/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
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
    Utils.checkNotNull(true, TEST_MESSAGE);
    assertThrows(
        IllegalStateException.class, () -> Utils.checkState(false, TEST_MESSAGE), TEST_MESSAGE);
  }

  @Test
  void checkNotNull() {
    Utils.checkNotNull(new Object(), TEST_MESSAGE);
    assertThrows(
        NullPointerException.class, () -> Utils.checkNotNull(null, TEST_MESSAGE), TEST_MESSAGE);
  }

  @Test
  void checkIndex_Valid() {
    Utils.checkIndex(1, 2);
  }

  @Test
  void checkIndex_NegativeSize() {
    assertThrows(
        IllegalArgumentException.class, () -> Utils.checkIndex(0, -1), "Negative size: -1");
  }

  @Test
  void checkIndex_NegativeIndex() {
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> Utils.checkIndex(-2, 10),
        "Index out of bounds: size=10, index=-2");
  }

  @Test
  void checkIndex_IndexEqualToSize() {
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> Utils.checkIndex(5, 5),
        "Index out of bounds: size=5, index=5");
  }

  @Test
  void checkIndex_IndexGreaterThanSize() {
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> Utils.checkIndex(11, 10),
        "Index out of bounds: size=10, index=11");
  }

  @Test
  void equalsObjects_Equal() {
    assertTrue(Utils.equalsObjects(null, null));
    assertTrue(Utils.equalsObjects(new Date(1L), new Date(1L)));
  }

  @Test
  void equalsObjects_Unequal() {
    assertFalse(Utils.equalsObjects(null, new Object()));
    assertFalse(Utils.equalsObjects(new Object(), null));
    assertFalse(Utils.equalsObjects(new Object(), new Object()));
  }
}
