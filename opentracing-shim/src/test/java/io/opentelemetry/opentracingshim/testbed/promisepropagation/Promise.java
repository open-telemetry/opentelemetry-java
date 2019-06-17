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

package io.opentelemetry.opentracingshim.testbed.promisepropagation;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.Collection;

final class Promise<T> {
  private final PromiseContext context;
  private final Tracer tracer;
  private final Span parentSpan;

  private final Collection<SuccessCallback<T>> successCallbacks = new ArrayList<>();
  private final Collection<ErrorCallback> errorCallbacks = new ArrayList<>();

  public Promise(PromiseContext context, Tracer tracer) {
    this.context = context;

    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;
    parentSpan = tracer.scopeManager().activeSpan();
  }

  public void onSuccess(SuccessCallback<T> successCallback) {
    successCallbacks.add(successCallback);
  }

  public void onError(ErrorCallback errorCallback) {
    errorCallbacks.add(errorCallback);
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  public void success(final T result) {
    for (final SuccessCallback<T> callback : successCallbacks) {
      context.submit(
          new Runnable() {
            @Override
            public void run() {
              Span childSpan =
                  tracer
                      .buildSpan("success")
                      .addReference(References.FOLLOWS_FROM, parentSpan.context())
                      .withTag(Tags.COMPONENT.getKey(), "success")
                      .start();
              try (Scope childScope = tracer.activateSpan(childSpan)) {
                callback.accept(result);
              } finally {
                childSpan.finish();
              }
              context.getPhaser().arriveAndAwaitAdvance(); // trace reported
            }
          });
    }
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  public void error(final Throwable error) {
    for (final ErrorCallback callback : errorCallbacks) {
      context.submit(
          new Runnable() {
            @Override
            public void run() {
              Span childSpan =
                  tracer
                      .buildSpan("error")
                      .addReference(References.FOLLOWS_FROM, parentSpan.context())
                      .withTag(Tags.COMPONENT.getKey(), "error")
                      .start();
              try (Scope childScope = tracer.activateSpan(childSpan)) {
                callback.accept(error);
              } finally {
                childSpan.finish();
              }
              context.getPhaser().arriveAndAwaitAdvance(); // trace reported
            }
          });
    }
  }

  public interface SuccessCallback<T> {
    void accept(T t);
  }

  public interface ErrorCallback {
    void accept(Throwable t);
  }
}
