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
  private final TracerShim tracerShim;

  public ScopeManagerShim(TracerShim tracerShim) {
    this.tracerShim = tracerShim;
  }

  @Override
  public Span activeSpan() {
    // TODO - is there a way to cleanly support baggage/tags here?
    return new SpanShim(tracerShim, tracerShim.tracer().getCurrentSpan());
  }

  @Override
  public Scope active() {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public Scope activate(Span span) {
    openconsensus.trace.Span actualSpan = getActualSpan(span);
    return new ScopeShim(tracerShim.tracer().withSpan(actualSpan));
  }

  @Override
  public Scope activate(Span span, boolean finishSpanOnClose) {
    throw new UnsupportedOperationException();
  }

  static openconsensus.trace.Span getActualSpan(Span span) {
    if (!(span instanceof SpanShim)) {
      throw new IllegalArgumentException("span is not a valid SpanShim object");
    }

    return ((SpanShim) span).getSpan();
  }
}
