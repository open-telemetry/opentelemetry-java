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
import io.opentracing.Span;

@SuppressWarnings("deprecation")
final class ScopeShim implements Scope {
  final openconsensus.common.Scope scope;
  final SpanShim spanShim;
  final boolean finishSpanOnClose;

  public ScopeShim(
      openconsensus.common.Scope scope, openconsensus.trace.Span span, boolean finishSpanOnClose) {
    this.scope = scope;
    this.spanShim = new SpanShim(span);
    this.finishSpanOnClose = finishSpanOnClose;
  }

  @Override
  public Span span() {
    return spanShim;
  }

  @Override
  public void close() {
    if (scope != null) {
      // In case we are using a no-op Scope.
      scope.close();
    }

    // TODO - Make sure this is indeed the active Span (Tracer.getCurrentSpan())
    // else do not finish it.
    if (finishSpanOnClose) {
      spanShim.getSpan().end();
    }
  }
}
