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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link StringUtils}. */
@RunWith(JUnit4.class)
public final class StringUtilsTest {

  @Test
  public void isPrintableString() {
    assertThat(StringUtils.isPrintableString("abcd")).isTrue();
    assertThat(StringUtils.isPrintableString("\2ab\3cd")).isFalse();
  }

  @Test
  public void isNullOrEmpty() throws Exception {
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
    } catch (Throwable e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void padLeft_throws_for_negative_length() {
    try {
      StringUtils.padLeft("value", -10);
    } catch (Throwable e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void padLeft_throws_for_zero_length() {
    try {
      StringUtils.padLeft("value", 0);
    } catch (Throwable e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void padLeft_length_does_not_exceed_length() {
    assertEquals(StringUtils.padLeft("value", 3), "value");
  }
}
