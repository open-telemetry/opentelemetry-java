/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.clientserver;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.concurrent.ArrayBlockingQueue;

final class Client {

  private final ArrayBlockingQueue<Message> queue;
  private final Tracer tracer;

  public Client(ArrayBlockingQueue<Message> queue, Tracer tracer) {
    this.queue = queue;
    this.tracer = tracer;
  }

  public void send() throws InterruptedException {
    Message message = new Message();

    Span span = tracer.spanBuilder("send").setSpanKind(Kind.CLIENT).startSpan();
    span.setAttribute("component", "example-client");

    try (Scope ignored = span.makeCurrent()) {
      GlobalOpenTelemetry.getPropagators()
          .getTextMapPropagator()
          .inject(Context.current(), message, Message::put);
      queue.put(message);
    } finally {
      span.end();
    }
  }
}
