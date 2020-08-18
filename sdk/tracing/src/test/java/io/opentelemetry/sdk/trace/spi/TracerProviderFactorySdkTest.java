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

package io.opentelemetry.sdk.trace.spi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Tracer;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TracerProviderFactorySdk}. */
class TracerProviderFactorySdkTest {

  @Test
  void testDefault() {
    Tracer tracerSdk = TracerSdkProvider.builder().build().get("");
    assertThat(OpenTelemetry.getTracerProvider().get("")).isInstanceOf(tracerSdk.getClass());
  }
}
