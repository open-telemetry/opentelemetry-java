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

package io.opentelemetry.sdk.contrib.trace.testbed.multiplecallbacks;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Client {
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Tracer tracer;

  public Client(Tracer tracer) {
    this.tracer = tracer;
  }

  public Future<Object> send(final Object message, final long milliseconds) {
    final Span parent = tracer.getCurrentSpan();

    return executor.submit(
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            Span span = tracer.spanBuilder("subtask").setParent(parent).startSpan();
            try (Scope subtaskScope = tracer.withSpan(span)) {
              // Simulate work.
              TestUtils.sleep(milliseconds);
            } finally {
              span.end();
            }

            return message + "::response";
          }
        });
  }
}
