/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;

final class NoopSpanBuilderShim implements SpanBuilder {

  private static final Tracer TRACER =
      TracerProvider.noop().get("io.opentelemetry.opentracingshim");

  private final String spanName;

  NoopSpanBuilderShim(String spanName) {
    this.spanName = spanName == null ? "" : spanName; // OT is more permissive than OTel.
  }

  @Override
  public SpanBuilder asChildOf(Span parent) {
    return this;
  }

  @Override
  public SpanBuilder asChildOf(SpanContext parent) {
    return this;
  }

  @Override
  public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
    return this;
  }

  @Override
  public SpanBuilder ignoreActiveSpan() {
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, String value) {
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, boolean value) {
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, Number number) {
    return this;
  }

  @Override
  public <T> SpanBuilder withTag(Tag<T> tag, T value) {
    return this;
  }

  @Override
  public SpanBuilder withStartTimestamp(long microseconds) {
    return this;
  }

  @Override
  public Span start() {
    return new SpanShim(TRACER.spanBuilder(spanName).startSpan());
  }
}
