/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
 * <p>To register the default B3 propagator, which injects a single header, use:
 *
 * <pre>{@code
 * OpenTelemetry.setPropagators(
 *   DefaultContextPropagators
 *     .builder()
 *     .addTextMapPropagator(B3Propagator.injectingSingleHeader())
 *     .build());
 * }</pre>
 *
 * <p>To register a B3 propagator that injects multiple headers, use:
 *
 * <pre>{@code
 * OpenTelemetry.setPropagators(
 *   DefaultContextPropagators
 *     .builder()
 *     .addTextMapPropagator(B3Propagator.injectingMultiHeaders())
 *     .build());
 * }</pre>
 */
@Immutable
public final class B3Propagator implements TextMapPropagator {
  static final String TRACE_ID_HEADER = "X-B3-TraceId";
  static final String SPAN_ID_HEADER = "X-B3-SpanId";
  static final String SAMPLED_HEADER = "X-B3-Sampled";
  static final String DEBUG_HEADER = "X-B3-Flags";
  static final String COMBINED_HEADER = "b3";
  static final String COMBINED_HEADER_DELIMITER = "-";
  static final ContextKey<Boolean> DEBUG_CONTEXT_KEY = ContextKey.named("b3-debug");
  static final String MULTI_HEADER_DEBUG = "1";
  static final String SINGLE_HEADER_DEBUG = "d";

  static final char COMBINED_HEADER_DELIMITER_CHAR = '-';
  static final char IS_SAMPLED = '1';
  static final char NOT_SAMPLED = '0';
  static final char DEBUG_SAMPLED = 'd';

  private static final Collection<String> FIELDS =
      Collections.unmodifiableList(
          Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER, COMBINED_HEADER));

  private static final B3Propagator SINGLE_HEADER_INSTANCE =
      new B3Propagator(new B3PropagatorInjectorSingleHeader());
  private static final B3Propagator MULTI_HEADERS_INSTANCE =
      new B3Propagator(new B3PropagatorInjectorMultipleHeaders());

  private final B3PropagatorExtractor singleHeaderExtractor =
      new B3PropagatorExtractorSingleHeader();
  private final B3PropagatorExtractor multipleHeadersExtractor =
      new B3PropagatorExtractorMultipleHeaders();
  private final B3PropagatorInjector b3PropagatorInjector;

  private B3Propagator(B3PropagatorInjector b3PropagatorInjector) {
    this.b3PropagatorInjector = b3PropagatorInjector;
  }

  /**
   * Returns an instance of the {@link B3Propagator} that injects multi headers format.
   *
   * <p>This instance extracts both formats, in the order: single header, multi header.
   *
   * @return an instance of the {@link B3Propagator} that injects multi headers format.
   */
  public static B3Propagator injectingMultiHeaders() {
    return MULTI_HEADERS_INSTANCE;
  }

  /**
   * Returns an instance of the {@link B3Propagator} that injects single header format.
   *
   * <p>This instance extracts both formats, in the order: single header, multi header.
   *
   * <p>This is the default instance for {@link B3Propagator}.
   *
   * @return an instance of the {@link B3Propagator} that injects single header format.
   */
  public static B3Propagator injectingSingleHeader() {
    return SINGLE_HEADER_INSTANCE;
  }

  @Override
  public Collection<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    b3PropagatorInjector.inject(context, carrier, setter);
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    return Stream.<Supplier<Optional<Context>>>of(
            () -> singleHeaderExtractor.extract(context, carrier, getter),
            () -> multipleHeadersExtractor.extract(context, carrier, getter),
            () -> Optional.ofNullable(context))
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElse(Context.root());
  }
}
