/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;

public final class JfrContextStorageWrapper implements ContextStorage {

  private final ContextStorage wrapped;

  public JfrContextStorageWrapper(ContextStorage wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public Scope attach(Context toAttach) {
    Scope scope = wrapped.attach(toAttach);
    ScopeEvent event = new ScopeEvent(Span.fromContext(toAttach).getSpanContext());
    event.begin();
    return () -> {
      if (event.shouldCommit()) {
        event.commit();
      }
      scope.close();
    };
  }

  @Override
  public Context current() {
    return wrapped.current();
  }
}
