/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace;

import openconsensus.trace.propagation.PropagationComponent;

/**
 * Class that manages a global instance of the {@link TraceComponent}.
 *
 * @since 0.1.0
 */
public final class Tracing {
  private static final TraceComponent traceComponent = TraceComponent.newNoopTraceComponent();

  /**
   * Returns the global {@link Tracer}.
   *
   * @return the global {@code Tracer}.
   * @since 0.1.0
   */
  public static Tracer getTracer() {
    return traceComponent.getTracer();
  }

  /**
   * Returns the global {@link PropagationComponent}.
   *
   * @return the global {@code PropagationComponent}.
   * @since 0.1.0
   */
  public static PropagationComponent getPropagationComponent() {
    return traceComponent.getPropagationComponent();
  }

  // No instance of this class.
  private Tracing() {}
}
