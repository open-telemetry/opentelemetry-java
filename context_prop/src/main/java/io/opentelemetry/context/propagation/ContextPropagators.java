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

package io.opentelemetry.context.propagation;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A container of the registered propagators for every supported format.
 *
 * <p>This container can be used to access a single, composite propagator for each supported format,
 * which will be responsible for injecting and extracting data for each registered concern (traces,
 * correlations, etc). Propagation will happen through {@code io.grpc.Context}, from which values
 * will be read upon injection, and which will store values from the extraction step. The resulting
 * {@code Context} can then be used implicitly or explicitly by the OpenTelemetry API.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tracer tracer = OpenTelemetry.getTracer();
 * void onSendRequest() {
 *   try (Scope scope = tracer.withSpan(span)) {
 *     ContextPropagators propagators = OpenTelemetry.getPropagators();
 *     HttpTextFormat textFormat = propagators.getHttpTextFormat();
 *
 *     // Inject the span's SpanContext and other available concerns (such as correlations)
 *     // contained in the specified Context.
 *     Map<String, String> map = new HashMap<>();
 *     httpTextFormat.inject(Context.current(), map, new Setter<String, String>() {
 *       public void put(Map<String, String> map, String key, String value) {
 *         map.put(key, value);
 *       }
 *     });
 *     // Send the request including the text map and wait for the response.
 *   }
 * }
 * }</pre>
 *
 * <p>Example of usage in the server:
 *
 * <pre>{@code
 * private static final Tracer tracer = OpenTelemetry.getTracer();
 * void onRequestReceived() {
 *   ContextPropagators propagators = OpenTelemetry.getPropagators();
 *   HttpTextFormat textFormat = propagators.getHttpTextFormat();
 *
 *   // Extract and store the propagated span's SpanContext and other available concerns
 *   // in the specified Context.
 *   Context context = textFormat.extract(Context.current(), request, new Getter<String, String>() {
 *     public String get(Object request, String key) {
 *       // Return the value associated to the key, if available.
 *     }
 *   });
 *   Span span = tracer.spanBuilder("MyRequest")
 *       .setParent(context)
 *       .setSpanKind(Span.Kind.SERVER).startSpan();
 *   try (Scope ss = tracer.withSpan(span)) {
 *     // Handle request and send response back.
 *   } finally {
 *     span.end();
 *   }
 * }
 * }</pre>
 *
 * @since 0.3.0
 */
@ThreadSafe
public interface ContextPropagators {

  /**
   * Returns a {@link HttpTextFormat} propagator.
   *
   * <p>The returned value will be a composite instance containing all the registered {@link
   * HttpTextFormat} propagators. If none is registered, the returned value will be a no-op
   * instance.
   *
   * @return the {@link HttpTextFormat} propagator to inject and extract data.
   * @since 0.3.0
   */
  HttpTextFormat getHttpTextFormat();
}
