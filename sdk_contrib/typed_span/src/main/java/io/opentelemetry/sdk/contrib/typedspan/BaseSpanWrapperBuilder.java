package io.opentelemetry.sdk.contrib.typedspan;

import io.opentelemetry.trace.Span;

public abstract class BaseSpanWrapperBuilder<B extends BaseSpanWrapper> {
  Span.Builder builder;

  public BaseSpanWrapperBuilder(Span.Builder builder) {
    this.builder = builder;
  }

  public Span.Builder getRawSpanBuilder() {
    return builder;
  }

  public abstract B startSpan();
}
