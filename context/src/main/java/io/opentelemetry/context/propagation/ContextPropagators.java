/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A container of the registered propagators for every supported format.
 *
 * <p>This container can be used to access a single, composite propagator for each supported format,
 * which will be responsible for injecting and extracting data for each registered concern (traces,
 * correlations, etc). Propagation will happen through {@link io.opentelemetry.context.Context},
 * from which values will be read upon injection, and which will store values from the extraction
 * step. The resulting {@code Context} can then be used implicitly or explicitly by the
 * OpenTelemetry API.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tracer tracer = OpenTelemetry.getTracer();
 * void onSendRequest() {
 *   try (Scope scope = TracingContextUtils.currentContextWith(span)) {
 *     ContextPropagators propagators = OpenTelemetry.getPropagators();
 *     TextMapPropagator textMapPropagator = propagators.getTextMapPropagator();
 *
 *     // Inject the span's SpanContext and other available concerns (such as correlations)
 *     // contained in the specified Context.
 *     Map<String, String> map = new HashMap<>();
 *     textMapPropagator.inject(Context.current(), map, new Setter<String, String>() {
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
 *   TextMapPropagator textMapPropagator = propagators.getTextMapPropagator();
 *
 *   // Extract and store the propagated span's SpanContext and other available concerns
 *   // in the specified Context.
 *   Context context = textMapPropagator.extract(Context.current(), request,
 *     new Getter<String, String>() {
 *       public String get(Object request, String key) {
 *         // Return the value associated to the key, if available.
 *       }
 *     }
 *   );
 *   Span span = tracer.spanBuilder("MyRequest")
 *       .setParent(context)
 *       .setSpanKind(Span.Kind.SERVER).startSpan();
 *   try (Scope ss = TracingContextUtils.currentContextWith(span)) {
 *     // Handle request and send response back.
 *   } finally {
 *     span.end();
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface ContextPropagators {

  /**
   * Returns a {@link ContextPropagators} which can be used to extract and inject context in text
   * payloads with the given {@link TextMapPropagator}. Use {@link
   * TextMapPropagator#composite(TextMapPropagator...)} to register multiple propagators, which will
   * all be executed when extracting or injecting.
   *
   * <pre>{@code
   * ContextPropagators propagators = ContextPropagators.create(
   *   TextMapPropagator.composite(
   *     HttpTraceContext.getInstance(),
   *     W3CBaggagePropagator.getInstance(),
   *     new MyCustomContextPropagator()));
   * }</pre>
   */
  @SuppressWarnings("deprecation")
  static ContextPropagators create(TextMapPropagator textPropagator) {
    requireNonNull(textPropagator, "textPropagator");
    return new DefaultContextPropagators(textPropagator);
  }

  /** Returns a {@link ContextPropagators} which performs no injection or extraction. */
  @SuppressWarnings("deprecation")
  static ContextPropagators noop() {
    return DefaultContextPropagators.noop();
  }

  /**
   * Returns a {@link TextMapPropagator} propagator.
   *
   * <p>The returned value will be a composite instance containing all the registered {@link
   * TextMapPropagator} propagators. If none is registered, the returned value will be a no-op
   * instance.
   *
   * @return the {@link TextMapPropagator} propagator to inject and extract data.
   */
  TextMapPropagator getTextMapPropagator();
}
