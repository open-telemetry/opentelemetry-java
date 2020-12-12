/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
 * TextMapPropagator traceFormats = TraceMultiPropagator.create(
 *     new HttpTraceContext(), new JaegerPropagator(), new MyCustomTracePropagator())
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

  /** Returns a {@link TraceMultiPropagator} for the given {@code propagators}. */
  public static TextMapPropagator create(TextMapPropagator... propagators) {
    return create(Arrays.asList(propagators));
  }

  /** Returns a {@link TraceMultiPropagator} for the given {@code propagators}. */
  public static TextMapPropagator create(Iterable<TextMapPropagator> propagators) {
    List<TextMapPropagator> propagatorsList = new ArrayList<>();
    for (TextMapPropagator propagator : propagators) {
      requireNonNull(propagator, "propagator");
      propagatorsList.add(propagator);
    }
    if (propagatorsList.isEmpty()) {
      return TextMapPropagator.noop();
    }
    if (propagatorsList.size() == 1) {
      return propagatorsList.get(0);
    }
    return new TraceMultiPropagator(propagatorsList);
  }

  private final TextMapPropagator[] propagators;
  private final Collection<String> propagatorsFields;

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
   * The propagation fields defined in all the registered propagators. The returned list will be
   * read-only.
   *
   * @return list of fields defined in all the registered propagators.
   */
  @Override
  public Collection<String> fields() {
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
}
