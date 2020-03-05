package io.opentelemetry.sdk.contrib.typedspan.http;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.net.HttpURLConnection;
import java.net.URLConnection;

public class HttpClientSpanWrapperBuilder extends HttpSpanWrapperBuilder {

  private HttpClientSpanWrapperBuilder(
      Tracer tracer, Method method, String url, String host, String scheme) {
    this(tracer, method, url, host, scheme, HttpClientSpanWrapperBuilder.extractSpanName(url));
  }

  private HttpClientSpanWrapperBuilder(
      Tracer tracer, Method method, String url, String host, String scheme, String spanName) {
    super(tracer, spanName, method);
    setrequiredFields(url, host, scheme);
  }

  private final void setrequiredFields(String url, String host, String scheme) {
    this.getRawSpanBuilder().setSpanKind(Span.Kind.CLIENT);
    this.setUrl(url);
    this.setHost(host);
    this.setScheme(scheme);
  }

  public static HttpClientSpanWrapperBuilder create(Tracer tracer, URLConnection connection) {
    String url = connection.getURL().toString();
    String host = connection.getHeaderField("Host");
    String schema = connection.getURL().getProtocol();
    return new HttpClientSpanWrapperBuilder(
        tracer, connection.getDoOutput() ? Method.GET : Method.POST, url, host, schema);
  }

  public static HttpClientSpanWrapperBuilder create(Tracer tracer, HttpURLConnection connection) {
    String url = connection.getURL().toString();
    String host = connection.getHeaderField("Host");
    String schema = connection.getURL().getProtocol();
    Method method = Method.getMethod(connection.getRequestMethod());
    return new HttpClientSpanWrapperBuilder(tracer, method, url, host, schema);
  }

  @Override
  public HttpClientSpanWrapper startSpan() {
    return new HttpClientSpanWrapper(this.getRawSpanBuilder().startSpan());
  }
}
