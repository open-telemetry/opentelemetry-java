/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2018, OpenCensus Authors
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

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;

import io.opencensus.trace.propagation.PropagationComponent;
import org.junit.jupiter.api.Test;

public class OpenTelemetryPropagationComponentImplTest {
  private final PropagationComponent propagationComponent =
      new OpenTelemetryPropagationComponentImpl();

  @Test
  public void implementationOfBinary() {
    assertThat(propagationComponent.getBinaryFormat().getClass().getName())
        .isEqualTo("io.opencensus.implcore.trace.propagation.BinaryFormatImpl");
  }

  @Test
  public void implementationOfB3Format() {
    assertThat(propagationComponent.getB3Format()).isInstanceOf(OpenTelemetryTextFormatImpl.class);
  }

  @Test
  public void implementationOfTraceContextFormat() {
    assertThat(propagationComponent.getTraceContextFormat())
        .isInstanceOf(OpenTelemetryTextFormatImpl.class);
  }
}
