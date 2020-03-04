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
package io.opentelemetry.sdk.contrib.typedspan.http;

import java.net.HttpURLConnection;

public final class HttpURLConnectionWebRequest extends WebRequest {

  public HttpURLConnectionWebRequest(HttpURLConnection connection, String spanName, String url, String host, String schema) {
    super(spanName, url, host, schema, computeMethod(connection));
  }

  private static Method computeMethod(HttpURLConnection connection) {
    WebRequest.Method method;
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
      case "PATCH":
        method = Method.PATCH;
        break;
      default:
        method = Method.OPTIONS;
    }
    return method;
  }


}
