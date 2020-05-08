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

import com.google.errorprone.annotations.MustBeClosed;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.trace.Span;

/** Static methods for interacting with the current (thread-bound) context. */
// TODO (trask) javadoc class and methods
public class CurrentContext {

  @MustBeClosed
  public static Scope withSpan(Span span) {
    return withContext(get().withSpan(span));
  }

  @MustBeClosed
  public static Scope withCorrelationContext(CorrelationContext correlationContext) {
    return withContext(get().withCorrelationContext(correlationContext));
  }

  /**
   * Binds the context to the current thread and returns a scope that must be used to unbind the
   * context from the current thread and restore the previously bound context (if any).
   */
  @MustBeClosed
  public static Scope withContext(Context context) {
    return new DefaultScope(context);
  }

  /** Convenience method for {@code CurrentContext.get().getSpan()}. */
  public static Span getSpan() {
    return get().getSpan();
  }

  /** Convenience method for {@code CurrentContext.get().getCorrelationContext()}. */
  public static CorrelationContext getCorrelationContext() {
    return get().getCorrelationContext();
  }

  /** Returns the context bound to the current thread. */
  public static Context get() {
    return DefaultContextStorage.current();
  }

  private CurrentContext() {}

  static class DefaultScope implements Scope {

    private final Context priorContext;
    private final Context context;

    DefaultScope(Context context) {
      this.priorContext = DefaultContextStorage.doAttach(context);
      this.context = context;
    }

    @Override
    public void close() {
      DefaultContextStorage.detach(context, priorContext);
    }
  }
}
