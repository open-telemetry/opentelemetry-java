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

import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.currentcontext.CurrentContext;
import io.opentelemetry.trace.Span;

// TODO (trask) once the current() method is removed (see below)
//      this could be turned into an interface
//      which improves interop possibilities with other libraries' contexts
//      but also limits the ability to evolve this class with additional methods,
//      e.g. withLoggingContext??? (insert sad Java 7 face here)
//
// TODO (trask) does this also need to support arbitrary key-values?
//
// TODO (trask) javadoc class and methods
public abstract class Context {

  public static Context empty() {
    return DefaultContext.EMPTY;
  }

  // TODO (trask) remove this method and use CurrentContext.get() directly instead
  //      so that this is a pure Context object, and has nothing to do with thread binding.
  //      this changes was not done yet in order to reduce code churn
  //      and make the PR easier to review as there are a lot of places
  //      that call Context.current().
  public static Context current() {
    return CurrentContext.get();
  }

  /**
   * Creates a new {@link Context} with the given {@link Span} set.
   *
   * @param span the {@link Span} to be set.
   * @return a new context with the given {@link Span} set.
   * @since 0.5.0
   */
  public abstract Context withSpan(Span span);

  /**
   * Creates a new {@link Context} with the given {@link CorrelationContext} set.
   *
   * @param correlationContext the {@link CorrelationContext} to be set.
   * @return a new context with the given {@link CorrelationContext} set.
   * @since 0.5.0
   */
  public abstract Context withCorrelationContext(CorrelationContext correlationContext);

  /**
   * Returns the {@link Span} from this context, falling back to an empty {@link Span}.
   *
   * @return the {@link Span} from this context.
   * @since 0.5.0
   */
  public abstract Span getSpan();

  /**
   * Returns the {@link CorrelationContext} from this context, falling back to an empty {@link
   * CorrelationContext}.
   *
   * @return the {@link CorrelationContext} from this context.
   * @since 0.5.0
   */
  public abstract CorrelationContext getCorrelationContext();
}
