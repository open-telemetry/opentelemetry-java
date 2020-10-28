/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.actorpropagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

final class Actor implements AutoCloseable {
  private final ExecutorService executor;
  private final Tracer tracer;
  private final Phaser phaser;

  Actor(Tracer tracer, Phaser phaser) {
    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;

    this.phaser = phaser;
    executor = Executors.newFixedThreadPool(2);
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  Future<?> tell(final String message) {
    final Context parent = Context.current();
    phaser.register();
    return executor.submit(
        () -> {
          Span child =
              tracer
                  .spanBuilder("received")
                  .setParent(parent)
                  .setSpanKind(Kind.CONSUMER)
                  .startSpan();
          try (Scope ignored = child.makeCurrent()) {
            phaser.arriveAndAwaitAdvance(); // child tracer started
            child.addEvent("received " + message);
            phaser.arriveAndAwaitAdvance(); // assert size
          } finally {
            child.end();
          }

          phaser.arriveAndAwaitAdvance(); // child tracer finished
          phaser.arriveAndAwaitAdvance(); // assert size
        });
  }

  Future<String> ask(final String message) {
    final Context parent = Context.current();
    phaser.register();
    return executor.submit(
        () -> {
          Span span =
              tracer
                  .spanBuilder("received")
                  .setParent(parent)
                  .setSpanKind(Kind.CONSUMER)
                  .startSpan();
          try {
            phaser.arriveAndAwaitAdvance(); // child tracer started
            phaser.arriveAndAwaitAdvance(); // assert size
            return "received " + message;
          } finally {
            span.end();
            phaser.arriveAndAwaitAdvance(); // child tracer finished
            phaser.arriveAndAwaitAdvance(); // assert size
          }
        });
  }
}
