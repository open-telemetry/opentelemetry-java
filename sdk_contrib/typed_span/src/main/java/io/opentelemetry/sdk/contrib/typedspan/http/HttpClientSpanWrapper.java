package io.opentelemetry.sdk.contrib.typedspan.http;

import static io.opentelemetry.sdk.contrib.typedspan.http.HttpSpanWrapperBuilder.STATUS_CODE_KEY;
import static io.opentelemetry.sdk.contrib.typedspan.http.HttpSpanWrapperBuilder.STATUS_TEXT_KEY;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;

public class HttpClientSpanWrapper extends HttpSpanWrapper {
  public HttpClientSpanWrapper(Span span) {
    super(span);
  }

  public HttpClientSpanWrapper setStatusCode(HttpStatusCode status) {
    this.getRawSpan().setAttribute(STATUS_CODE_KEY, status.getCode());
    this.getRawSpan().setAttribute(STATUS_TEXT_KEY, status.getMsg());
    return this;
  }

  public HttpClientSpanWrapper setStatusCode(int code) {
    HttpStatusCode status = new HttpStatusCode(code);
    this.getRawSpan().setAttribute(STATUS_CODE_KEY, status.getCode());
    this.getRawSpan().setAttribute(STATUS_TEXT_KEY, status.getMsg());
    return this;
  }

  public void end(Status status) {
    this.getRawSpan().setStatus(status);
    this.getRawSpan().end();
  }
}
