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

package io.opentelemetry.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LabelValue}. */
@RunWith(JUnit4.class)
public class LabelValueTest {

  private static final LabelValue VALUE = LabelValue.create("value");
  private static final LabelValue UNSET = LabelValue.create(null);
  private static final LabelValue EMPTY = LabelValue.create("");

  @Test
  public void testGetValue() {
    assertThat(VALUE.getValue()).isEqualTo("value");
    assertThat(UNSET.getValue()).isNull();
    assertThat(EMPTY.getValue()).isEmpty();
  }

  @Test
  public void create_NoLengthConstraint() {
    // We have a length constraint of 256-characters for AttributeValue. That constraint doesn't
    // apply to
    // LabelValue.
    char[] chars = new char[300];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    assertThat(LabelValue.create(value).getValue()).isEqualTo(value);
  }

  @Test
  public void create_WithUnprintableChars() {
    String value = "\2ab\3cd";
    assertThat(LabelValue.create(value).getValue()).isEqualTo(value);
  }

  @Test
  public void create_WithNonAsciiChars() {
    String value = "值";
    LabelValue nonAsciiValue = LabelValue.create(value);
    assertThat(nonAsciiValue.getValue()).isEqualTo(value);
  }

  @Test
  public void testLabelValueEquals() {
    new EqualsTester()
        .addEqualityGroup(LabelValue.create("foo"), LabelValue.create("foo"))
        .addEqualityGroup(UNSET)
        .addEqualityGroup(EMPTY)
        .addEqualityGroup(LabelValue.create("bar"))
        .testEquals();
  }
}
