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
import io.opentelemetry.trace.Span;

// TODO (trask) javadoc class and methods
public interface Context {

  Context EMPTY = DefaultContext.EMPTY;

  /**
   * Creates a new {@link Context} with the given {@link Span} set.
   *
   * @param span the {@link Span} to be set.
   * @return a new context with the given {@link Span} set.
   * @since 0.5.0
   */
  Context withSpan(Span span);

  /**
   * Creates a new {@link Context} with the given {@link CorrelationContext} set.
   *
   * @param correlationContext the {@link CorrelationContext} to be set.
   * @return a new context with the given {@link CorrelationContext} set.
   * @since 0.5.0
   */
  Context withCorrelationContext(CorrelationContext correlationContext);

  /**
   * Returns the {@link Span} from this context, falling back to an empty {@link Span}.
   *
   * @return the {@link Span} from this context.
   * @since 0.5.0
   */
  Span getSpan();

  /**
   * Returns the {@link CorrelationContext} from this context, falling back to an empty {@link
   * CorrelationContext}.
   *
   * @return the {@link CorrelationContext} from this context.
   * @since 0.5.0
   */
  CorrelationContext getCorrelationContext();
}
