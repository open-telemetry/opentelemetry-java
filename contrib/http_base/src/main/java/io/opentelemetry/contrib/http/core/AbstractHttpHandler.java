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

package io.opentelemetry.contrib.http.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Span;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Base class for handling request on http client and server. */
abstract class AbstractHttpHandler<Q, P> {

  /** The {@link HttpExtractor} used to extract information from request/response. */
  @VisibleForTesting final HttpExtractor<Q, P> extractor;

  @VisibleForTesting final StatusCodeConverter statusConverter;
  private final Meter meter;
  private final DoubleMeasure measureDuration;
  private final LongMeasure measureSentMessageSize;
  private final LongMeasure measureReceivedMessageSize;

  /** Constructor to allow access from same package subclasses only. */
  AbstractHttpHandler(
      HttpExtractor<Q, P> extractor, StatusCodeConverter statusConverter, Meter meter) {
    checkNotNull(extractor, "extractor is required");
    this.extractor = extractor;
    if (statusConverter == null) {
      this.statusConverter = new StatusCodeConverter();
    } else {
      this.statusConverter = statusConverter;
    }
    if (meter == null) {
      this.meter = OpenTelemetry.getMeterFactory().get(INSTRUMENTATION_LIB_ID);
    } else {
      this.meter = meter;
    }
    this.measureDuration = new TemporaryMeasureDouble();
    this.measureSentMessageSize = new TemporaryMeasureLong();
    this.measureReceivedMessageSize = new TemporaryMeasureLong();
    //    this.measureDuration =
    // this.meter.measureDoubleBuilder(HttpTraceConstants.MEASURE_DURATION)
    //        .setUnit("s")
    //        .build();
    //    this.measureMessageSize = this.meter
    //        .measureLongBuilder(HttpTraceConstants.MEASURE_RESP_SIZE)
    //        .setUnit("B")
    //        .build();
    //    this.measureMessageSize = this.meter
    //        .measureLongBuilder(HttpTraceConstants.MEASURE_REQ_SIZE)
    //        .setUnit("B")
    //        .build();
  }

  /**
   * A convenience to record a {@link Event} with given parameters.
   *
   * @param span the span which this {@code Event} will be added to.
   * @param type the type of event.
   * @param uncompressedMessageSize size of the message before compressed (optional).
   * @param compressedMessageSize size of the message after compressed (optional).
   */
  static void recordMessageEvent(
      Span span,
      long id,
      AttributeValue type,
      long uncompressedMessageSize,
      long compressedMessageSize) {
    Map<String, AttributeValue> attributes = new HashMap<>();
    attributes.put(HttpTraceConstants.MSG_EVENT_ATTR_TYPE, type);
    attributes.put(HttpTraceConstants.MSG_EVENT_ATTR_ID, AttributeValue.longAttributeValue(id));
    if (compressedMessageSize > 0L) {
      attributes.put(
          HttpTraceConstants.MSG_EVENT_ATTR_COMPRESSED,
          AttributeValue.longAttributeValue(compressedMessageSize));
    }
    if (uncompressedMessageSize > 0L) {
      attributes.put(
          HttpTraceConstants.MSG_EVENT_ATTR_UNCOMPRESSED,
          AttributeValue.longAttributeValue(uncompressedMessageSize));
    }
    span.addEvent(HttpTraceConstants.MSG_EVENT_NAME, attributes);
  }

  private static void putAttributeIfNotEmptyOrNull(Span span, String key, @Nullable String value) {
    if (value != null && !value.isEmpty()) {
      span.setAttribute(key, AttributeValue.stringAttributeValue(value));
    }
  }

  /**
   * Instrument an HTTP span after a message is sent. Typically called for every chunk of request or
   * response is sent.
   *
   * @param context request specific {@link HttpRequestContext}
   * @param bytes bytes sent.
   */
  public final void handleMessageSent(HttpRequestContext context, long bytes) {
    checkNotNull(context, "context is required");
    context.sentMessageSize.addAndGet(bytes);
    recordMessageEvent(
        context.span,
        context.sentSeqId.addAndGet(1L),
        HttpTraceConstants.MSG_EVENT_ATTR_SENT,
        bytes,
        0L);
  }

  /**
   * Instrument an HTTP span after a message is received. Typically called for every chunk of
   * request or response is received.
   *
   * @param context request specific {@link HttpRequestContext}
   * @param bytes bytes received.
   */
  public final void handleMessageReceived(HttpRequestContext context, long bytes) {
    checkNotNull(context, "context is required");
    context.receiveMessageSize.addAndGet(bytes);
    recordMessageEvent(
        context.span,
        context.receviedSeqId.addAndGet(1L),
        HttpTraceConstants.MSG_EVENT_ATTR_RECEIVED,
        bytes,
        0L);
  }

  void spanEnd(Span span, int httpStatus, @Nullable Throwable error) {
    span.setAttribute(
        HttpTraceConstants.HTTP_STATUS_CODE, AttributeValue.longAttributeValue(httpStatus));
    if (error != null) {
      String message = extractErrorMessage(error);
      span.setAttribute(
          HttpTraceConstants.HTTP_STATUS_TEXT, AttributeValue.stringAttributeValue(message));
      getLogger().log(Level.FINE, message, error);
    }
    span.setStatus(statusConverter.convert(httpStatus));
    span.end();
  }

  final String getSpanName(Q request) {
    String spanName = extractor.getRoute(request);
    if (spanName == null) {
      spanName = "/";
    }
    if (!spanName.startsWith("/")) {
      spanName = "/" + spanName;
    }
    return spanName;
  }

  final void addSpanRequestAttributes(Span span, Q request) {
    span.setAttribute(HttpTraceConstants.COMPONENT, HttpTraceConstants.COMPONENT_ATTR_VALUE);
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceConstants.HTTP_METHOD, extractor.getMethod(request));
    putAttributeIfNotEmptyOrNull(span, HttpTraceConstants.HTTP_ROUTE, extractor.getRoute(request));
    putAttributeIfNotEmptyOrNull(span, HttpTraceConstants.HTTP_URL, extractor.getUrl(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceConstants.HTTP_USERAGENT, extractor.getUserAgent(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceConstants.HTTP_FLAVOR, extractor.getHttpFlavor(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceConstants.HTTP_CLIENTIP, extractor.getClientIp(request));
  }

  String extractErrorMessage(Throwable error) {
    String message = error.getMessage();
    if (message == null) {
      message = error.getClass().getSimpleName();
    }
    return message;
  }

  protected void recordMeasurements(HttpRequestContext context, int httpCode) {
    //    BatchRecorder recorder = meter.newMeasureBatchRecorder();
    try {
      meter.newMeasureBatchRecorder();
      meter.longCounterBuilder(HttpTraceConstants.MEASURE_COUNT + httpCode).build();
    } catch (UnsupportedOperationException ignore) {
      // NoOp
    }
    BatchRecorder recorder = new TemporaryMeasureBatchRecorder();
    //    recorder.setDistributedContext(context.distContext);
    recorder.put(measureDuration, (System.nanoTime() - context.requestStartTime) / 1000000000.0);
    recorder.put(measureReceivedMessageSize, context.receiveMessageSize.longValue());
    recorder.put(measureSentMessageSize, context.sentMessageSize.longValue());
    recorder.record();
  }

  abstract Logger getLogger();

  /**
   * Retrieves {@link Span} from the {@link HttpRequestContext}.
   *
   * @param context request specific {@link HttpRequestContext}
   * @return {@link Span} associated with the request.
   */
  public Span getSpanFromContext(HttpRequestContext context) {
    checkNotNull(context, "context is required");
    return context.span;
  }

  HttpRequestContext getNewContext(Span span, DistributedContext distributedContext) {
    return new HttpRequestContext(span, distributedContext);
  }
}
