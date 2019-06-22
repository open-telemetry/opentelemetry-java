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

package io.opentelemetry.inmemorytrace;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.context.propagation.TraceContextFormat;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.unsafe.ContextUtils;
import java.util.ArrayList;
import java.util.List;

// TODO - Use the Nullable annotation everywhere.
public final class InMemoryTracer implements Tracer {
  private final List<SpanData> finishedSpanDataItems = new ArrayList<>();
  private final HttpTextFormat<SpanContext> textFormat = new TraceContextFormat();
  private final Resource resource;

  public InMemoryTracer() {
    this(Resource.getEmpty());
  }

  public InMemoryTracer(Resource resource) {
    Check.isNotNull(resource, "resource");
    this.resource = resource;
  }

  static final class InMemoryScope implements Scope {
    final Context context;
    final Context prevContext;

    public InMemoryScope(Context context) {
      this.context = context;
      this.prevContext = context.attach();
    }

    @Override
    public void close() {
      context.detach(prevContext);
    }
  }

  Resource getResource() {
    return resource;
  }

  @Override
  public Span getCurrentSpan() {
    return ContextUtils.getValue(Context.current());
  }

  @Override
  public Scope withSpan(Span span) {
    Check.isNotNull(span, "span");

    Context context = ContextUtils.withValue(span, Context.current());
    return new InMemoryScope(context);
  }

  @Override
  public Span.Builder spanBuilder(String spanName) {
    Check.isNotNull(spanName, "spanName");

    return new InMemorySpan.Builder(this, spanName);
  }

  @Override
  public void recordSpanData(SpanData spanData) {
    Check.isNotNull(spanData, "spanData");

    synchronized (this) {
      finishedSpanDataItems.add(spanData);
    }
  }

  @Override
  @SuppressWarnings("ReturnMissingNullable")
  public BinaryFormat<SpanContext> getBinaryFormat() {
    // TODO
    return null;
  }

  @Override
  public HttpTextFormat<SpanContext> getHttpTextFormat() {
    return textFormat;
  }

  /** Returns a {@code List} of the finished {@code Span}s, represented by {@code SpanData}. */
  public List<SpanData> getFinishedSpanDataItems() {
    synchronized (this) {
      return new ArrayList<>(finishedSpanDataItems);
    }
  }

  /** Clears the internal {@code List} of finished {@code Span}s. */
  public void reset() {
    synchronized (this) {
      finishedSpanDataItems.clear();
    }
  }
}
