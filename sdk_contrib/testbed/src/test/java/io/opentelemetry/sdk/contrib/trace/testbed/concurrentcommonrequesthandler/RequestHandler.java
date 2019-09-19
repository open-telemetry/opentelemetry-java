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

package io.opentelemetry.sdk.contrib.trace.testbed.concurrentcommonrequesthandler;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;

/**
 * One instance per Client. Executed concurrently for all requests of one client. 'beforeRequest'
 * and 'afterResponse' are executed in different threads for one 'send'
 */
final class RequestHandler {
  static final String OPERATION_NAME = "send";

  private final Tracer tracer;

  private final SpanContext parentContext;

  public RequestHandler(Tracer tracer) {
    this(tracer, null);
  }

  public RequestHandler(Tracer tracer, SpanContext parentContext) {
    this.tracer = tracer;
    this.parentContext = parentContext;
  }

  public void beforeRequest(Object request, Context context) {
    // we cannot use active span because we don't know in which thread it is executed
    // and we cannot therefore activate span. thread can come from common thread pool.
    Span.Builder spanBuilder =
        tracer.spanBuilder(OPERATION_NAME).setNoParent().setSpanKind(Kind.CLIENT);

    if (parentContext != null) {
      spanBuilder.setParent(parentContext);
    }

    context.put("span", spanBuilder.startSpan());
  }

  public void afterResponse(Object response, Context context) {
    Object spanObject = context.get("span");
    if (spanObject instanceof Span) {
      Span span = (Span) spanObject;
      span.end();
    }
  }
}
