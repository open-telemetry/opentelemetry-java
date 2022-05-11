/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.aws;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Implementation of the AWS X-Ray Trace Header propagation protocol. See <a href=
 * https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader>AWS
 * Tracing header spec</a>
 *
 * <p>To register the X-Ray propagator together with default propagator when using the SDK:
 *
 * <pre>{@code
 * OpenTelemetrySdk.builder()
 *   .setPropagators(
 *     ContextPropagators.create(
 *         TextMapPropagator.composite(
 *             W3CTraceContextPropagator.getInstance(),
 *             AWSXrayPropagator.getInstance())))
 *    .build();
 * }</pre>
 */
public final class AwsXrayPropagator implements TextMapPropagator {

  // Visible for testing
  static final String TRACE_HEADER_KEY = "X-Amzn-Trace-Id";

  private static final Logger logger = Logger.getLogger(AwsXrayPropagator.class.getName());

  private static final char TRACE_HEADER_DELIMITER = ';';
  private static final char KV_DELIMITER = '=';

  private static final String TRACE_ID_KEY = "Root";
  private static final int TRACE_ID_LENGTH = 35;
  private static final String TRACE_ID_VERSION = "1";
  private static final char TRACE_ID_DELIMITER = '-';
  private static final int TRACE_ID_DELIMITER_INDEX_1 = 1;
  private static final int TRACE_ID_DELIMITER_INDEX_2 = 10;
  private static final int TRACE_ID_FIRST_PART_LENGTH = 8;

  private static final String PARENT_ID_KEY = "Parent";
  private static final int PARENT_ID_LENGTH = 16;

  private static final String SAMPLED_FLAG_KEY = "Sampled";
  private static final int SAMPLED_FLAG_LENGTH = 1;
  private static final char IS_SAMPLED = '1';
  private static final char NOT_SAMPLED = '0';

  private static final Collection<String> FIELDS = Collections.singletonList(TRACE_HEADER_KEY);

  private static final AwsXrayPropagator INSTANCE = new AwsXrayPropagator();

  private AwsXrayPropagator() {
    // singleton
  }

  public static AwsXrayPropagator getInstance() {
    return INSTANCE;
  }

  @Override
  public Collection<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null) {
      return;
    }
    if (setter == null) {
      return;
    }

    Span span = Span.fromContext(context);
    if (!span.getSpanContext().isValid()) {
      return;
    }

    SpanContext spanContext = span.getSpanContext();

    String otTraceId = spanContext.getTraceId();
    String xrayTraceId =
        TRACE_ID_VERSION
            + TRACE_ID_DELIMITER
            + otTraceId.substring(0, TRACE_ID_FIRST_PART_LENGTH)
            + TRACE_ID_DELIMITER
            + otTraceId.substring(TRACE_ID_FIRST_PART_LENGTH);
    String parentId = spanContext.getSpanId();
    char samplingFlag = spanContext.isSampled() ? IS_SAMPLED : NOT_SAMPLED;
    // TODO: Add OT trace state to the X-Ray trace header

    StringBuilder traceHeader = new StringBuilder();
    traceHeader
        .append(TRACE_ID_KEY)
        .append(KV_DELIMITER)
        .append(xrayTraceId)
        .append(TRACE_HEADER_DELIMITER)
        .append(PARENT_ID_KEY)
        .append(KV_DELIMITER)
        .append(parentId)
        .append(TRACE_HEADER_DELIMITER)
        .append(SAMPLED_FLAG_KEY)
        .append(KV_DELIMITER)
        .append(samplingFlag);

    Baggage baggage = Baggage.fromContext(context);
    // Truncate baggage to 256 chars per X-Ray spec.
    baggage.forEach(
        new BiConsumer<String, BaggageEntry>() {

          private int baggageWrittenBytes;

          @Override
          public void accept(String key, BaggageEntry entry) {
            if (key.equals(TRACE_ID_KEY)
                || key.equals(PARENT_ID_KEY)
                || key.equals(SAMPLED_FLAG_KEY)) {
              return;
            }
            // Size is key/value pair, excludes delimiter.
            int size = key.length() + entry.getValue().length() + 1;
            if (baggageWrittenBytes + size > 256) {
              return;
            }
            traceHeader
                .append(TRACE_HEADER_DELIMITER)
                .append(key)
                .append(KV_DELIMITER)
                .append(entry.getValue());
            baggageWrittenBytes += size;
          }
        });

    setter.set(carrier, TRACE_HEADER_KEY, traceHeader.toString());
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      return Context.root();
    }
    if (getter == null) {
      return context;
    }

    return getContextFromHeader(context, carrier, getter);
  }

  private static <C> Context getContextFromHeader(
      Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    String traceHeader = getter.get(carrier, TRACE_HEADER_KEY);
    if (traceHeader == null || traceHeader.isEmpty()) {
      return context;
    }

    String traceId = TraceId.getInvalid();
    String spanId = SpanId.getInvalid();
    Boolean isSampled = false;

    BaggageBuilder baggage = null;
    int baggageReadBytes = 0;

    int pos = 0;
    while (pos < traceHeader.length()) {
      int delimiterIndex = traceHeader.indexOf(TRACE_HEADER_DELIMITER, pos);
      String part;
      if (delimiterIndex >= 0) {
        part = traceHeader.substring(pos, delimiterIndex);
        pos = delimiterIndex + 1;
      } else {
        // Last part.
        part = traceHeader.substring(pos);
        pos = traceHeader.length();
      }
      String trimmedPart = part.trim();
      int equalsIndex = trimmedPart.indexOf(KV_DELIMITER);
      if (equalsIndex < 0) {
        logger.fine("Error parsing X-Ray trace header. Invalid key value pair: " + part);
        return context;
      }

      String value = trimmedPart.substring(equalsIndex + 1);

      if (trimmedPart.startsWith(TRACE_ID_KEY)) {
        traceId = parseTraceId(value);
      } else if (trimmedPart.startsWith(PARENT_ID_KEY)) {
        spanId = parseSpanId(value);
      } else if (trimmedPart.startsWith(SAMPLED_FLAG_KEY)) {
        isSampled = parseTraceFlag(value);
      } else if (baggageReadBytes + trimmedPart.length() <= 256) {
        if (baggage == null) {
          baggage = Baggage.builder();
        }
        baggage.put(trimmedPart.substring(0, equalsIndex), value);
        baggageReadBytes += trimmedPart.length();
      }
    }
    if (isSampled == null) {
      logger.fine(
          "Invalid Sampling flag in X-Ray trace header: '"
              + TRACE_HEADER_KEY
              + "' with value "
              + traceHeader
              + "'.");
      return context;
    }

    if (spanId == null || traceId == null) {
      logger.finest("Both traceId and spanId are required to extract a valid span context. ");
    }

    SpanContext spanContext =
        SpanContext.createFromRemoteParent(
            StringUtils.padLeft(traceId, TraceId.getLength()),
            spanId,
            isSampled ? TraceFlags.getSampled() : TraceFlags.getDefault(),
            TraceState.getDefault());
    if (spanContext.isValid()) {
      context = context.with(Span.wrap(spanContext));
    }
    if (baggage != null) {
      context = context.with(baggage.build());
    }
    return context;
  }

  private static String parseTraceId(String xrayTraceId) {
    return (xrayTraceId.length() == TRACE_ID_LENGTH
        ? parseSpecTraceId(xrayTraceId)
        : parseShortTraceId(xrayTraceId));
  }

  private static String parseSpecTraceId(String xrayTraceId) {

    // Check version trace id version
    if (!xrayTraceId.startsWith(TRACE_ID_VERSION)) {
      return TraceId.getInvalid();
    }

    // Check delimiters
    if (xrayTraceId.charAt(TRACE_ID_DELIMITER_INDEX_1) != TRACE_ID_DELIMITER
        || xrayTraceId.charAt(TRACE_ID_DELIMITER_INDEX_2) != TRACE_ID_DELIMITER) {
      return TraceId.getInvalid();
    }

    String epochPart =
        xrayTraceId.substring(TRACE_ID_DELIMITER_INDEX_1 + 1, TRACE_ID_DELIMITER_INDEX_2);
    String uniquePart = xrayTraceId.substring(TRACE_ID_DELIMITER_INDEX_2 + 1, TRACE_ID_LENGTH);

    // X-Ray trace id format is 1-{8 digit hex}-{24 digit hex}
    return epochPart + uniquePart;
  }

  private static String parseShortTraceId(String xrayTraceId) {
    if (xrayTraceId.length() > TRACE_ID_LENGTH) {
      return TraceId.getInvalid();
    }

    // Check version trace id version
    if (!xrayTraceId.startsWith(TRACE_ID_VERSION)) {
      return TraceId.getInvalid();
    }

    // Check delimiters
    int firstDelimiter = xrayTraceId.indexOf(TRACE_ID_DELIMITER);
    // we don't allow the epoch part to be missing completely
    int secondDelimiter = xrayTraceId.indexOf(TRACE_ID_DELIMITER, firstDelimiter + 2);
    if (firstDelimiter != TRACE_ID_DELIMITER_INDEX_1
        || secondDelimiter == -1
        || secondDelimiter > TRACE_ID_DELIMITER_INDEX_2) {
      return TraceId.getInvalid();
    }

    String epochPart = xrayTraceId.substring(firstDelimiter + 1, secondDelimiter);
    String uniquePart = xrayTraceId.substring(secondDelimiter + 1, secondDelimiter + 25);

    // X-Ray trace id format is 1-{at most 8 digit hex}-{24 digit hex}
    // epoch part can have leading 0s truncated
    return epochPart + uniquePart;
  }

  private static String parseSpanId(String xrayParentId) {
    if (xrayParentId.length() != PARENT_ID_LENGTH) {
      return SpanId.getInvalid();
    }

    return xrayParentId;
  }

  @Nullable
  private static Boolean parseTraceFlag(String xraySampledFlag) {
    if (xraySampledFlag.length() != SAMPLED_FLAG_LENGTH) {
      // Returning null as there is no invalid trace flag defined.
      return null;
    }

    char flag = xraySampledFlag.charAt(0);
    if (flag == IS_SAMPLED) {
      return true;
    } else if (flag == NOT_SAMPLED) {
      return false;
    } else {
      return null;
    }
  }
}
