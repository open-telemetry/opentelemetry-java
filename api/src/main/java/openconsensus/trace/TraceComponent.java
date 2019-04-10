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
 * Class that holds the implementation instances for {@link Tracer}, and {@link
 * PropagationComponent}.
 *
 * <p>Unless otherwise noted all methods (on component) results are cacheable.
 *
 * @since 0.1.0
 */
public interface TraceComponent {

  /**
   * Returns the {@link Tracer} with the provided implementations. If no implementation is provided
   * then no-op implementations will be used.
   *
   * @return the {@code Tracer} implementation.
   * @since 0.1.0
   */
  Tracer getTracer();

  /**
   * Returns the {@link PropagationComponent} with the provided implementation. If no implementation
   * is provided then no-op implementation will be used.
   *
   * @return the {@code PropagationComponent} implementation.
   * @since 0.1.0
   */
  PropagationComponent getPropagationComponent();
}
