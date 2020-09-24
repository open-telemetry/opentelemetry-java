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
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorInjectorSingleHeader implements B3PropagatorInjector {
  private static final int SAMPLED_FLAG_SIZE = 1;
  private static final int TRACE_ID_HEX_SIZE = TraceId.getHexLength();
  private static final int SPAN_ID_HEX_SIZE = SpanId.getHexLength();
  private static final int COMBINED_HEADER_DELIMITER_SIZE = 1;
  private static final int SPAN_ID_OFFSET = TRACE_ID_HEX_SIZE + COMBINED_HEADER_DELIMITER_SIZE;
  private static final int SAMPLED_FLAG_OFFSET =
      SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + COMBINED_HEADER_DELIMITER_SIZE;
  private static final int COMBINED_HEADER_SIZE = SAMPLED_FLAG_OFFSET + SAMPLED_FLAG_SIZE;

  @Override
  public <C> void inject(Context context, C carrier, TextMapPropagator.Setter<C> setter) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(setter, "setter");

    SpanContext spanContext = TracingContextUtils.getSpan(context).getContext();
    if (!spanContext.isValid()) {
      return;
    }

    char[] chars = new char[COMBINED_HEADER_SIZE];
    String traceId = spanContext.getTraceIdAsHexString();
    traceId.getChars(0, traceId.length(), chars, 0);
    chars[SPAN_ID_OFFSET - 1] = B3Propagator.COMBINED_HEADER_DELIMITER_CHAR;

    String spanId = spanContext.getSpanIdAsHexString();
    System.arraycopy(spanId.toCharArray(), 0, chars, SPAN_ID_OFFSET, SpanId.getHexLength());

    chars[SAMPLED_FLAG_OFFSET - 1] = B3Propagator.COMBINED_HEADER_DELIMITER_CHAR;
    chars[SAMPLED_FLAG_OFFSET] =
        spanContext.isSampled() ? B3Propagator.IS_SAMPLED : B3Propagator.NOT_SAMPLED;
    setter.set(carrier, B3Propagator.COMBINED_HEADER, new String(chars));
  }
}
