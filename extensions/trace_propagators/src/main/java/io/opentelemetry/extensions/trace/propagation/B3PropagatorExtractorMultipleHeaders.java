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

import static io.opentelemetry.extensions.trace.propagation.B3Propagator.SAMPLED_HEADER;
import static io.opentelemetry.extensions.trace.propagation.B3Propagator.SPAN_ID_HEADER;
import static io.opentelemetry.extensions.trace.propagation.B3Propagator.TRACE_ID_HEADER;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorExtractorMultipleHeaders implements B3PropagatorExtractor {
  private static final Logger logger =
      Logger.getLogger(B3PropagatorExtractorMultipleHeaders.class.getName());

  @Override
  public <C> Context extract(Context context, C carrier, HttpTextFormat.Getter<C> getter) {
    Objects.requireNonNull(carrier, "carrier");
    Objects.requireNonNull(getter, "getter");
    SpanContext spanContext = getSpanContextFromMultipleHeaders(carrier, getter);
    if (!spanContext.isValid()) {
      return context;
    }

    return TracingContextUtils.withSpan(DefaultSpan.create(spanContext), context);
  }

  private static <C> SpanContext getSpanContextFromMultipleHeaders(
      C carrier, HttpTextFormat.Getter<C> getter) {
    String traceId = getter.get(carrier, TRACE_ID_HEADER);
    if (StringUtils.isNullOrEmpty(traceId)) {
      return SpanContext.getInvalid();
    }
    if (!Common.isTraceIdValid(traceId)) {
      logger.fine(
          "Invalid TraceId in B3 header: " + traceId + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String spanId = getter.get(carrier, SPAN_ID_HEADER);
    if (!Common.isSpanIdValid(spanId)) {
      logger.fine("Invalid SpanId in B3 header: " + spanId + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String sampled = getter.get(carrier, SAMPLED_HEADER);
    return Common.buildSpanContext(traceId, spanId, sampled);
  }
}
