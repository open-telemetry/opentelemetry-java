package io.opentelemetry.sdk.contrib.typedspan;

import io.opentelemetry.trace.Span;

public abstract class BaseSpanWrapper {
  Span span;

  public BaseSpanWrapper(Span span) {
    this.span = span;
  }

  public Span getRawSpan() {
    return span;
  }
}
