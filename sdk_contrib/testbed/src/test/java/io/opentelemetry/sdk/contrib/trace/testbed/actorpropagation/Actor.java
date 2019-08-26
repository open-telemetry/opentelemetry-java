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

package io.opentelemetry.sdk.contrib.trace.testbed.actorpropagation;

import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.util.concurrent.Callable;
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
    final Span parent = tracer.getCurrentSpan();
    phaser.register();
    return executor.submit(
        new Runnable() {
          @Override
          public void run() {
            Span child =
                tracer
                    .spanBuilder("received")
                    .setParent(parent)
                    .setSpanKind(Kind.CONSUMER)
                    .startSpan();
            try (Scope ignored = tracer.withSpan(child)) {
              phaser.arriveAndAwaitAdvance(); // child tracer started
              child.addEvent("received " + message);
              phaser.arriveAndAwaitAdvance(); // assert size
            } finally {
              child.end();
            }

            phaser.arriveAndAwaitAdvance(); // child tracer finished
            phaser.arriveAndAwaitAdvance(); // assert size
          }
        });
  }

  Future<String> ask(final String message) {
    final Span parent = tracer.getCurrentSpan();
    phaser.register();
    return executor.submit(
        new Callable<String>() {
          @Override
          public String call() {
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
          }
        });
  }
}
