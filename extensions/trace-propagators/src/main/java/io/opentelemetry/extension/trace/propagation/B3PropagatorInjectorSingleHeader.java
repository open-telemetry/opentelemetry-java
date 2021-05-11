/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.B3Propagator.COMBINED_HEADER;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorInjectorSingleHeader implements B3PropagatorInjector {
  private static final int SAMPLED_FLAG_SIZE = 1;
  private static final int TRACE_ID_HEX_SIZE = TraceId.getLength();
  private static final int SPAN_ID_HEX_SIZE = SpanId.getLength();
  private static final int COMBINED_HEADER_DELIMITER_SIZE = 1;
  private static final int SPAN_ID_OFFSET = TRACE_ID_HEX_SIZE + COMBINED_HEADER_DELIMITER_SIZE;
  private static final int SAMPLED_FLAG_OFFSET =
      SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + COMBINED_HEADER_DELIMITER_SIZE;
  private static final int COMBINED_HEADER_SIZE = SAMPLED_FLAG_OFFSET + SAMPLED_FLAG_SIZE;
  private static final Collection<String> FIELDS = Collections.singletonList(COMBINED_HEADER);

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null) {
      return;
    }
    if (setter == null) {
      return;
    }

    SpanContext spanContext = Span.fromContext(context).getSpanContext();
    if (!spanContext.isValid()) {
      return;
    }

    char[] chars = new char[COMBINED_HEADER_SIZE];
    String traceId = spanContext.getTraceId();
    traceId.getChars(0, traceId.length(), chars, 0);
    chars[SPAN_ID_OFFSET - 1] = B3Propagator.COMBINED_HEADER_DELIMITER_CHAR;

    String spanId = spanContext.getSpanId();
    System.arraycopy(spanId.toCharArray(), 0, chars, SPAN_ID_OFFSET, SpanId.getLength());

    chars[SAMPLED_FLAG_OFFSET - 1] = B3Propagator.COMBINED_HEADER_DELIMITER_CHAR;
    if (Boolean.TRUE.equals(context.get(B3Propagator.DEBUG_CONTEXT_KEY))) {
      chars[SAMPLED_FLAG_OFFSET] = B3Propagator.DEBUG_SAMPLED;
    } else {
      chars[SAMPLED_FLAG_OFFSET] =
          spanContext.isSampled() ? B3Propagator.IS_SAMPLED : B3Propagator.NOT_SAMPLED;
    }
    setter.set(carrier, COMBINED_HEADER, new String(chars));
  }

  @Override
  public Collection<String> fields() {
    return FIELDS;
  }
}
