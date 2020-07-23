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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the B3 propagation protocol. See <a
 * href=https://github.com/openzipkin/b3-propagation>openzipkin/b3-propagation</a>.
 */
@Immutable
public class B3Propagator implements HttpTextFormat {
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

  private final B3PropagatorInjector b3PropagatorInjector;
  private final B3PropagatorExtractor b3PropagatorExtractor;

  private static final B3Propagator SINGLE_HEADER =
      new B3Propagator(
          new B3PropagatorInjectorSingleHeader(), new B3PropagatorExtractorSingleHeader());
  private static final B3Propagator MULTI_HEADER =
      new B3Propagator(
          new B3PropagatorInjectorMultipleHeaders(), new B3PropagatorExtractorMultipleHeaders());

  /**
   * Returns an instance of {@link B3Propagator} with Single Header Implementation of B3 propagation
   * protocol. See <a
   * href=https://github.com/openzipkin/b3-propagation#single-header>openzipkin/b3-propagation#single-header</a>.
   *
   * @return Returns an instance of {@link B3Propagator} with Single Header implementation of B3
   *     propagation protocol.
   */
  public static B3Propagator getSingleHeaderPropagator() {
    return SINGLE_HEADER;
  }

  /**
   * Returns an instance of {@link B3Propagator} with Multiple Header Implementation of B3
   * propagation protocol. See <a
   * href=https://github.com/openzipkin/b3-propagation#multiple-headers>openzipkin/b3-propagation#multiple-headers</a>.
   *
   * @return Returns an instance of {@link B3Propagator} with Multiple Header implementation of B3
   *     propagation protocol.
   */
  public static B3Propagator getMultipleHeaderPropagator() {
    return MULTI_HEADER;
  }

  private B3Propagator(
      B3PropagatorInjector b3PropagatorInjector, B3PropagatorExtractor b3PropagatorExtractor) {
    this.b3PropagatorInjector = b3PropagatorInjector;
    this.b3PropagatorExtractor = b3PropagatorExtractor;
  }

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    b3PropagatorInjector.inject(context, carrier, setter);
  }

  @Override
  public <C> Context extract(Context context, C carrier, Getter<C> getter) {
    return b3PropagatorExtractor.extract(context, carrier, getter);
  }
}
