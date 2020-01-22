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
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
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

    try (Scope ignored = tracer.withSpan(span)) {
      OpenTelemetry.getPropagators()
          .getHttpTextFormat()
          .inject(
              Context.current(),
              message,
              new Setter<Message>() {
                @Override
                public void set(Message carrier, String key, String value) {
                  carrier.put(key, value);
                }
              });
      queue.put(message);
    } finally {
      span.end();
    }
  }
}
