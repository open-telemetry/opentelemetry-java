/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An {@link HttpHandler} that will be used to render HTML pages using any {@code ZPageHandler}. */
final class ZPageHttpHandler implements HttpHandler {
  // Splitter for splitting URL query parameters
  private static final Splitter QUERY_SPLITTER = Splitter.on("&").trimResults().omitEmptyStrings();
  // Splitter for splitting URL query parameters' key value
  private static final Splitter QUERY_KEYVAL_SPLITTER =
      Splitter.on("=").trimResults().omitEmptyStrings();
  // Query string parameter name for span name
  private static final String PARAM_SPAN_NAME = "zspanname";
  // The corresponding ZPageHandler for the zPage (e.g. TracezZPageHandler)
  private final ZPageHandler zpageHandler;

  /** Constructs a new {@code ZPageHttpHandler}. */
  ZPageHttpHandler(ZPageHandler zpageHandler) {
    this.zpageHandler = zpageHandler;
  }

  /**
   * Build a query map from the query string.
   *
   * @param queryString the query string for buiding the query map.
   * @return the query map built based on the query string.
   */
  @VisibleForTesting
  static ImmutableMap<String, String> parseQueryString(String queryString)
      throws UnsupportedEncodingException {
    if (queryString == null) {
      return ImmutableMap.of();
    }
    Map<String, String> queryMap = new HashMap<String, String>();
    for (String param : QUERY_SPLITTER.split(queryString)) {
      List<String> keyValuePair = QUERY_KEYVAL_SPLITTER.splitToList(param);
      if (keyValuePair.size() > 1) {
        if (keyValuePair.get(0).equals(PARAM_SPAN_NAME)) {
          queryMap.put(keyValuePair.get(0), URLDecoder.decode(keyValuePair.get(1), "UTF-8"));
        } else {
          queryMap.put(keyValuePair.get(0), keyValuePair.get(1));
        }
      }
    }
    return ImmutableMap.copyOf(queryMap);
  }

  @Override
  public final void handle(HttpExchange httpExchange) throws IOException {
    try {
      String requestMethod = httpExchange.getRequestMethod();
      httpExchange.sendResponseHeaders(200, 0);
      if (requestMethod.equalsIgnoreCase("GET")) {
        zpageHandler.emitHtml(
            parseQueryString(httpExchange.getRequestURI().getRawQuery()),
            httpExchange.getResponseBody());
      } else {
        final String queryString;
        try (InputStreamReader requestBodyReader =
            new InputStreamReader(httpExchange.getRequestBody(), "utf-8")) {
          queryString = CharStreams.toString(requestBodyReader);
        }
        boolean error =
            zpageHandler.processRequest(
                requestMethod, parseQueryString(queryString), httpExchange.getResponseBody());
        if (!error) {
          zpageHandler.emitHtml(parseQueryString(queryString), httpExchange.getResponseBody());
        }
      }
    } finally {
      httpExchange.close();
    }
  }
}
