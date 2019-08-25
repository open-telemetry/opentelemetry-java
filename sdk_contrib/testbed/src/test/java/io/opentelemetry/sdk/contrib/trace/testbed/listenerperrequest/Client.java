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

package io.opentelemetry.sdk.contrib.trace.testbed.listenerperrequest;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class Client {
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Tracer tracer;

  public Client(Tracer tracer) {
    this.tracer = tracer;
  }

  /** Async execution. */
  private Future<Object> execute(final Object message, final ResponseListener responseListener) {
    return executor.submit(
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // send via wire and get response
            Object response = message + ":response";
            responseListener.onResponse(response);
            return response;
          }
        });
  }

  public Future<Object> send(final Object message) {
    Span span = tracer.spanBuilder("send").setSpanKind(Kind.CLIENT).startSpan();
    return execute(message, new ResponseListener(span));
  }
}
