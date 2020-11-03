/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.grpc.override;

import io.grpc.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;
import java.util.logging.Level;
import java.util.logging.Logger;

// This exact package / class name indicates to gRPC to use this override.
public class ContextStorageOverride extends Context.Storage {

  private static final Logger log = Logger.getLogger(ContextStorageOverride.class.getName());

  private static final ContextKey<Context> GRPC_CONTEXT = ContextKey.named("grpc-context");
  private static final Context.Key<Scope> OTEL_SCOPE = Context.key("otel-scope");

  @SuppressWarnings("MustBeClosedChecker")
  @Override
  public Context doAttach(Context toAttach) {
    io.opentelemetry.context.Context otelContext = io.opentelemetry.context.Context.current();
    Context current = otelContext.get(GRPC_CONTEXT);

    if (current == toAttach) {
      return toAttach;
    }

    if (current == null) {
      current = Context.ROOT;
    }

    io.opentelemetry.context.Context newOtelContext = otelContext.with(GRPC_CONTEXT, toAttach);
    Scope scope = newOtelContext.makeCurrent();
    return current.withValue(OTEL_SCOPE, scope);
  }

  @Override
  public void detach(Context toDetach, Context toRestore) {
    if (current() != toDetach) {
      // Log a severe message instead of throwing an exception as the context to attach is assumed
      // to be the correct one and the unbalanced state represents a coding mistake in a lower
      // layer in the stack that cannot be recovered from here.
      log.log(
          Level.SEVERE,
          "Context was not attached when detaching",
          new Throwable().fillInStackTrace());
    }

    Scope otelScope = OTEL_SCOPE.get(toRestore);
    otelScope.close();
  }

  @Override
  public Context current() {
    return io.opentelemetry.context.Context.current().get(GRPC_CONTEXT);
  }
}
