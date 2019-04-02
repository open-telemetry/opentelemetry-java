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

package openconsensus.opentracingshim;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

@SuppressWarnings("deprecation")
final class ScopeManagerShim implements ScopeManager {
  private final openconsensus.trace.Tracer tracer;

  public ScopeManagerShim(openconsensus.trace.Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public Span activeSpan() {
    // TODO - getCurrentSpan() returns a no-op instance
    // if there's no actual active Span, so we need to
    // handle this case when building new ones (as they
    // will try to become children from this no-op Span).
    return new SpanShim(tracer.getCurrentSpan());
  }

  @Override
  public Scope active() {
    // TODO - If we decide to bridge against an OT version that
    // still has this Deprecated API, we have to keep track of the
    // actual Scope objects we create.
    openconsensus.trace.Span span = tracer.getCurrentSpan();
    return new ScopeShim(null, span, /* finishSpanOnClose= */ false);
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public Scope activate(Span span, boolean finishSpanOnClose) {
    openconsensus.trace.Span actualSpan = getActualSpan(span);
    return new ScopeShim(tracer.withSpan(actualSpan), actualSpan, finishSpanOnClose);
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public Scope activate(Span span) {
    openconsensus.trace.Span actualSpan = getActualSpan(span);
    return new ScopeShim(tracer.withSpan(actualSpan), actualSpan, /* finishSpanOnClose= */ false);
  }

  static openconsensus.trace.Span getActualSpan(Span span) {
    if (!(span instanceof SpanShim)) {
      throw new IllegalArgumentException("span is not a valid SpanShim object");
    }

    return ((SpanShim) span).getSpan();
  }
}
