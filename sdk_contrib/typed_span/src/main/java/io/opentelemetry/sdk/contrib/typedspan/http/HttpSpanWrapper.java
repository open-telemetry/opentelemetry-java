package io.opentelemetry.sdk.contrib.typedspan.http;

import io.opentelemetry.sdk.contrib.typedspan.BaseSpanWrapper;
import io.opentelemetry.trace.Span;

class HttpSpanWrapper extends BaseSpanWrapper {

  HttpSpanWrapper(Span span) {
    super(span);
  }
}
