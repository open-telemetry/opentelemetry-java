/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.clientserver;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator.Getter;
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
        OpenTelemetry.getGlobalPropagators()
            .getTextMapPropagator()
            .extract(
                Context.current(),
                message,
                new Getter<Message>() {
                  @Nullable
                  @Override
                  public String get(Message carrier, String key) {
                    return carrier.get(key);
                  }
                });
    Span span =
        tracer.spanBuilder("receive").setSpanKind(Kind.SERVER).setParent(context).startSpan();
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
