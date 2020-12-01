/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.clientserver;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.tag.Tags;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

final class Client {

  private final ArrayBlockingQueue<Message> queue;
  private final Tracer tracer;

  public Client(ArrayBlockingQueue<Message> queue, Tracer tracer) {
    this.queue = queue;
    this.tracer = tracer;
  }

  public void send(boolean convertKeysToUpperCase) throws InterruptedException {
    Message message = new Message();

    Span span =
        tracer
            .buildSpan("send")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
            .withTag(Tags.COMPONENT.getKey(), "example-client")
            .start();
    try (Scope scope = tracer.activateSpan(span)) {
      tracer.inject(span.context(), Builtin.TEXT_MAP_INJECT, new TextMapInjectAdapter(message));
      if (convertKeysToUpperCase) {
        Message newMessage = new Message();
        for (Map.Entry<String, String> entry : message.entrySet()) {
          newMessage.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        message = newMessage;
      }
      queue.put(message);
    } finally {
      span.finish();
    }
  }
}
