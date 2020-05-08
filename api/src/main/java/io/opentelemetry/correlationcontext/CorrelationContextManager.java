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

package io.opentelemetry.correlationcontext;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Object for creating new {@link CorrelationContext}s and {@code CorrelationContext}s based on the
 * current context.
 *
 * <p>This class returns {@link CorrelationContext.Builder builders} that can be used to create the
 * implementation-dependent {@link CorrelationContext}s.
 *
 * <p>Implementations may have different constraints and are free to convert entry contexts to their
 * own subtypes.
 *
 * @since 0.1.0
 */
// TODO (trask) update class javadoc
@ThreadSafe
public interface CorrelationContextManager {

  /**
   * Returns a new {@code Builder}.
   *
   * @return a new {@code Builder}.
   * @since 0.1.0
   */
  CorrelationContext.Builder contextBuilder();
}
