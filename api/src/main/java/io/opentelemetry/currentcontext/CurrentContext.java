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

package io.opentelemetry.currentcontext;

import com.google.errorprone.annotations.MustBeClosed;
import io.grpc.Context;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.trace.Span;

/** Static methods for interacting with the current (thread-bound) context. */
// TODO (trask) javadoc class and methods
public class CurrentContext {

  @MustBeClosed
  public static Scope withSpan(Span span) {
    return withContext(get().withValue(Span.KEY, span));
  }

  @MustBeClosed
  public static Scope withCorrelationContext(CorrelationContext correlationContext) {
    return withContext(get().withValue(CorrelationContext.KEY, correlationContext));
  }

  /**
   * Binds the context to the current thread and returns a scope that must be used to unbind the
   * context from the current thread and restore the previously bound context (if any).
   */
  @MustBeClosed
  public static Scope withContext(Context context) {
    return new DefaultScope(context);
  }

  public static Span getSpan() {
    return Span.KEY.get();
  }

  public static CorrelationContext getCorrelationContext() {
    return CorrelationContext.KEY.get();
  }

  /** Returns the context bound to the current thread. */
  // TODO (trask) this method is not needed currently, in favor of using Context.current() directly,
  //      but if we move to a Context object that doesn't have built-in thread-binding, then this is
  //      the only additional method we would need
  private static Context get() {
    return Context.current();
  }

  private CurrentContext() {}

  static class DefaultScope implements Scope {

    private final Context priorContext;
    private final Context context;

    DefaultScope(Context context) {
      this.priorContext = context.attach();
      this.context = context;
    }

    @Override
    public void close() {
      context.detach(priorContext);
    }
  }
}
