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
import io.opentelemetry.correlationcontext.EmptyCorrelationContext;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;

final class DefaultContext extends Context {

  static final Context EMPTY = new DefaultContext(null, null);

  private final Span span;
  private final CorrelationContext correlationContext;

  DefaultContext(Span span, CorrelationContext correlationContext) {
    this.span = span;
    this.correlationContext = correlationContext;
  }

  @Override
  public Context withSpan(Span span) {
    return new DefaultContext(span, correlationContext);
  }

  @Override
  public Context withCorrelationContext(CorrelationContext correlationContext) {
    return new DefaultContext(span, correlationContext);
  }

  @Override
  public Span getSpan() {
    return span == null ? DefaultSpan.getInvalid() : span;
  }

  // TODO (trask) is this needed?
  @Override
  public Span getSpanWithoutDefault() {
    return span;
  }

  // TODO (trask) do we need getCorrelationContextWithoutDefault()?
  //      because if we don't need either of the  ..WithoutDefault() methods,
  //      then we can just initialize span to invalid and correlationContext to empty
  @Override
  public CorrelationContext getCorrelationContext() {
    return correlationContext == null ? EmptyCorrelationContext.getInstance() : correlationContext;
  }
}
