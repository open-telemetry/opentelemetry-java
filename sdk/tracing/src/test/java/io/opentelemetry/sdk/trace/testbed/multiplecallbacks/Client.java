/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.multiplecallbacks;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Client {
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final CountDownLatch parentDoneLatch;
  private final Tracer tracer;

  public Client(Tracer tracer, CountDownLatch parentDoneLatch) {
    this.tracer = tracer;
    this.parentDoneLatch = parentDoneLatch;
  }

  public Future<Object> send(final Object message) {
    final Context parent = Context.current();

    return executor.submit(
        () -> {
          Span span = tracer.spanBuilder("subtask").setParent(parent).startSpan();
          try (Scope subtaskScope = span.makeCurrent()) {
            // Simulate work - make sure we finish *after* the parent Span.
            parentDoneLatch.await();
          } finally {
            span.end();
          }

          return message + "::response";
        });
  }
}
