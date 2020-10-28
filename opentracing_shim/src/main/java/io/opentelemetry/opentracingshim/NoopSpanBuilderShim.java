/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.trace.Tracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;

final class NoopSpanBuilderShim extends BaseShimObject implements SpanBuilder {
  private final String spanName;

  public NoopSpanBuilderShim(TelemetryInfo telemetryInfo, String spanName) {
    super(telemetryInfo);
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
    return new SpanShim(telemetryInfo, Tracer.getDefault().spanBuilder(spanName).startSpan());
  }
}
