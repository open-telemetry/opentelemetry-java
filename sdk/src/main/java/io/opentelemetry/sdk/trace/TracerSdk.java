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

package io.opentelemetry.sdk.trace;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.unsafe.ContextUtils;

/** {@link TracerSdk} is SDK implementation of {@link Tracer}. */
public class TracerSdk implements Tracer {

  @Override
  public Span getCurrentSpan() {
    return ContextUtils.getValue();
  }

  @Override
  public Scope withSpan(Span span) {
    return new ScopeInSpan(span);
  }

  @Override
  public Span.Builder spanBuilder(String spanName) {
    return null;
  }

  @Override
  public void setResource(Resource resource) {}

  @Override
  public Resource getResource() {
    return null;
  }

  @Override
  public void recordSpanData(SpanData span) {}

  @Override
  public BinaryFormat<SpanContext> getBinaryFormat() {
    return null;
  }

  @Override
  public HttpTextFormat<SpanContext> getHttpTextFormat() {
    return null;
  }

  /**
   * Attempts to stop all the activity for this {@link Tracer}. Calls {@link
   * SpanProcessor#shutdown()} for all registered {@link SpanProcessor}s.
   *
   * <p>This operation may block until all the Spans are processed. Must be called before turning
   * off the main application to ensure all data are processed and exported.
   *
   * <p>After this is called all the newly created {@code Span}s will be no-op.
   */
  public void shutdown() {}

  // Defines an arbitrary scope of code as a traceable operation. Supports try-with-resources idiom.
  private static final class ScopeInSpan implements Scope {
    private final Context origContext;

    /**
     * Constructs a new {@link ScopeInSpan}.
     *
     * @param span is the {@code Span} to be added to the current {@code io.grpc.Context}.
     */
    private ScopeInSpan(Span span) {
      origContext = ContextUtils.withValue(span).attach();
    }

    @Override
    public void close() {
      Context.current().detach(origContext);
    }
  }
}
