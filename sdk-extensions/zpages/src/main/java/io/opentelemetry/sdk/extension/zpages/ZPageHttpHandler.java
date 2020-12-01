/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** An {@link HttpHandler} that will be used to render HTML pages using any {@code ZPageHandler}. */
final class ZPageHttpHandler implements HttpHandler {
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
  // Visible for testing
  static Map<String, String> parseQueryString(String queryString) {
    if (queryString == null) {
      return Collections.emptyMap();
    }
    Map<String, String> queryMap = new HashMap<String, String>();
    Arrays.stream(queryString.split("&"))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .forEach(
            param -> {
              List<String> keyValuePair =
                  Arrays.stream(param.split("="))
                      .map(String::trim)
                      .filter(s -> !s.isEmpty())
                      .collect(Collectors.toList());
              if (keyValuePair.size() > 1) {
                if (keyValuePair.get(0).equals(PARAM_SPAN_NAME)) {
                  try {
                    queryMap.put(
                        keyValuePair.get(0), URLDecoder.decode(keyValuePair.get(1), "UTF-8"));
                  } catch (UnsupportedEncodingException e) {
                    // Ignore encoding exception.
                  }
                } else {
                  queryMap.put(keyValuePair.get(0), keyValuePair.get(1));
                }
              }
            });
    return Collections.unmodifiableMap(queryMap);
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
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"))) {
          // Query strings can only have one line
          queryString = reader.readLine();
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
