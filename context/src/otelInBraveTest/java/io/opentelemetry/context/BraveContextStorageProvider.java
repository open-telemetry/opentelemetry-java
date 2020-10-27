/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import java.util.List;
import javax.annotation.Nullable;

public class BraveContextStorageProvider implements ContextStorageProvider {

  @Override
  public ContextStorage get() {
    return BraveContextStorage.INSTANCE;
  }

  @SuppressWarnings("ReferenceEquality")
  private enum BraveContextStorage implements ContextStorage {
    INSTANCE;

    @Override
    public Scope attach(Context toAttach) {
      CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
      TraceContext currentBraveContext = currentTraceContext.get();
      ContextWrapper currentContext = fromBraveContext(currentBraveContext);
      if (currentContext == ContextWrapper.ROOT
          && (toAttach == null || toAttach == Context.root())) {
        // It may be possible that in the current brave we have null, and asked to add root,
        // but the behavior of Current is to never return null, so it is fine to return noop.
        return Scope.noop();
      }

      TraceContext newBraveContext =
          currentBraveContext.toBuilder().addExtra(new ContextWrapper(toAttach)).build();
      CurrentTraceContext.Scope braveScope = currentTraceContext.newScope(newBraveContext);
      return braveScope::close;
    }

    @Override
    public Context current() {
      TraceContext currentBraveContext = Tracing.current().currentTraceContext().get();
      if (currentBraveContext == null) {
        return Context.root();
      }
      return fromBraveContext(currentBraveContext);
    }

    private static ContextWrapper fromBraveContext(TraceContext braveContext) {
      List<Object> extra = braveContext.extra();
      for (int i = extra.size() - 1; i >= 0; i--) {
        Object nextExtra = extra.get(i);
        if (nextExtra.getClass() == ContextWrapper.class) {
          return (ContextWrapper) nextExtra;
        }
      }
      return ContextWrapper.ROOT;
    }
  }

  // Need to wrap the Context because brave findExtra searches for perfect match of the class.
  static final class ContextWrapper implements Context {
    private final Context baseContext;
    private static final ContextWrapper ROOT = new ContextWrapper(Context.root());

    ContextWrapper(Context baseContext) {
      this.baseContext = baseContext;
    }

    @Nullable
    @Override
    public <V> V get(ContextKey<V> key) {
      return baseContext.get(key);
    }

    @Override
    public <V> Context with(ContextKey<V> k1, V v1) {
      return baseContext.with(k1, v1);
    }
  }
}
