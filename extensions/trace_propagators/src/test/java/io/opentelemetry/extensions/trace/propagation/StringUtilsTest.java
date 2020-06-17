/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.extensions.trace.propagation;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class StringUtilsTest {

  @Test
  public void isNullOrEmpty() {
    assertThat(StringUtils.isNullOrEmpty("")).isTrue();
    assertThat(StringUtils.isNullOrEmpty(null)).isTrue();
    assertThat(StringUtils.isNullOrEmpty("hello")).isFalse();
    assertThat(StringUtils.isNullOrEmpty(" ")).isFalse();
  }

  @Test
  public void padLeft() {
    assertEquals(StringUtils.padLeft("value", 10), "00000value");
  }

  @Test
  public void padLeft_throws_for_null_value() {
    try {
      StringUtils.padLeft(null, 10);
      fail("Expected exception not");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void padLeft_length_does_not_exceed_length() {
    assertEquals(StringUtils.padLeft("value", 3), "value");
    assertEquals(StringUtils.padLeft("value", -10), "value");
    assertEquals(StringUtils.padLeft("value", 0), "value");
  }
}
