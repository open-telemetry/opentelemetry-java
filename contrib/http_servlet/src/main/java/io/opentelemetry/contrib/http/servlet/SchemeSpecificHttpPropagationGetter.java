/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.contrib.http.servlet;

import io.opentelemetry.context.propagation.HttpTextFormat;
import javax.servlet.http.HttpServletRequest;

/**
 * Defines the strategy interface used by {@link MultiSchemeHttpPropagationGetter} to retrieve
 * values sent by a single external distributed tracing team.
 */
public interface SchemeSpecificHttpPropagationGetter
    extends HttpTextFormat.Getter<HttpServletRequest> {

  /**
   * Returns the name of the scheme this getter supports.
   *
   * @return the scheme
   */
  String getName();

  /**
   * Returns whether the supplied HTTP request contains the tracing values handled by this getter.
   *
   * @param request the HTTP request from the client
   * @return can handle or not
   */
  boolean canProvideValues(HttpServletRequest request);
}
