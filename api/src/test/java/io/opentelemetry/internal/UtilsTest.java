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

import java.util.Date;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Utils}. */
@RunWith(JUnit4.class)
public final class UtilsTest {
  private static final String TEST_MESSAGE = "test message";
  private static final String TEST_MESSAGE_TEMPLATE = "I ate %s eggs.";
  private static final int TEST_MESSAGE_VALUE = 2;
  private static final String FORMATTED_SIMPLE_TEST_MESSAGE = "I ate 2 eggs.";
  private static final String FORMATTED_COMPLEX_TEST_MESSAGE = "I ate 2 eggs. [2]";

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void checkArgument() {
    Utils.checkArgument(true, TEST_MESSAGE);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(TEST_MESSAGE);
    Utils.checkArgument(false, TEST_MESSAGE);
  }

  @Test
  public void checkArgument_NullErrorMessage() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("null");
    Utils.checkArgument(false, null);
  }

  @Test
  public void checkArgument_WithSimpleFormat() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(FORMATTED_SIMPLE_TEST_MESSAGE);
    Utils.checkArgument(false, TEST_MESSAGE_TEMPLATE, TEST_MESSAGE_VALUE);
  }

  @Test
  public void checkArgument_WithComplexFormat() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(FORMATTED_COMPLEX_TEST_MESSAGE);
    Utils.checkArgument(false, TEST_MESSAGE_TEMPLATE, TEST_MESSAGE_VALUE, TEST_MESSAGE_VALUE);
  }

  @Test
  public void checkState() {
    Utils.checkNotNull(true, TEST_MESSAGE);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(TEST_MESSAGE);
    Utils.checkState(false, TEST_MESSAGE);
  }

  @Test
  public void checkState_NullErrorMessage() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("null");
    Utils.checkState(false, null);
  }

  @Test
  public void checkNotNull() {
    Utils.checkNotNull(new Object(), TEST_MESSAGE);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(TEST_MESSAGE);
    Utils.checkNotNull(null, TEST_MESSAGE);
  }

  @Test
  public void checkNotNull_NullErrorMessage() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("null");
    Utils.checkNotNull(null, null);
  }

  @Test
  public void checkIndex_Valid() {
    Utils.checkIndex(1, 2);
  }

  @Test
  public void checkIndex_NegativeSize() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Negative size: -1");
    Utils.checkIndex(0, -1);
  }

  @Test
  public void checkIndex_NegativeIndex() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage("Index out of bounds: size=10, index=-2");
    Utils.checkIndex(-2, 10);
  }

  @Test
  public void checkIndex_IndexEqualToSize() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage("Index out of bounds: size=5, index=5");
    Utils.checkIndex(5, 5);
  }

  @Test
  public void checkIndex_IndexGreaterThanSize() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage("Index out of bounds: size=10, index=11");
    Utils.checkIndex(11, 10);
  }

  @Test
  public void equalsObjects_Equal() {
    assertTrue(Utils.equalsObjects(null, null));
    assertTrue(Utils.equalsObjects(new Date(1L), new Date(1L)));
  }

  @Test
  public void equalsObjects_Unequal() {
    assertFalse(Utils.equalsObjects(null, new Object()));
    assertFalse(Utils.equalsObjects(new Object(), null));
    assertFalse(Utils.equalsObjects(new Object(), new Object()));
  }
}
