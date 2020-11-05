/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.statelesscommonrequesthandler;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One instance per Client. 'beforeRequest' and 'afterResponse' are executed in the same thread for
 * one 'send', but as these methods do not expose any object storing state, a thread-local field in
 * 'RequestHandler' itself is used to contain the Scope related to Span activation.
 */
final class RequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

  static final String OPERATION_NAME = "send";

  private final Tracer tracer;

  private static final ThreadLocal<Scope> tlsScope = new ThreadLocal<>();

  public RequestHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  /** beforeRequest handler....... */
  public void beforeRequest(Object request) {
    logger.info("before send {}", request);

    Span span = tracer.buildSpan(OPERATION_NAME).start();
    tlsScope.set(tracer.activateSpan(span));
  }

  /** afterResponse handler....... */
  public void afterResponse(Object response) {
    logger.info("after response {}", response);

    // Finish the Span
    tracer.scopeManager().activeSpan().finish();

    // Deactivate the Span
    tlsScope.get().close();
  }
}
