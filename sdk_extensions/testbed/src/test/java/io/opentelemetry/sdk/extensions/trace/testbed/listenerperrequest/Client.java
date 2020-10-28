/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.listenerperrequest;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class Client {
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Tracer tracer;

  public Client(Tracer tracer) {
    this.tracer = tracer;
  }

  /** Async execution. */
  private Future<Object> execute(final Object message, final ResponseListener responseListener) {
    return executor.submit(
        () -> {
          // send via wire and get response
          Object response = message + ":response";
          responseListener.onResponse(response);
          return response;
        });
  }

  public Future<Object> send(final Object message) {
    Span span = tracer.spanBuilder("send").setSpanKind(Kind.CLIENT).startSpan();
    return execute(message, new ResponseListener(span));
  }
}
