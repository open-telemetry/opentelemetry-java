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
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;

@SuppressWarnings("deprecation")
final class SpanBuilderShim implements SpanBuilder {
  openconsensus.trace.Tracer tracer;
  openconsensus.trace.SpanBuilder builder;

  public SpanBuilderShim(
      openconsensus.trace.Tracer tracer, openconsensus.trace.SpanBuilder builder) {
    this.tracer = tracer;
    this.builder = builder;
  }

  @Override
  public SpanBuilder asChildOf(SpanContext parent) {
    // TODO - Verify we handle a no-op SpanContext
    return this;
  }

  @Override
  public SpanBuilder asChildOf(Span parent) {
    // TODO - Verify we handle a no-op Span
    return this;
  }

  @Override
  public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder ignoreActiveSpan() {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, String value) {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, boolean value) {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, Number value) {
    // TODO
    return this;
  }

  @Override
  public <T> SpanBuilder withTag(Tag<T> tag, T value) {
    // TODO
    return this;
  }

  @Override
  public SpanBuilder withStartTimestamp(long microseconds) {
    // TODO
    return this;
  }

  @Override
  public Span startManual() {
    return start();
  }

  @Override
  public Span start() {
    return new SpanShim(builder.startSpan());
  }

  @SuppressWarnings("MustBeClosedChecker")
  @Override
  public Scope startActive(boolean finishSpanOnClose) {
    openconsensus.trace.Span span = builder.startSpan();
    return new ScopeShim(tracer.withSpan(span), span, finishSpanOnClose);
  }
}
