/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import java.io.OutputStream;
import java.util.Map;

/**
 * The main interface for all zPages. All zPages should implement this interface to allow the HTTP
 * server implementation to support these pages.
 */
public abstract class ZPageHandler {

  /**
   * Returns the URL path that should be used to register this zPage to the HTTP server.
   *
   * @return the URL path that should be used to register this zPage to the HTTP server.
   */
  public abstract String getUrlPath();

  /**
   * Returns the name of the zPage.
   *
   * @return the name of the zPage.
   */
  public abstract String getPageName();

  /**
   * Returns the description of the zPage.
   *
   * @return the description of the zPage.
   */
  public abstract String getPageDescription();

  /**
   * Process requests that require changes (POST/PUT/DELETE).
   *
   * @param requestMethod the request method HttpHandler received.
   * @param queryMap the map of the URL query parameters.
   * @return true if theres an error while processing the request.
   */
  public boolean processRequest(
      String requestMethod, Map<String, String> queryMap, OutputStream outputStream) {
    // base no-op method
    return false;
  }

  /**
   * Emits the generated HTML page to the {@code outputStream}.
   *
   * @param queryMap the map of the URL query parameters.
   * @param outputStream the output for the generated HTML page.
   */
  public abstract void emitHtml(Map<String, String> queryMap, OutputStream outputStream);

  /** Package protected constructor to disallow users to extend this class. */
  ZPageHandler() {}
}
