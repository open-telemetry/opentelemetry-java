/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.opentracingshim;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;

final class TracerShim implements Tracer {
  private final io.opentelemetry.trace.Tracer tracer;
  private final ScopeManager scopeManagerShim;

  TracerShim(io.opentelemetry.trace.Tracer tracer) {
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
    return new SpanBuilderShim(tracer, operationName);
  }

  // TODO - do not fail in case the context was null!
  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {
    io.opentelemetry.trace.SpanContext actualContext = getActualContext(context);

    // TODO - Shall we expect to get no-op objects if a given format is not supported at all?
    if (format == Format.Builtin.TEXT_MAP
        || format == Format.Builtin.TEXT_MAP_INJECT
        || format == Format.Builtin.HTTP_HEADERS) {
      Propagation.injectTextFormat(
          tracer.getHttpTextFormat(), actualContext, (TextMapInject) carrier);
    } else if (format == Format.Builtin.BINARY) {
      Propagation.injectBinaryFormat(tracer.getBinaryFormat(), actualContext, (Binary) carrier);
    }
  }

  @SuppressWarnings("ReturnMissingNullable")
  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {
    SpanContext context = null;

    if (format == Format.Builtin.TEXT_MAP
        || format == Format.Builtin.TEXT_MAP_EXTRACT
        || format == Format.Builtin.HTTP_HEADERS) {
      context = Propagation.extractTextFormat(tracer.getHttpTextFormat(), (TextMapExtract) carrier);
    } else if (format == Format.Builtin.BINARY) {
      context = Propagation.extractBinaryFormat(tracer.getBinaryFormat(), (Binary) carrier);
    }

    return context;
  }

  @Override
  public void close() {
    // TODO
  }

  static io.opentelemetry.trace.SpanContext getActualContext(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException("context is not a valid SpanContextShim object");
    }

    return ((SpanContextShim) context).getSpanContext();
  }
}
