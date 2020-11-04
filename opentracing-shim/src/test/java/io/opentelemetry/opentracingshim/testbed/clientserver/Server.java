/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.clientserver;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import java.util.concurrent.ArrayBlockingQueue;

final class Server extends Thread {

  private final ArrayBlockingQueue<Message> queue;
  private final Tracer tracer;

  public Server(ArrayBlockingQueue<Message> queue, Tracer tracer) {
    this.queue = queue;
    this.tracer = tracer;
  }

  private void process(Message message) {
    SpanContext context =
        tracer.extract(Builtin.TEXT_MAP_EXTRACT, new TextMapExtractAdapter(message));
    Span span =
        tracer
            .buildSpan("receive")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
            .withTag(Tags.COMPONENT.getKey(), "example-server")
            .asChildOf(context)
            .start();

    try (Scope scope = tracer.activateSpan(span)) {
      // Simulate work.
    } finally {
      span.finish();
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
