/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the B3 propagation protocol. See <a
 * href=https://github.com/openzipkin/b3-propagation>openzipkin/b3-propagation</a>.
 *
 * <p>Also see <a
 * href=https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/context/api-propagators.md#b3-requirements>B3
 * Requirements</a>
 *
 * <p>To register the default B3 propagator, which injects a single header, use the default instance
 *
 * <pre>{@code
 * OpenTelemetry.setPropagators(
 *   DefaultContextPropagators
 *     .builder()
 *     .addTextMapPropagator(B3Propagator.getInstance())
 *     .build());
 * }</pre>
 *
 * <p>To register a B3 propagator that injects multiple headers, use the builder
 *
 * <pre>{@code
 * OpenTelemetry.setPropagators(
 *   DefaultContextPropagators
 *     .builder()
 *     .addTextMapPropagator(B3Propagator.builder().injectMultipleHeaders().build())
 *     .build());
 * }</pre>
 */
@Immutable
public class B3Propagator implements TextMapPropagator {
  static final String TRACE_ID_HEADER = "X-B3-TraceId";
  static final String SPAN_ID_HEADER = "X-B3-SpanId";
  static final String SAMPLED_HEADER = "X-B3-Sampled";
  static final String COMBINED_HEADER = "b3";
  static final String COMBINED_HEADER_DELIMITER = "-";

  static final char COMBINED_HEADER_DELIMITER_CHAR = '-';
  static final char IS_SAMPLED = '1';
  static final char NOT_SAMPLED = '0';

  private static final List<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER));

  private static final B3Propagator INSTANCE = B3Propagator.builder().build();

  private final B3PropagatorExtractor singleHeaderExtractor =
      new B3PropagatorExtractorSingleHeader();
  private final B3PropagatorExtractor multipleHeadersExtractor =
      new B3PropagatorExtractorMultipleHeaders();
  private final B3PropagatorInjector b3PropagatorInjector;

  private B3Propagator(B3PropagatorInjector b3PropagatorInjector) {
    this.b3PropagatorInjector = b3PropagatorInjector;
  }

  /**
   * Returns a new {@link B3Propagator.Builder} instance for configuring injection option for {@link
   * B3Propagator}.
   */
  public static B3Propagator.Builder builder() {
    return new Builder();
  }

  /**
   * Enables the creation of an {@link B3Propagator} instance with the ability to switch injector
   * from single header (default) to multiple headers.
   */
  public static class Builder {
    private boolean injectSingleHeader;

    private Builder() {
      injectSingleHeader = true;
    }

    public Builder injectMultipleHeaders() {
      this.injectSingleHeader = false;
      return this;
    }

    /** Create the {@link B3Propagator} with selected injector type. */
    public B3Propagator build() {
      if (injectSingleHeader) {
        return new B3Propagator(new B3PropagatorInjectorSingleHeader());
      } else {
        return new B3Propagator(new B3PropagatorInjectorMultipleHeaders());
      }
    }
  }

  public static B3Propagator getInstance() {
    return INSTANCE;
  }

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, Setter<C> setter) {
    b3PropagatorInjector.inject(context, carrier, setter);
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
    return Stream.<Supplier<Optional<Context>>>of(
            () -> singleHeaderExtractor.extract(context, carrier, getter),
            () -> multipleHeadersExtractor.extract(context, carrier, getter),
            () -> Optional.of(context))
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .get();
  }
}
