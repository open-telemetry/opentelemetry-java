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
