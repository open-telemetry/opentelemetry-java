/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.multiplecallbacks;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final CountDownLatch parentDoneLatch;
  private final Tracer tracer;

  public Client(Tracer tracer, CountDownLatch parentDoneLatch) {
    this.tracer = tracer;
    this.parentDoneLatch = parentDoneLatch;
  }

  public Future<Object> send(final Object message) {
    final SpanContext parentSpanContext = tracer.activeSpan().context();

    return executor.submit(
        () -> {
          logger.info("Child thread with message '{}' started", message);

          Span span =
              tracer
                  .buildSpan("subtask")
                  .addReference(References.FOLLOWS_FROM, parentSpanContext)
                  .start();
          try (Scope subtaskScope = tracer.activateSpan(span)) {
            // Simulate work - make sure we finish *after* the parent Span.
            parentDoneLatch.await();
          } finally {
            span.finish();
          }

          logger.info("Child thread with message '{}' finished", message);
          return message + "::response";
        });
  }
}
