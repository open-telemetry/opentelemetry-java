package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.CloseableSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

public final class SdkCloseableSpan implements CloseableSpan {
  private final Span span;
  private final Scope scope;

  private SdkCloseableSpan(Span span, Scope scope) {
    this.span = span;
    this.scope = scope;
  }

  public static CloseableSpan create(Span span) {
    Scope scope = span.makeCurrent();
    return new SdkCloseableSpan(span, scope);
  }

  @Override
  public Span getSpan() {
    return span;
  }

  @Override
  public Scope getScope() {
    return scope;
  }
}
