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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void isNullOrEmpty() {
    assertThat(StringUtils.isNullOrEmpty("")).isTrue();
    assertThat(StringUtils.isNullOrEmpty(null)).isTrue();
    assertThat(StringUtils.isNullOrEmpty("hello")).isFalse();
    assertThat(StringUtils.isNullOrEmpty(" ")).isFalse();
  }

  @Test
  void padLeft() {
    assertThat(StringUtils.padLeft("value", 10)).isEqualTo("00000value");
  }

  @Test
  void padLeft_throws_for_null_value() {
    assertThrows(NullPointerException.class, () -> StringUtils.padLeft(null, 10));
  }

  @Test
  void padLeft_length_does_not_exceed_length() {
    assertThat(StringUtils.padLeft("value", 3)).isEqualTo("value");
    assertThat(StringUtils.padLeft("value", -10)).isEqualTo("value");
    assertThat(StringUtils.padLeft("value", 0)).isEqualTo("value");
  }
}
