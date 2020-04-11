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

import static io.opentelemetry.internal.Utils.checkArgument;
import static io.opentelemetry.internal.Utils.checkNotNull;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the TraceContext propagation protocol. See <a
 * href=https://github.com/w3c/distributed-tracing>w3c/distributed-tracing</a>.
 */
@Immutable
public class HttpTraceContext implements HttpTextFormat {
  private static final Logger logger = Logger.getLogger(HttpTraceContext.class.getName());

  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  static final String TRACE_PARENT = "traceparent";
  static final String TRACE_STATE = "tracestate";
  private static final List<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_PARENT, TRACE_STATE));

  private static final String VERSION = "00";
  private static final int VERSION_SIZE = 2;
  private static final char TRACEPARENT_DELIMITER = '-';
  private static final int TRACEPARENT_DELIMITER_SIZE = 1;
  private static final int TRACE_ID_HEX_SIZE = 2 * TraceId.getSize();
  private static final int SPAN_ID_HEX_SIZE = 2 * SpanId.getSize();
  private static final int TRACE_OPTION_HEX_SIZE = 2 * TraceFlags.getSize();
  private static final int TRACE_ID_OFFSET = VERSION_SIZE + TRACEPARENT_DELIMITER_SIZE;
  private static final int SPAN_ID_OFFSET =
      TRACE_ID_OFFSET + TRACE_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
  private static final int TRACE_OPTION_OFFSET =
      SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
  private static final int TRACEPARENT_HEADER_SIZE = TRACE_OPTION_OFFSET + TRACE_OPTION_HEX_SIZE;
  private static final int TRACESTATE_MAX_SIZE = 512;
  private static final int TRACESTATE_MAX_MEMBERS = 32;
  private static final char TRACESTATE_KEY_VALUE_DELIMITER = '=';
  private static final char TRACESTATE_ENTRY_DELIMITER = ',';
  private static final Pattern TRACESTATE_ENTRY_DELIMITER_SPLIT_PATTERN =
      Pattern.compile("[ \t]*" + TRACESTATE_ENTRY_DELIMITER + "[ \t]*");

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    checkNotNull(context, "context");
    checkNotNull(setter, "setter");

    Span span = TracingContextUtils.getSpanWithoutDefault(context);
    if (span == null) {
      return;
    }

    injectImpl(span.getContext(), carrier, setter);
  }

  private static <C> void injectImpl(SpanContext spanContext, C carrier, Setter<C> setter) {
    char[] chars = new char[TRACEPARENT_HEADER_SIZE];
    chars[0] = VERSION.charAt(0);
    chars[1] = VERSION.charAt(1);
    chars[2] = TRACEPARENT_DELIMITER;
    spanContext.getTraceId().copyLowerBase16To(chars, TRACE_ID_OFFSET);
    chars[SPAN_ID_OFFSET - 1] = TRACEPARENT_DELIMITER;
    spanContext.getSpanId().copyLowerBase16To(chars, SPAN_ID_OFFSET);
    chars[TRACE_OPTION_OFFSET - 1] = TRACEPARENT_DELIMITER;
    spanContext.getTraceFlags().copyLowerBase16To(chars, TRACE_OPTION_OFFSET);
    setter.set(carrier, TRACE_PARENT, new String(chars));
    List<TraceState.Entry> entries = spanContext.getTraceState().getEntries();
    if (entries.isEmpty()) {
      // No need to add an empty "tracestate" header.
      return;
    }
    StringBuilder stringBuilder = new StringBuilder(TRACESTATE_MAX_SIZE);
    for (TraceState.Entry entry : entries) {
      if (stringBuilder.length() != 0) {
        stringBuilder.append(TRACESTATE_ENTRY_DELIMITER);
      }
      stringBuilder
          .append(entry.getKey())
          .append(TRACESTATE_KEY_VALUE_DELIMITER)
          .append(entry.getValue());
    }
    setter.set(carrier, TRACE_STATE, stringBuilder.toString());
  }

  @Override
  public <C /*>>> extends @NonNull Object*/> Context extract(
      Context context, C carrier, Getter<C> getter) {
    checkNotNull(context, "context");
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");

    SpanContext spanContext = extractImpl(carrier, getter);
    return TracingContextUtils.withSpan(DefaultSpan.create(spanContext), context);
  }

  private static <C> SpanContext extractImpl(C carrier, Getter<C> getter) {
    String traceParent = getter.get(carrier, TRACE_PARENT);
    if (traceParent == null) {
      return SpanContext.getInvalid();
    }

    SpanContext contextFromParentHeader = extractContextFromTraceParent(traceParent);
    if (!contextFromParentHeader.isValid()) {
      return contextFromParentHeader;
    }

    String traceStateHeader = getter.get(carrier, TRACE_STATE);
    if (traceStateHeader == null || traceStateHeader.isEmpty()) {
      return contextFromParentHeader;
    }

    try {
      TraceState traceState = extractTraceState(traceStateHeader);
      return SpanContext.createFromRemoteParent(
          contextFromParentHeader.getTraceId(),
          contextFromParentHeader.getSpanId(),
          contextFromParentHeader.getTraceFlags(),
          traceState);
    } catch (IllegalArgumentException e) {
      logger.info("Unparseable tracestate header. Returning span context without state.");
      return contextFromParentHeader;
    }
  }

  private static SpanContext extractContextFromTraceParent(String traceparent) {
    // TODO(bdrutu): Do we need to verify that version is hex and that
    // for the version the length is the expected one?
    boolean isValid =
        (traceparent.length() == TRACEPARENT_HEADER_SIZE
                || (traceparent.length() > TRACEPARENT_HEADER_SIZE
                    && traceparent.charAt(TRACEPARENT_HEADER_SIZE) == TRACEPARENT_DELIMITER))
            && traceparent.charAt(TRACE_ID_OFFSET - 1) == TRACEPARENT_DELIMITER
            && traceparent.charAt(SPAN_ID_OFFSET - 1) == TRACEPARENT_DELIMITER
            && traceparent.charAt(TRACE_OPTION_OFFSET - 1) == TRACEPARENT_DELIMITER;
    if (!isValid) {
      logger.info("Unparseable traceparent header. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    try {
      TraceId traceId = TraceId.fromLowerBase16(traceparent, TRACE_ID_OFFSET);
      SpanId spanId = SpanId.fromLowerBase16(traceparent, SPAN_ID_OFFSET);
      TraceFlags traceFlags = TraceFlags.fromLowerBase16(traceparent, TRACE_OPTION_OFFSET);
      return SpanContext.createFromRemoteParent(traceId, spanId, traceFlags, TRACE_STATE_DEFAULT);
    } catch (IllegalArgumentException e) {
      logger.info("Unparseable traceparent header. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }
  }

  private static TraceState extractTraceState(String traceStateHeader) {
    TraceState.Builder traceStateBuilder = TraceState.builder();
    String[] listMembers = TRACESTATE_ENTRY_DELIMITER_SPLIT_PATTERN.split(traceStateHeader);
    checkArgument(
        listMembers.length <= TRACESTATE_MAX_MEMBERS, "TraceState has too many elements.");
    // Iterate in reverse order because when call builder set the elements is added in the
    // front of the list.
    for (int i = listMembers.length - 1; i >= 0; i--) {
      String listMember = listMembers[i];
      int index = listMember.indexOf(TRACESTATE_KEY_VALUE_DELIMITER);
      checkArgument(index != -1, "Invalid TraceState list-member format.");
      traceStateBuilder.set(listMember.substring(0, index), listMember.substring(index + 1));
    }
    return traceStateBuilder.build();
  }
}
