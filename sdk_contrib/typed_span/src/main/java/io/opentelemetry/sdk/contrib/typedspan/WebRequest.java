/*
 * Copyright 2020, OpenTelemetry Authors
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
package io.opentelemetry.sdk.contrib.typedspan;

import io.opentelemetry.trace.Span;
import java.net.URL;
import java.net.URLConnection;
import sun.net.www.protocol.http.HttpURLConnection;

public class WebRequest extends BaseType {

  static final String UNKNOWN_URL = "<unknown>";

  /**
   * Semantic Attributes
   * https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/data-http.md
   */
  public static final String URL_KEY = "http.url";

  public static final String HOST_KEY = "http.host";
  public static final String SCHEME_KEY = "http.scheme";
  public static final String METHOD_KEY = "http.method";

  enum Method {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    CONNECT,
    OPTIONS,
    PATCH
  }

  private final String url;
  private final String host;
  private final String schema;
  private final Method method;

  WebRequest(String spanName, String url, String host, String schema, WebRequest.Method method) {
    super(spanName);
    this.url = url;
    this.host = host;
    this.schema = schema;
    this.method = method;
  }

  private static String extractSpanName(URL url) {
    if (url == null) {
      return WebRequest.UNKNOWN_URL;
    }

    return url.toString().split("\\?", 2)[0];
  }

  public static Span.Builder create(URLConnection connection) {
    String spanName = extractSpanName(connection.getURL());
    String url = connection.getURL().toString();
    String host = connection.getHeaderField("Host");
    String schema = connection.getURL().getProtocol();
    Method method = Method.GET;
    WebRequest wb = new WebRequest(spanName, url, host, schema, method);
    return wb.build();
  }

  public static Span.Builder create(HttpURLConnection connection) {
    String spanName = extractSpanName(connection.getURL());
    String url = connection.getURL().toString();
    String host = connection.getHeaderField("Host");
    String schema = connection.getURL().getProtocol();
    Method method;
    switch (connection.getRequestMethod()) {
      case "GET":
        method = Method.GET;
        break;
      case "POST":
        method = Method.POST;
        break;
      case "HEAD":
        method = Method.HEAD;
        break;
      case "PUT":
        method = Method.PUT;
        break;
      case "DELETE":
        method = Method.DELETE;
        break;
      case "CONNECT":
        method = Method.CONNECT;
        break;
      case "OPTIONS":
        method = Method.OPTIONS;
        break;
      case "PATCH":
        method = Method.PATCH;
        break;
      default:
        method = Method.GET;
    }
    WebRequest wb = new WebRequest(spanName, url, host, schema, method);
    return wb.build();
  }

  @Override
  Span.Builder build() {
    Span.Builder builder = super.build();
    builder.setAttribute(URL_KEY, url);
    builder.setAttribute(HOST_KEY, host);
    builder.setAttribute(SCHEME_KEY, schema);
    builder.setAttribute(METHOD_KEY, method.toString());
    builder.setSpanKind(Span.Kind.CLIENT);
    return builder;
  }
}
