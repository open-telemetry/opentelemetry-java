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

package io.opentelemetry.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkTest {

  @Test
  void testDefault() {
    assertThat(OpenTelemetrySdk.getTracerProvider().get(""))
        .isSameAs(OpenTelemetry.getTracerProvider().get(""));
    assertThat(OpenTelemetrySdk.getCorrelationContextManager())
        .isSameAs(OpenTelemetry.getCorrelationContextManager());
    assertThat(OpenTelemetrySdk.getMeterProvider()).isSameAs(OpenTelemetry.getMeterProvider());
  }

  @Test
  void testShortcutVersions() {
    assertThat(OpenTelemetry.getTracer("testTracer1"))
        .isEqualTo(OpenTelemetry.getTracerProvider().get("testTracer1"));
    assertThat(OpenTelemetry.getTracer("testTracer2", "testVersion"))
        .isEqualTo(OpenTelemetry.getTracerProvider().get("testTracer2", "testVersion"));
    assertThat(OpenTelemetry.getMeter("testMeter1"))
        .isEqualTo(OpenTelemetry.getMeterProvider().get("testMeter1"));
    assertThat(OpenTelemetry.getMeter("testMeter2", "testVersion"))
        .isEqualTo(OpenTelemetry.getMeterProvider().get("testMeter2", "testVersion"));
  }
}
