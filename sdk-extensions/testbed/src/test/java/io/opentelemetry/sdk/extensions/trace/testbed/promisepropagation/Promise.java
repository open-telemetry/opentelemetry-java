/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.promisepropagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.Collection;

final class Promise<T> {
  private final PromiseContext context;
  private final Tracer tracer;
  private final Context parent;

  private final Collection<SuccessCallback<T>> successCallbacks = new ArrayList<>();
  private final Collection<ErrorCallback> errorCallbacks = new ArrayList<>();

  Promise(PromiseContext context, Tracer tracer) {
    this.context = context;

    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;
    parent = Context.current();
  }

  void onSuccess(SuccessCallback<T> successCallback) {
    successCallbacks.add(successCallback);
  }

  void onError(ErrorCallback errorCallback) {
    errorCallbacks.add(errorCallback);
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  void success(final T result) {
    for (final SuccessCallback<T> callback : successCallbacks) {
      context.submit(
          () -> {
            Span childSpan = tracer.spanBuilder("success").setParent(parent).startSpan();
            childSpan.setAttribute("component", "success");
            try (Scope ignored = childSpan.makeCurrent()) {
              callback.accept(result);
            } finally {
              childSpan.end();
            }
            context.getPhaser().arriveAndAwaitAdvance(); // trace reported
          });
    }
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  void error(final Throwable error) {
    for (final ErrorCallback callback : errorCallbacks) {
      context.submit(
          () -> {
            Span childSpan = tracer.spanBuilder("error").setParent(parent).startSpan();
            childSpan.setAttribute("component", "error");
            try (Scope ignored = childSpan.makeCurrent()) {
              callback.accept(error);
            } finally {
              childSpan.end();
            }
            context.getPhaser().arriveAndAwaitAdvance(); // trace reported
          });
    }
  }

  interface SuccessCallback<T> {
    void accept(T t);
  }

  interface ErrorCallback {
    void accept(Throwable t);
  }
}
