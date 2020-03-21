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

package io.opentelemetry.sdk.contrib.trace.testbed.clientserver;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat.Getter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;
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
        OpenTelemetry.getPropagators()
            .getHttpTextFormat()
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
    SpanContext spanContext = TracingContextUtils.getSpan(context).getContext();
    Span span =
        tracer.spanBuilder("receive").setSpanKind(Kind.SERVER).setParent(spanContext).startSpan();
    span.setAttribute("component", "example-server");

    try (Scope ignored = tracer.withSpan(span)) {
      // Simulate work.
      tracer.getCurrentSpan().addEvent("DoWork");
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
