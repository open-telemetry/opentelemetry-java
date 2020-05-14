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

package io.opentelemetry.scope;

import com.google.errorprone.annotations.MustBeClosed;
import io.grpc.Context;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.trace.Span;

// TODO (trask) javadoc class and methods
public interface ScopeManager {

  @MustBeClosed
  Scope withSpan(Span span);

  @MustBeClosed
  Scope withCorrelationContext(CorrelationContext correlationContext);

  /**
   * Binds the context to the current thread and returns a scope that must be used to unbind the
   * context from the current thread and restore the previously bound context (if any).
   */
  @MustBeClosed
  Scope withContext(Context context);

  Span getSpan();

  CorrelationContext getCorrelationContext();

  /** Returns the context bound to the current thread. */
  // TODO (trask) this method is not needed currently, in favor of using Context.current() directly,
  //      but if we move to a Context object that doesn't have built-in thread-binding, then this is
  //      the only additional method we would need
  // Context getContext();
}
