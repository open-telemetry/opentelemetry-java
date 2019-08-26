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

package io.opentelemetry.sdk.contrib.trace.testbed.statelesscommonrequesthandler;

import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;

/**
 * One instance per Client. 'beforeRequest' and 'afterResponse' are executed in the same thread for
 * one 'send', but as these methods do not expose any object storing state, a thread-local field in
 * 'RequestHandler' itself is used to contain the Scope related to Span activation.
 */
@SuppressWarnings("MustBeClosedChecker")
final class RequestHandler {
  static final String OPERATION_NAME = "send";

  private final Tracer tracer;

  private static final ThreadLocal<Scope> tlsScope = new ThreadLocal<>();

  public RequestHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  /** beforeRequest handler....... */
  public void beforeRequest(Object request) {
    Span span = tracer.spanBuilder(OPERATION_NAME).setSpanKind(Kind.SERVER).startSpan();
    tlsScope.set(tracer.withSpan(span));
  }

  /** afterResponse handler....... */
  public void afterResponse(Object response) {
    // Finish the Span
    tracer.getCurrentSpan().end();

    // Deactivate the Span
    tlsScope.get().close();
  }
}
