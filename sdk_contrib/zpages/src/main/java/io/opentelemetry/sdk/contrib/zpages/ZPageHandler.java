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

package io.opentelemetry.sdk.contrib.zpages;

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
   * Emits the generated HTML page to the {@code outputStream}.
   *
   * @param queryMap the map of the URL query parameters.
   * @param outputStream the output for the generated HTML page.
   */
  public abstract void emitHtml(Map<String, String> queryMap, OutputStream outputStream);

  /** Package protected constructor to disallow users to extend this class. */
  ZPageHandler() {}
}
