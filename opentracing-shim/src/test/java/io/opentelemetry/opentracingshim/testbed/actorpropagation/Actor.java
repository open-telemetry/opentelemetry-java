/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.actorpropagation;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

final class Actor implements AutoCloseable {
  private final ExecutorService executor;
  private final Tracer tracer;
  private final Phaser phaser;

  public Actor(Tracer tracer, Phaser phaser) {
    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;

    this.phaser = phaser;
    executor = Executors.newFixedThreadPool(2);
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  public Future<?> tell(final String message) {
    final Span parent = tracer.scopeManager().activeSpan();
    phaser.register();
    return executor.submit(
        () -> {
          Span child =
              tracer
                  .buildSpan("received")
                  .addReference(References.FOLLOWS_FROM, parent.context())
                  .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                  .start();
          try (Scope scope = tracer.activateSpan(child)) {
            phaser.arriveAndAwaitAdvance(); // child tracer started
            child.log("received " + message);
            phaser.arriveAndAwaitAdvance(); // assert size
          } finally {
            child.finish();
          }

          phaser.arriveAndAwaitAdvance(); // child tracer finished
          phaser.arriveAndAwaitAdvance(); // assert size
        });
  }

  public Future<String> ask(final String message) {
    final Span parent = tracer.scopeManager().activeSpan();
    phaser.register();
    Future<String> future =
        executor.submit(
            () -> {
              Span span =
                  tracer
                      .buildSpan("received")
                      .addReference(References.FOLLOWS_FROM, parent.context())
                      .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                      .start();
              try {
                phaser.arriveAndAwaitAdvance(); // child tracer started
                phaser.arriveAndAwaitAdvance(); // assert size
                return "received " + message;
              } finally {
                span.finish();

                phaser.arriveAndAwaitAdvance(); // child tracer finished
                phaser.arriveAndAwaitAdvance(); // assert size
              }
            });
    return future;
  }
}
