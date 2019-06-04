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

package io.opentelemetry.opentracingshim;

import io.opentelemetry.OpenTelemetry;
import io.opentracing.Tracer;

public final class TraceShim {
  private TraceShim() {}

  /**
   * Creates a {@code io.opentracing.Tracer} shim around {@code OpenTelemetry.getTracer()}.
   *
   * @return a {@code io.opentracing.Tracer}.
   * @since 0.1.0
   */
  public static Tracer createTracerShim() {
    return new TracerShim(OpenTelemetry.getTracer());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim around the specified {@code Tracer}.
   *
   * @param tracer the {@code Tracer} used by this shim.
   * @return a {@code io.opentracing.Tracer}.
   * @since 0.1.0
   */
  public static Tracer createTracerShim(io.opentelemetry.trace.Tracer tracer) {
    return new TracerShim(tracer);
  }
}
