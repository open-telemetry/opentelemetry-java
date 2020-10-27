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
      Context currentContext = ContextWrapper.fromBraveContext(currentBraveContext);
      if (currentContext == toAttach) {
        return Scope.noop();
      }

      TraceContext newBraveContext;
      if (toAttach instanceof ContextWrapper) {
        newBraveContext = ((ContextWrapper) toAttach).toBraveContext();
      } else {
        newBraveContext = currentBraveContext.toBuilder().addExtra(toAttach).build();
      }

      if (currentBraveContext == newBraveContext) {
        return Scope.noop();
      }
      CurrentTraceContext.Scope braveScope = currentTraceContext.newScope(newBraveContext);
      return braveScope::close;
    }

    @Override
    public Context current() {
      TraceContext currentBraveContext = Tracing.current().currentTraceContext().get();
      if (currentBraveContext == null) {
        return new ContextWrapper(null, DefaultContext.root());
      }
      return new ContextWrapper(
          currentBraveContext, ContextWrapper.fromBraveContext(currentBraveContext));
    }
  }

  // Need to wrap the Context because brave findExtra searches for perfect match of the class.
  static final class ContextWrapper implements Context {
    private final TraceContext baseBraveContext;
    private final DefaultContext context;

    ContextWrapper(TraceContext baseBraveContext, DefaultContext context) {
      this.baseBraveContext = baseBraveContext;
      this.context = context;
    }

    TraceContext toBraveContext() {
      if (fromBraveContext(baseBraveContext) == context) {
        return baseBraveContext;
      }
      return baseBraveContext.toBuilder().addExtra(context).build();
    }

    @Nullable
    @Override
    public <V> V get(ContextKey<V> key) {
      return context.get(key);
    }

    @Override
    public <V> Context with(ContextKey<V> k1, V v1) {
      return new ContextWrapper(baseBraveContext, context.with(k1, v1));
    }

    private static DefaultContext fromBraveContext(TraceContext braveContext) {
      List<Object> extra = braveContext.extra();
      for (int i = extra.size() - 1; i >= 0; i--) {
        Object nextExtra = extra.get(i);
        if (nextExtra.getClass() == DefaultContext.class) {
          return (DefaultContext) nextExtra;
        }
      }
      return DefaultContext.root();
    }
  }
}
