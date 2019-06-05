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

package io.opentelemetry.dctx;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;

/**
 * Object for creating new {@link DistributedContext}s and {@code DistributedContext}s based on the
 * current context.
 *
 * <p>This class returns {@link DistributedContext.Builder builders} that can be used to create the
 * implementation-dependent {@link DistributedContext}s.
 *
 * <p>Implementations may have different constraints and are free to convert attribute contexts to
 * their own subtypes. This means callers cannot assume the {@link #getCurrentContext() current
 * context} is the same instance as the one {@link #withDistributedContext(DistributedContext)
 * placed into scope}.
 *
 * @since 0.1.0
 */
public interface DistributedContextManager {

  /**
   * Returns the current {@code DistributedContext}.
   *
   * @return the current {@code DistributedContext}.
   * @since 0.1.0
   */
  DistributedContext getCurrentContext();

  /**
   * Returns a new {@code Builder}.
   *
   * @return a new {@code Builder}.
   * @since 0.1.0
   */
  DistributedContext.Builder contextBuilder();

  /**
   * Enters the scope of code where the given {@code DistributedContext} is in the current context
   * (replacing the previous {@code DistributedContext}) and returns an object that represents that
   * scope. The scope is exited when the returned object is closed.
   *
   * @param distContext the {@code DistributedContext} to be set as the current context.
   * @return an object that defines a scope where the given {@code DistributedContext} is set as the
   *     current context.
   * @since 0.1.0
   */
  Scope withContext(DistributedContext distContext);

  /**
   * Returns the {@link BinaryFormat} for this implementation.
   *
   * <p>Example of usage on the client:
   *
   * <pre>{@code
   * private static final DistributedContextManager contextManager =
   *     OpenTelemetry.getDistributedContextManager();
   * private static final BinaryFormat binaryFormat = contextManager.getBinaryFormat();
   *
   * Request createRequest() {
   *   Request req = new Request();
   *   byte[] ctxBuffer = binaryFormat.toByteArray(contextManager.getCurrentContext());
   *   request.addMetadata("distributedContext", ctxBuffer);
   *   return request;
   * }
   * }</pre>
   *
   * <p>Example of usage on the server:
   *
   * <pre>{@code
   * private static final DistributedContextManager contextManager =
   *     OpenTelemetry.getDistributedContextManager();
   * private static final BinaryFormat binaryFormat = contextManager.getBinaryFormat();
   *
   * void onRequestReceived(Request request) {
   *   byte[] ctxBuffer = request.getMetadata("distributedContext");
   *   DistributedContext distContext = textFormat.fromByteArray(ctxBuffer);
   *   try (Scope s = contextManager.withContext(distContext)) {
   *     // Handle request and send response back.
   *   }
   * }
   * }</pre>
   *
   * @return the {@code BinaryFormat} for this implementation.
   * @since 0.1.0
   */
  BinaryFormat<DistributedContext> getBinaryFormat();

  /**
   * Returns the {@link HttpTextFormat} for this implementation.
   *
   * <p>Usually this will be the W3C Correlation Context as the HTTP text format. For more details,
   * see <a href="https://github.com/w3c/correlation-context">correlation-context</a>.
   *
   * <p>Example of usage on the client:
   *
   * <pre>{@code
   * private static final DistributedContextManager contextManager =
   *     OpenTelemetry.getDistributedContextManager();
   * private static final HttpTextFormat textFormat = contextManager.getHttpTextFormat();
   *
   * private static final HttpTextFormat.Setter setter =
   *     new HttpTextFormat.Setter<HttpURLConnection>() {
   *       public void put(HttpURLConnection carrier, String key, String value) {
   *         carrier.setRequestProperty(field, value);
   *       }
   *     };
   *
   * void makeHttpRequest() {
   *   HttpURLConnection connection =
   *       (HttpURLConnection) new URL("http://myserver").openConnection();
   *   textFormat.inject(contextManager.getCurrentContext(), connection, httpURLConnectionSetter);
   *   // Send the request, wait for response and maybe set the status if not ok.
   * }
   * }</pre>
   *
   * <p>Example of usage on the server:
   *
   * <pre>{@code
   * private static final DistributedContextManager contextManager =
   *     OpenTelemetry.getDistributedContextManager();
   * private static final HttpTextFormat textFormat = contextManager.getHttpTextFormat();
   * private static final HttpTextFormat.Getter<HttpRequest> getter = ...;
   *
   * void onRequestReceived(HttpRequest request) {
   *   DistributedContext distContext = textFormat.extract(request, getter);
   *   try (Scope s = contextManager.withContext(distContext)) {
   *     // Handle request and send response back.
   *   }
   * }
   * }</pre>
   *
   * @return the {@code HttpTextFormat} for this implementation.
   * @since 0.1.0
   */
  HttpTextFormat<DistributedContext> getHttpTextFormat();
}
