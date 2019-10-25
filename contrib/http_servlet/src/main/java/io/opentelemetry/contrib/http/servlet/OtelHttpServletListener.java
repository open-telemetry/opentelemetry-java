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

package io.opentelemetry.contrib.http.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.errorprone.annotations.MustBeClosed;
import io.opentelemetry.context.Scope;
import io.opentelemetry.contrib.http.core.HttpRequestContext;
import io.opentelemetry.contrib.http.core.HttpServerHandler;
import io.opentelemetry.trace.Tracer;
import java.io.Closeable;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Implementation of {@link AsyncListener} which handles span completion for async requests. */
class OtelHttpServletListener implements Closeable, AsyncListener {

  private final Tracer tracer;
  private final HttpRequestContext context;
  private final HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>
      handler;

  OtelHttpServletListener(
      Tracer tracer,
      HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler,
      HttpRequestContext context) {
    checkNotNull(tracer, "tracer is required");
    checkNotNull(context, "context is required");
    checkNotNull(handler, "handler is required");
    this.tracer = tracer;
    this.context = context;
    this.handler = handler;
  }

  @Override
  public void onComplete(AsyncEvent event) {
    OtelHttpServletUtils.recordMessageSentEvent(
        handler, context, (HttpServletResponse) event.getSuppliedResponse());
    handler.handleEnd(
        context,
        (HttpServletRequest) event.getSuppliedRequest(),
        (HttpServletResponse) event.getSuppliedResponse(),
        null);
    close();
  }

  @Override
  public void onTimeout(AsyncEvent event) {
    handler.handleEnd(
        context,
        (HttpServletRequest) event.getSuppliedRequest(),
        (HttpServletResponse) event.getSuppliedResponse(),
        null);
  }

  @Override
  public void onError(AsyncEvent event) {
    handler.handleEnd(
        context,
        (HttpServletRequest) event.getSuppliedRequest(),
        (HttpServletResponse) event.getSuppliedResponse(),
        event.getThrowable());
  }

  @Override
  public void onStartAsync(AsyncEvent event) {
    AsyncContext eventAsyncContext = event.getAsyncContext();
    if (eventAsyncContext != null) {
      eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
    }
  }

  @Override
  public void close() {
    // NoOp
  }

  @MustBeClosed
  Scope withSpan() {
    return tracer.withSpan(handler.getSpanFromContext(context));
  }
}
