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
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import openconsensus.trace.Trace;

public final class TracerShim implements Tracer {
  private final openconsensus.trace.Tracer tracer;
  private final ScopeManager scopeManagerShim;

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of the {@code openconsensus.trace.Tracer}
   * exposed by {@code openconsensus.trace.Trace}.
   *
   * @since 0.1.0
   */
  public TracerShim() {
    this(Trace.getTracer());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of a {@code openconsensus.trace.Tracer}.
   *
   * @param tracer the {@code openconsensus.trace.Tracer} used by this shim.
   * @since 0.1.0
   */
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
    return new SpanBuilderShim(tracer.spanBuilder(operationName));
  }

  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {
    openconsensus.trace.SpanContext actualContext = getActualContext(context);

    // TODO - Shall we expect to get no-op objects if a given format is not supported at all?
    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      Propagation.injectTextFormat(tracer.getTextFormat(), actualContext, (TextMap) carrier);
    } else if (format == Format.Builtin.BINARY) {
      Propagation.injectBinaryFormat(tracer.getBinaryFormat(), actualContext, (Binary) carrier);
    }
  }

  @SuppressWarnings("ReturnMissingNullable")
  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {

    SpanContext context = null;

    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      context = Propagation.extractTextFormat(tracer.getTextFormat(), (TextMap) carrier);
    } else if (format == Format.Builtin.BINARY) {
      context = Propagation.extractBinaryFormat(tracer.getBinaryFormat(), (Binary) carrier);
    }

    return context;
  }

  @Override
  public void close() {
    // TODO
  }

  static openconsensus.trace.SpanContext getActualContext(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException("context is not a valid SpanContextShim object");
    }

    return ((SpanContextShim) context).getSpanContext();
  }
}
