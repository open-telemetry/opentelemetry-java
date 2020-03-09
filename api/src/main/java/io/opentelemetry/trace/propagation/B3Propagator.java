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

package io.opentelemetry.trace.propagation;

import static io.opentelemetry.internal.Utils.checkNotNull;

import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the B3 propagation protocol. See <a
 * href=https://github.com/openzipkin/b3-propagation>openzipkin/b3-propagation</a>.
 */
@Immutable
public class B3Propagator implements HttpTextFormat<SpanContext> {
  private static final Logger logger = Logger.getLogger(HttpTraceContext.class.getName());

  static final String TRACE_ID_HEADER = "X-B3-TraceId";
  static final String SPAN_ID_HEADER = "X-B3-SpanId";
  static final String SAMPLED_HEADER = "X-B3-Sampled";
  static final String TRUE_INT = "1";
  static final String FALSE_INT = "0";

  private static final int MAX_TRACE_ID_LENGTH = 2 * TraceId.getSize();
  private static final int MAX_SPAN_ID_LENGTH = 2 * SpanId.getSize();

  private static final List<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER));

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    checkNotNull(spanContext, "spanContext");
    checkNotNull(setter, "setter");
    checkNotNull(carrier, "carrier");

    setter.set(carrier, TRACE_ID_HEADER, spanContext.getTraceId().toLowerBase16());
    setter.set(carrier, SPAN_ID_HEADER, spanContext.getSpanId().toLowerBase16());
    setter.set(
        carrier, SAMPLED_HEADER, spanContext.getTraceFlags().isSampled() ? TRUE_INT : FALSE_INT);
  }

  @Override
  public <C /*>>> extends @NonNull Object*/> SpanContext extract(C carrier, Getter<C> getter) {
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");

    try {
      String traceId = getter.get(carrier, TRACE_ID_HEADER);
      if (traceId == null || traceId.isEmpty() || traceId.length() > MAX_TRACE_ID_LENGTH) {
        return SpanContext.getInvalid();
      }
      String spanId = getter.get(carrier, SPAN_ID_HEADER);
      if (spanId == null || traceId.isEmpty() || spanId.length() > MAX_SPAN_ID_LENGTH) {
        return SpanContext.getInvalid();
      }
      String sampled = getter.get(carrier, SAMPLED_HEADER);
      TraceFlags traceFlags =
          TraceFlags.builder()
              .setIsSampled(
                  TRUE_INT.equals(sampled)
                      || Boolean.parseBoolean(sampled)) // accept either "1" or "true"
              .build();

      return SpanContext.createFromRemoteParent(
          TraceId.fromLowerBase16(traceId, 0),
          SpanId.fromLowerBase16(spanId, 0),
          traceFlags,
          TraceState.getDefault());
    } catch (Exception exception) {
      logger.info("Unable to retrieve B3 encoded span context. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }
  }
}
