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

package io.opentelemetry.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemporaryBuffersTest {

  @Test
  void chars() {
    TemporaryBuffers.clearChars();
    char[] buffer10 = TemporaryBuffers.chars(10);
    assertThat(buffer10).hasSize(10);
    char[] buffer8 = TemporaryBuffers.chars(8);
    // Buffer was reused even though smaller.
    assertThat(buffer8).isSameAs(buffer10);
    char[] buffer20 = TemporaryBuffers.chars(20);
    assertThat(buffer20).hasSize(20);
  }
}
