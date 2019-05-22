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

package io.opentelemetry.sdk.trace.config;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceParams}. */
@RunWith(JUnit4.class)
public class TraceParamsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultTraceParams() {
    assertThat(TraceParams.DEFAULT.getMaxNumberOfAttributes()).isEqualTo(32);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfEvents()).isEqualTo(128);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfLinks()).isEqualTo(32);
  }

  @Test
  public void updateTraceParams_NonPositiveMaxNumberOfAttributes() {
    thrown.expect(IllegalArgumentException.class);
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfAttributes(0).build();
  }

  @Test
  public void updateTraceParams_NonPositiveMaxNumberOfEvents() {
    thrown.expect(IllegalArgumentException.class);
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfEvents(0).build();
  }

  @Test
  public void updateTraceParams_NonPositiveMaxNumberOfLinks() {
    thrown.expect(IllegalArgumentException.class);
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfLinks(0).build();
  }

  @Test
  public void updateTraceParams_All() {
    TraceParams traceParams =
        TraceParams.DEFAULT
            .toBuilder()
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .build();
    assertThat(traceParams.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceParams.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(11);
  }
}
