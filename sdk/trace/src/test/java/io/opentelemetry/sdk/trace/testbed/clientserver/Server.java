/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.clientserver;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.concurrent.ArrayBlockingQueue;
import javax.annotation.Nullable;

final class Server extends Thread {

  private final ArrayBlockingQueue<Message> queue;
  private final Tracer tracer;

  public Server(ArrayBlockingQueue<Message> queue, Tracer tracer) {
    this.queue = queue;
    this.tracer = tracer;
  }

  private void process(Message message) {
    Context context =
        W3CTraceContextPropagator.getInstance()
            .extract(
                Context.current(),
                message,
                new TextMapGetter<Message>() {
                  @Override
                  public Iterable<String> keys(Message carrier) {
                    return carrier.keySet();
                  }

                  @Nullable
                  @Override
                  public String get(Message carrier, String key) {
                    return carrier.get(key);
                  }
                });
    Span span =
        tracer.spanBuilder("receive").setSpanKind(SpanKind.SERVER).setParent(context).startSpan();
    span.setAttribute("component", "example-server");

    try (Scope ignored = span.makeCurrent()) {
      // Simulate work.
      Span.current().addEvent("DoWork");
    } finally {
      span.end();
    }
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        process(queue.take());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }
}
