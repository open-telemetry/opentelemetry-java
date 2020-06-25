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

package io.opentelemetry.extensions.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * A propagator designed to inject and extract multiple trace {@code HttpTextFormat} propagators,
 * intendended for backwards compatibility with existing services using different formats. It works
 * in a stack-fashion, starting with the last registered propagator, to the first one.
 *
 * <p>Upon injection, this propagator invokes {@code HttpTextFormat#inject()} for every registered
 * trace propagator. This will result in the carrier containing all the registered formats.
 *
 * <p>Upon extraction, this propagator invokes {@code HttpTextFormat#extract()} for every registered
 * trace propagator, returning immediately when a successful extraction happened.
 *
 * <pre>{@code
 * HttpTextFormat traceFormats = StackTracePropagator.builder()
 *   .addPropagator(new MyCustomTracePropagator())
 *   .addPropagator(new JaegerPropagator())
 *   .addPropagator(new HttpTraceContext())
 *   .build();
 * // Register it in the global propagators:
 * OpenTelemetry.setPropagators(
 *     DefaultContextPropagators.builder()
 *       .addHttpTextFormat(traceFormats)
 *       .build());
 * ...
 * // Extraction will be performed in reverse  order, i.e. starting with the last
 * // registered propagator (HttpTraceContext in this example).
 * Context context = OpenTelemetry.getPropagators().getHttpTextFormat()
 *   .extract(context, carrier, carrierGetter);
 * }</pre>
 *
 * @since 0.6.0
 */
@Immutable
public class StackTracePropagator implements HttpTextFormat {
  private final HttpTextFormat[] propagators;
  private final List<String> propagatorsFields;

  private StackTracePropagator(List<HttpTextFormat> propagatorList) {
    this.propagators = new HttpTextFormat[propagatorList.size()];
    propagatorList.toArray(this.propagators);

    List<String> fields = new ArrayList<>();
    for (HttpTextFormat propagator : propagators) {
      fields.addAll(propagator.fields());
    }
    this.propagatorsFields = Collections.unmodifiableList(fields);
  }

  /**
   * Returns a {@link StackTracePropagator.Builder} to create a new {@link StackTracePropagator}
   * object.
   *
   * @return a {@link StackTracePropagator.Builder}.
   * @since 0.6.0
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The propagation fields defined in all the registered propagators. The returned list will be
   * read-only.
   *
   * @return list of fields defined in all the registered propagators.
   * @since 0.6.0
   */
  @Override
  public List<String> fields() {
    return propagatorsFields;
  }

  /**
   * Injects the value downstream invoking all the registered propagators, starting with the last
   * one.
   *
   * @param context the {@code Context} containing the value to be injected.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param setter invoked for each propagation key to add or remove.
   * @param <C> carrier of propagation fields, such as an http request
   * @since 0.6.0
   */
  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    for (int i = propagators.length - 1; i >= 0; i--) {
      propagators[i].inject(context, carrier, setter);
    }
  }

  /**
   * Extracts the value from upstream invoking all the registered propagators, starting with the
   * last one. Iterating over the propagators will stop and return immediately upon the first
   * successful extraction.
   *
   * @param context the {@code Context} used to store the extracted value.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param getter invoked for each propagation key to get.
   * @param <C> carrier of propagation fields, such as an http request.
   * @return the {@code Context} containing the extracted value.
   * @since 0.6.0
   */
  @Override
  public <C> Context extract(Context context, C carrier, Getter<C> getter) {
    for (int i = propagators.length - 1; i >= 0; i--) {
      context = propagators[i].extract(context, carrier, getter);
      if (TracingContextUtils.getSpanWithoutDefault(context) != null) {
        break;
      }
    }

    return context;
  }

  /**
   * {@link Builder} is used to construct a new {@code StackTracePropagator} object with the
   * specified propagators.
   *
   * @since 0.6.0
   */
  public static class Builder {
    private final List<HttpTextFormat> propagators;

    private Builder() {
      propagators = new ArrayList<>();
    }

    /**
     * Adds a {@link HttpTextFormat} trace propagator.
     *
     * <p>Registered propagators will be invoked in reverse order, starting with the last propagator
     * to the first one.
     *
     * @param propagator the propagator to be added.
     * @return this.
     * @throws NullPointerException if {@code propagator} is {@code null}.
     * @since 0.6.0
     */
    public Builder addPropagator(HttpTextFormat propagator) {
      Objects.requireNonNull(propagator, "propagator");

      propagators.add(propagator);
      return this;
    }

    /**
     * Builds a new {@code StackTracePropagator} with the specified propagators.
     *
     * @return the newly created {@code StackTracePropagator} instance.
     * @since 0.6.0
     */
    public StackTracePropagator build() {
      return new StackTracePropagator(propagators);
    }
  }
}
