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

package io.opentelemetry.contrib.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
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
  static final String TRUE_INT = "1";
  static final String FALSE_INT = "0";
  static final String COMBINED_HEADER = "b3";
  static final String COMBINED_HEADER_DELIMITER = "-";
  static final int MAX_TRACE_ID_LENGTH = 2 * TraceId.getSize();
  static final int MAX_SPAN_ID_LENGTH = 2 * SpanId.getSize();

  static final char COMBINED_HEADER_DELIMITER_CHAR = '-';
  static final char IS_SAMPLED = '1';
  static final char NOT_SAMPLED = '0';

  private static final List<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER));

  private final B3PropagatorInjector b3PropagatorInjector;
  private final B3PropagatorExtractor b3PropagatorExtractor;

  /** Creates a new instance of {@link B3Propagator}. Defaults to use multiple headers. */
  public B3Propagator() {
    this(false);
  }

  /**
   * Creates a new instance of {@link B3Propagator}.
   *
   * @param singleHeader whether to use single or multiple headers.
   */
  public B3Propagator(boolean singleHeader) {
    if (singleHeader) {
      b3PropagatorInjector = new B3PropagatorInjectorSingleHeader();
      b3PropagatorExtractor = new B3PropagatorExtractorSingleHeader();
    } else {
      b3PropagatorInjector = new B3PropagatorInjectorMultipleHeaders();
      b3PropagatorExtractor = new B3PropagatorExtractorMultipleHeaders();
    }
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
