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

package openconsensus.trace;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import openconsensus.context.NoopScope;
import openconsensus.context.Scope;
import openconsensus.context.propagation.BinaryFormat;
import openconsensus.context.propagation.HttpTextFormat;
import openconsensus.internal.Utils;
import openconsensus.resource.Resource;

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
  private static final class NoopTracer extends Tracer {
    private static final BinaryFormat<SpanContext> BINARY_FORMAT = new NoopBinaryFormat();
    private static final HttpTextFormat<SpanContext> HTTP_TEXT_FORMAT = new NoopHttpTextFormat();

    @Override
    public Span getCurrentSpan() {
      return BlankSpan.INSTANCE;
    }

    @Override
    public Scope withSpan(Span span) {
      return NoopScope.getInstance();
    }

    @Override
    public Runnable withSpan(Span span, Runnable runnable) {
      return runnable;
    }

    @Override
    public <C> Callable<C> withSpan(Span span, Callable<C> callable) {
      return callable;
    }

    @Override
    public SpanBuilder spanBuilder(String spanName) {
      return spanBuilderWithExplicitParent(spanName, getCurrentSpan());
    }

    @Override
    public SpanBuilder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent) {
      return NoopSpanBuilder.createWithParent(spanName, parent);
    }

    @Override
    public SpanBuilder spanBuilderWithRemoteParent(
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

  // Noop implementation of SpanBuilder.
  private static final class NoopSpanBuilder extends SpanBuilder {
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
    public void startSpanAndRun(Runnable runnable) {
      runnable.run();
    }

    @Override
    public <V> V startSpanAndCall(Callable<V> callable) throws Exception {
      return callable.call();
    }

    @Override
    public SpanBuilder setSampler(@Nullable Sampler sampler) {
      return this;
    }

    @Override
    public SpanBuilder addLink(Link link) {
      return this;
    }

    @Override
    public SpanBuilder addLinks(List<Link> links) {
      return this;
    }

    @Override
    public SpanBuilder setRecordEvents(boolean recordEvents) {
      return this;
    }

    @Override
    public SpanBuilder setSpanKind(Span.Kind spanKind) {
      return this;
    }

    private NoopSpanBuilder(String name) {
      Utils.checkNotNull(name, "name");
    }
  }

  private static final class NoopBinaryFormat extends BinaryFormat<SpanContext> {

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

  private static final class NoopHttpTextFormat extends HttpTextFormat<SpanContext> {

    private NoopHttpTextFormat() {}

    @Override
    public List<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
      Utils.checkNotNull(spanContext, "spanContext");
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(setter, "setter");
    }

    @Override
    public <C> SpanContext extract(C carrier, Getter<C> getter) {
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(getter, "getter");
      return SpanContext.BLANK;
    }
  }
}
