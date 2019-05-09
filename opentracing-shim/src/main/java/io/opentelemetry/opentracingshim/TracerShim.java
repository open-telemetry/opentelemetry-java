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
import io.opentracing.propagation.TextMap;

final class TracerShim implements Tracer {
  private final io.opentelemetry.trace.Tracer tracer;
  private final io.opentelemetry.tags.Tagger tagger;
  private final ScopeManager scopeManagerShim;
  private final Propagation propagation;

  TracerShim(io.opentelemetry.trace.Tracer tracer) {
    this.tracer = tracer;
    this.tagger = io.opentelemetry.tags.Tags.getTagger(); // TODO - Let the user specify it.
    this.scopeManagerShim = new ScopeManagerShim(this);
    this.propagation = new Propagation(this);
  }

  io.opentelemetry.tags.Tagger getTagger() {
    return tagger;
  }

  io.opentelemetry.trace.Tracer getTracer() {
    return tracer;
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
    return new SpanBuilderShim(this, operationName);
  }

  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {
    SpanContextShim contextShim = getContextShim(context);

    // TODO - Shall we expect to get no-op objects if a given format is not supported at all?
    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      propagation.injectTextFormat(contextShim, (TextMap) carrier);
    } else if (format == Format.Builtin.BINARY) {
      propagation.injectBinaryFormat(contextShim, (Binary) carrier);
    }
  }

  @SuppressWarnings("ReturnMissingNullable")
  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {

    SpanContextShim context = null;

    if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS) {
      context = propagation.extractTextFormat((TextMap) carrier);
    } else if (format == Format.Builtin.BINARY) {
      context = propagation.extractBinaryFormat((Binary) carrier);
    }

    return context;
  }

  @Override
  public void close() {
    // TODO
  }

  static SpanContextShim getContextShim(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException("context is not a valid SpanContextShim object");
    }

    return (SpanContextShim) context;
  }
}
