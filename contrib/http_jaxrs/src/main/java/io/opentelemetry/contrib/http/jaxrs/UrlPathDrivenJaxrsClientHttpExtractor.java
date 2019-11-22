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

package io.opentelemetry.contrib.http.jaxrs;

import io.opentelemetry.contrib.http.core.HttpExtractor;
import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

/**
 * Extracts span attributes from {@link ClientRequestContext} and {@link ClientResponseContext} with
 * routes being the URI path.
 */
public class UrlPathDrivenJaxrsClientHttpExtractor
    extends HttpExtractor<ClientRequestContext, ClientResponseContext> {

  @Override
  public String getMethod(ClientRequestContext request) {
    return request.getMethod();
  }

  @Override
  public String getUrl(ClientRequestContext request) {
    return request.getUri().toString();
  }

  @Override
  public String getRoute(ClientRequestContext request) {
    return request.getUri().getPath();
  }

  @Override
  public int getStatusCode(@Nullable ClientResponseContext response) {
    return response != null ? response.getStatus() : 0;
  }
}
