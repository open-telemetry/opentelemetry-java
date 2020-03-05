package io.opentelemetry.sdk.contrib.typedspan.http;

import io.opentelemetry.sdk.contrib.typedspan.BaseSpanWrapper;
import io.opentelemetry.sdk.contrib.typedspan.BaseSpanWrapperBuilder;
import io.opentelemetry.trace.Tracer;

public abstract class HttpSpanWrapperBuilder extends BaseSpanWrapperBuilder<BaseSpanWrapper> {

  static final String UNKNOWN_URL = "<unknown>";

  /**
   * Semantic Attributes
   * https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/data-http.md
   */
  public static final String METHOD_KEY = "http.method";

  public static final String URL_KEY = "http.url";
  public static final String TARGET_KEY = "http.target";
  public static final String HOST_KEY = "http.host";
  public static final String SCHEME_KEY = "http.scheme";
  public static final String STATUS_CODE_KEY = "http.status_code";
  public static final String STATUS_TEXT_KEY = "http.status_text";
  public static final String FLAVOR_KEY = "http.flavor";
  public static final String USER_AGENT_KEY = "http.user_agent";

  public enum Method {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    CONNECT,
    OPTIONS,
    PATCH;

    public static Method getMethod(String method) {
      switch (method.toUpperCase()) {
        case "GET":
          return GET;
        case "HEAD":
          return HEAD;
        case "POST":
          return POST;
        case "PUT":
          return PUT;
        case "DELETE":
          return DELETE;
        case "CONNECT":
          return CONNECT;
        case "OPTIONS":
          return OPTIONS;
        default:
          return PATCH;
      }
    }
  }

  public enum Flavor {
    HTTP_1,
    HTTP_1_1,
    HTTP_2,
    SPDY,
    QUIC
  }

  public HttpSpanWrapperBuilder(Tracer tracer, String spanName, Method method) {
    super(tracer.spanBuilder(spanName));
    setRequiredFields(method);
  }

  private final void setRequiredFields(Method method) {
    this.getRawSpanBuilder().setAttribute(METHOD_KEY, method.toString());
  }

  public HttpSpanWrapperBuilder setUrl(String url) {
    this.getRawSpanBuilder().setAttribute(URL_KEY, url);
    return this;
  }

  public HttpSpanWrapperBuilder setHost(String host) {
    this.getRawSpanBuilder().setAttribute(HOST_KEY, host);
    return this;
  }

  public HttpSpanWrapperBuilder setScheme(String schema) {
    this.getRawSpanBuilder().setAttribute(SCHEME_KEY, schema);
    return this;
  }

  public HttpSpanWrapperBuilder setTarget(String target) {
    this.getRawSpanBuilder().setAttribute(TARGET_KEY, target);
    return this;
  }

  public HttpSpanWrapperBuilder setFlavor(Flavor flavor) {
    this.getRawSpanBuilder().setAttribute(FLAVOR_KEY, flavor.toString());
    return this;
  }

  public HttpSpanWrapperBuilder setUserAgent(String userAgent) {
    this.getRawSpanBuilder().setAttribute(USER_AGENT_KEY, userAgent);
    return this;
  }

  protected static String extractSpanName(String url) {
    if (url == null) {
      return HttpSpanWrapperBuilder.UNKNOWN_URL;
    }
    return url.split("\\?", 2)[0];
  }
}
