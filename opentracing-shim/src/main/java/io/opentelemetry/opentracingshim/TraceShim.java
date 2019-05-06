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

import io.opentelemetry.trace.Trace;
import io.opentracing.Tracer;

public final class TraceShim {
  private TraceShim() {}

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of the {@code Tracer} exposed by {@code
   * Trace}.
   *
   * @since 0.1.0
   */
  public Tracer newTracerShim() {
    return new TracerShim(Trace.getTracer());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of a {@code Tracer}.
   *
   * @param tracer the {@code Tracer} used by this shim.
   * @since 0.1.0
   */
  public Tracer newTracerShim(io.opentelemetry.trace.Tracer tracer) {
    return new TracerShim(tracer);
  }
}
