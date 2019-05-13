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

package io.opentelemetry.trace;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.context.propagation.TraceContextFormat;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.trace.unsafe.ContextUtils;
import java.util.List;
import javax.annotation.Nullable;

/**
 * No-op implementations of trace classes.
 *
 * @since 0.1.0
 */
public final class NoopTrace {

  private NoopTrace() {}

  /**
   * Returns a {@code Tracer} instance that is no-op implementations.
   *
   * @return a {@code Tracer} instance that is no-op implementations.
   * @since 0.1.0
   */
  public static Tracer newNoopTracer() {
    return new NoopTracer();
  }

  // No-Op implementation of the Tracer.
  private static final class NoopTracer implements Tracer {
    private static final BinaryFormat<SpanContext> BINARY_FORMAT = new NoopBinaryFormat();
    private static final HttpTextFormat<SpanContext> HTTP_TEXT_FORMAT = new TraceContextFormat();

    @Override
    public Span getCurrentSpan() {
      return ContextUtils.getValue();
    }

    @Override
    public Scope withSpan(Span span) {
      return SpanInScope.create(span);
    }

    @Override
    public Span.Builder spanBuilder(String spanName) {
      return spanBuilderWithExplicitParent(spanName, getCurrentSpan());
    }

    @Override
    public Span.Builder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent) {
      return NoopSpanBuilder.createWithParent(spanName, parent);
    }

    @Override
    public Span.Builder spanBuilderWithRemoteParent(
        String spanName, @Nullable SpanContext remoteParentSpanContext) {
      return NoopSpanBuilder.createWithRemoteParent(spanName, remoteParentSpanContext);
    }

    @Override
    public void recordSpanData(SpanData spanData) {
      Utils.checkNotNull(spanData, "spanData");
    }

    @Override
    public void setResource(Resource resource) {
      // do nothing
    }

    @Override
    public Resource getResource() {
      return Resource.getEmpty();
    }

    @Override
    public BinaryFormat<SpanContext> getBinaryFormat() {
      return BINARY_FORMAT;
    }

    @Override
    public HttpTextFormat<SpanContext> getHttpTextFormat() {
      return HTTP_TEXT_FORMAT;
    }

    private NoopTracer() {}
  }

  // Noop implementation of Span.Builder.
  private static final class NoopSpanBuilder implements Span.Builder {
    static NoopSpanBuilder createWithParent(String spanName, @Nullable Span parent) {
      return new NoopSpanBuilder(spanName);
    }

    static NoopSpanBuilder createWithRemoteParent(
        String spanName, @Nullable SpanContext remoteParentSpanContext) {
      return new NoopSpanBuilder(spanName);
    }

    @Override
    public Span startSpan() {
      return BlankSpan.INSTANCE;
    }

    @Override
    public NoopSpanBuilder setSampler(@Nullable Sampler sampler) {
      return this;
    }

    @Override
    public NoopSpanBuilder addLink(Link link) {
      return this;
    }

    @Override
    public NoopSpanBuilder addLinks(List<Link> links) {
      return this;
    }

    @Override
    public NoopSpanBuilder setRecordEvents(boolean recordEvents) {
      return this;
    }

    @Override
    public NoopSpanBuilder setSpanKind(Span.Kind spanKind) {
      return this;
    }

    private NoopSpanBuilder(String name) {
      Utils.checkNotNull(name, "name");
    }
  }

  private static final class NoopBinaryFormat implements BinaryFormat<SpanContext> {

    @Override
    public byte[] toByteArray(SpanContext spanContext) {
      Utils.checkNotNull(spanContext, "spanContext");
      return new byte[0];
    }

    @Override
    public SpanContext fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return SpanContext.BLANK;
    }

    private NoopBinaryFormat() {}
  }

  private static final class SpanInScope implements Scope {
    private final Context previous;
    private final Context current;

    private SpanInScope(Span span) {
      current = ContextUtils.withValue(span);
      previous = current.attach();
    }

    public static SpanInScope create(Span span) {
      return new SpanInScope(span);
    }

    @Override
    public void close() {
      current.detach(previous);
    }
  }
}
