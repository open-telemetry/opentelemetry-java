/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.concurrentcommonrequesthandler;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One instance per Client. Executed concurrently for all requests of one client. 'beforeRequest'
 * and 'afterResponse' are executed in different threads for one 'send'
 */
final class RequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

  static final String OPERATION_NAME = "send";

  private final Tracer tracer;

  private final SpanContext parentContext;

  public RequestHandler(Tracer tracer) {
    this(tracer, null);
  }

  public RequestHandler(Tracer tracer, SpanContext parentContext) {
    this.tracer = tracer;
    this.parentContext = parentContext;
  }

  public void beforeRequest(Object request, Context context) {
    logger.info("before send {}", request);

    // we cannot use active span because we don't know in which thread it is executed
    // and we cannot therefore activate span. thread can come from common thread pool.
    SpanBuilder spanBuilder =
        tracer
            .buildSpan(OPERATION_NAME)
            .ignoreActiveSpan()
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

    if (parentContext != null) {
      spanBuilder.asChildOf(parentContext);
    }

    context.put("span", spanBuilder.start());
  }

  public void afterResponse(Object response, Context context) {
    logger.info("after response {}", response);

    Object spanObject = context.get("span");
    if (spanObject instanceof Span) {
      Span span = (Span) spanObject;
      span.finish();
    }
  }
}
