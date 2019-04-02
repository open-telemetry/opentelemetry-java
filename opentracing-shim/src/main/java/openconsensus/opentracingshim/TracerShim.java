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
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

@SuppressWarnings("ReturnMissingNullable")
public final class TracerShim implements Tracer {
  private final openconsensus.trace.Tracer tracer;
  private final ScopeManager scopeManagerShim;

  public TracerShim(openconsensus.trace.Tracer tracer) {
    this.tracer = tracer;
    this.scopeManagerShim = new ScopeManagerShim(tracer);
  }

  @Override
  public ScopeManager scopeManager() {
    return scopeManagerShim;
  }

  @Override
  public Span activeSpan() {
    return scopeManagerShim.activeSpan();
  }

  @Override
  public Scope activateSpan(Span span) {
    return scopeManagerShim.activate(span);
  }

  @Override
  public SpanBuilder buildSpan(String operationName) {
    return new SpanBuilderShim(tracer, tracer.spanBuilder(operationName));
  }

  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {}

  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {
    return null;
  }

  @Override
  public void close() {
    // TODO
  }
}
