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

package io.opentelemetry.context;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;

/**
 * This class provides access to for a global `ContextPropagators` object.
 *
 * <p>The default value is a no-op instance.
 */
public final class GlobalPropagators {
  private static final ContextPropagators DEFAULT_INSTANCE =
      DefaultContextPropagators.builder().build();

  private static volatile ContextPropagators propagators = DEFAULT_INSTANCE;

  private GlobalPropagators() {}

  static void reset() {
    propagators = DEFAULT_INSTANCE;
  }

  /**
   * Returns a global {@link ContextPropagators} object, which can be used to access the set of
   * registered propagators for each supported format.
   *
   * @return registered propagators container, defaulting to a no-op {@link ContextPropagators}
   *     object.
   * @since 0.4.0
   */
  public static ContextPropagators get() {
    return propagators;
  }

  /**
   * Sets the global {@link ContextPropagators} object, which can be used to access the set of
   * registered propagators for each supported format.
   *
   * @param globalPropagators the {@link ContextPropagators} object to be registered.
   * @throws NullPointerException if {@code globalPropagators} is {@code null}.
   * @since 0.4.0
   */
  public static void set(ContextPropagators globalPropagators) {
    if (globalPropagators == null) {
      throw new NullPointerException("globalPropagators");
    }

    propagators = globalPropagators;
  }
}
