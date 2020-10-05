/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.statelesscommonrequesthandler;

import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;

/**
 * One instance per Client. 'beforeRequest' and 'afterResponse' are executed in the same thread for
 * one 'send', but as these methods do not expose any object storing state, a thread-local field in
 * 'RequestHandler' itself is used to contain the Scope related to Span activation.
 */
@SuppressWarnings("MustBeClosedChecker")
final class RequestHandler {
  static final String OPERATION_NAME = "send";

  private final Tracer tracer;

  private static final ThreadLocal<Scope> tlsScope = new ThreadLocal<>();

  public RequestHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  /** beforeRequest handler....... */
  public void beforeRequest(Object request) {
    Span span = tracer.spanBuilder(OPERATION_NAME).setSpanKind(Kind.SERVER).startSpan();
    tlsScope.set(tracer.withSpan(span));
  }

  /** afterResponse handler....... */
  public void afterResponse(Object response) {
    // Finish the Span
    tracer.getCurrentSpan().end();

    // Deactivate the Span
    tlsScope.get().close();
  }
}
