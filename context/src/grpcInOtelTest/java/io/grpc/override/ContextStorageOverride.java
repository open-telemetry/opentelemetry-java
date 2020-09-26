/*
 * Copyright 2020, OpenTelemetry Authors
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

  @Override
  public Context doAttach(Context toAttach) {
    io.opentelemetry.context.Context otelContext = io.opentelemetry.context.Context.current();
    Context current = otelContext.getValue(GRPC_CONTEXT);

    if (current == toAttach) {
      return toAttach;
    }

    if (current == null) {
      current = Context.ROOT;
    }

    io.opentelemetry.context.Context newOtelContext =
        otelContext.withValues(GRPC_CONTEXT, toAttach);
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
    return io.opentelemetry.context.Context.current().getValue(GRPC_CONTEXT);
  }
}
