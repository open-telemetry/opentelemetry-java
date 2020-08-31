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

package io.opentelemetry.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceFlags}. */
class TraceFlagsTest {

  @Test
  void isDefaultSampled() {
    assertThat(TraceFlags.getDefault()).isFalse();
  }

  @Test
  void toFromBase16() {
    assertThat(TraceFlags.isSampledFromHex("ff", 0)).isTrue();
    assertThat(TraceFlags.isSampledFromHex("01", 0)).isTrue();
    assertThat(TraceFlags.isSampledFromHex("05", 0)).isTrue();
    assertThat(TraceFlags.isSampledFromHex("00", 0)).isFalse();
  }
}
