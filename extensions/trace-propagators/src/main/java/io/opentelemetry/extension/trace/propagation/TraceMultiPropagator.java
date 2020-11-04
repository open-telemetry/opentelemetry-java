/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A propagator designed to inject and extract multiple trace {@code TextMapPropagator} propagators,
 * intended for backwards compatibility with existing services using different formats. It works in
 * a stack-fashion, starting with the last registered propagator, to the first one.
 *
 * <p>The propagation fields retrieved from all registered propagators are de-duplicated.
 *
 * <p>Upon injection, this propagator invokes {@code TextMapPropagator#inject()} for every
 * registered trace propagator. This will result in the carrier containing all the registered
 * formats.
 *
 * <p>Upon extraction, this propagator invokes {@code TextMapPropagator#extract()} for every
 * registered trace propagator, returning immediately when a successful extraction happened.
 *
 * <pre>{@code
 * TextMapPropagator traceFormats = TraceMultiPropagator.builder()
 *   .addPropagator(new MyCustomTracePropagator())
 *   .addPropagator(new JaegerPropagator())
 *   .addPropagator(new HttpTraceContext())
 *   .build();
 * // Register it in the global propagators:
 * OpenTelemetry.setPropagators(
 *     DefaultContextPropagators.builder()
 *       .addTextMapPropagator(traceFormats)
 *       .build());
 * ...
 * // Extraction will be performed in reverse  order, i.e. starting with the last
 * // registered propagator (HttpTraceContext in this example).
 * Context context = OpenTelemetry.getPropagators().getTextMapPropagator()
 *   .extract(context, carrier, carrierGetter);
 * }</pre>
 */
@Immutable
public class TraceMultiPropagator implements TextMapPropagator {
  private final TextMapPropagator[] propagators;
  private final List<String> propagatorsFields;

  private TraceMultiPropagator(List<TextMapPropagator> propagatorList) {
    this.propagators = new TextMapPropagator[propagatorList.size()];
    propagatorList.toArray(this.propagators);

    Set<String> fields = new LinkedHashSet<>();
    for (TextMapPropagator propagator : propagators) {
      fields.addAll(propagator.fields());
    }
    this.propagatorsFields = Collections.unmodifiableList(new ArrayList<>(fields));
  }

  /**
   * Returns a {@link TraceMultiPropagator.Builder} to create a new {@link TraceMultiPropagator}
   * object.
   *
   * @return a {@link TraceMultiPropagator.Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The propagation fields defined in all the registered propagators. The returned list will be
   * read-only.
   *
   * @return list of fields defined in all the registered propagators.
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
   */
  @Override
  public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
    for (int i = propagators.length - 1; i >= 0; i--) {
      context = propagators[i].extract(context, carrier, getter);
      if (isSpanContextExtracted(context)) {
        break;
      }
    }

    return context;
  }

  private static boolean isSpanContextExtracted(Context context) {
    return Span.fromContextOrNull(context) != null;
  }

  /**
   * {@link Builder} is used to construct a new {@code TraceMultiPropagator} object with the
   * specified propagators.
   */
  public static class Builder {
    private final List<TextMapPropagator> propagators;

    private Builder() {
      propagators = new ArrayList<>();
    }

    /**
     * Adds a {@link TextMapPropagator} trace propagator.
     *
     * <p>Registered propagators will be invoked in reverse order, starting with the last propagator
     * to the first one.
     *
     * @param propagator the propagator to be added.
     * @return this.
     * @throws NullPointerException if {@code propagator} is {@code null}.
     */
    public Builder addPropagator(TextMapPropagator propagator) {
      Objects.requireNonNull(propagator, "propagator");

      propagators.add(propagator);
      return this;
    }

    /**
     * Builds a new {@code TraceMultiPropagator} with the specified propagators.
     *
     * @return the newly created {@code TraceMultiPropagator} instance.
     */
    public TraceMultiPropagator build() {
      return new TraceMultiPropagator(propagators);
    }
  }
}
