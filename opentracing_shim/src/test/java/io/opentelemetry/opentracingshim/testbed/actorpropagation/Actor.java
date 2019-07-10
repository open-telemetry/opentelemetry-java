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

package io.opentelemetry.opentracingshim.testbed.actorpropagation;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.util.concurrent.Callable;
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
        new Runnable() {
          @Override
          public void run() {
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
          }
        });
  }

  public Future<String> ask(final String message) {
    final Span parent = tracer.scopeManager().activeSpan();
    phaser.register();
    Future<String> future =
        executor.submit(
            new Callable<String>() {
              @Override
              public String call() throws Exception {
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
              }
            });
    return future;
  }
}
