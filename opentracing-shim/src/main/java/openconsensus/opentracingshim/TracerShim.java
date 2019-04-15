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
import openconsensus.trace.propagation.BinaryFormat;
import openconsensus.trace.propagation.TextFormat;

public final class TracerShim implements Tracer {
  private final openconsensus.trace.Tracer tracer;
  private final openconsensus.trace.propagation.TextFormat[] textFormats;
  private final openconsensus.trace.propagation.BinaryFormat binaryFormat;
  private final ScopeManager scopeManagerShim;

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of the {@code openconsensus.trace.Tracer} and
   * the {@code openconsensus.trace.propagation.PropagationComponent} exposed by {@code
   * openconsensus.trace.Tracing}.
   *
   * @since 0.1.0
   */
  public TracerShim() {
    this(
        openconsensus.trace.Tracing.getTracer(),
        openconsensus.trace.Tracing.getPropagationComponent().getBinaryFormat(),
        openconsensus.trace.Tracing.getPropagationComponent().getTraceContextFormat(),
        openconsensus.trace.Tracing.getPropagationComponent().getB3Format());
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of a {@code openconsensus.trace.Tracer} and
   * the specified {@code io.openconsensus.trace.propagation.TextFormat} values.
   *
   * @param tracer the {@code openconsensus.trace.Tracer} used by this shim.
   * @param textFormats the {@code openconsensus.trace.propagation.TextFormat} values used for text
   *     injection and extraction.
   * @since 0.1.0
   */
  public TracerShim(openconsensus.trace.Tracer tracer, TextFormat... textFormats) {
    this(tracer, null, textFormats);
  }

  /**
   * Creates a {@code io.opentracing.Tracer} shim out of a {@code openconsensus.trace.Tracer} and
   * the specified {@code io.openconsensus.trace.propagation.TextFormat} values.
   *
   * @param tracer the {@code openconsensus.trace.Tracer} used by this shim.
   * @param binaryFormat the {@code openconsensus.trace.propagation.BinaryFormat} used for binary
   *     injection and extraction.
   * @param textFormats the {@code openconsensus.trace.propagation.TextFormat} values used for text
   *     injection and extraction.
   * @since 0.1.0
   */
  public TracerShim(
      openconsensus.trace.Tracer tracer, BinaryFormat binaryFormat, TextFormat... textFormats) {
    this.tracer = tracer;
    this.binaryFormat = binaryFormat;
    this.textFormats = textFormats;
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

    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      Propagation.injectTextFormat(textFormats, actualContext, (TextMap) carrier);
    } else if (format == Format.Builtin.BINARY) {
      if (binaryFormat != null) {
        Propagation.injectBinaryFormat(binaryFormat, actualContext, (Binary) carrier);
      }
    }
  }

  @SuppressWarnings("ReturnMissingNullable")
  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {

    SpanContext context = null;

    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      context = Propagation.extractTextFormat(textFormats, (TextMap) carrier);
    } else if (format == Format.Builtin.BINARY) {
      if (binaryFormat != null) {
        context = Propagation.extractBinaryFormat(binaryFormat, (Binary) carrier);
      }
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
