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

package io.opentelemetry.context;

public class GrpcContextStorageProvider implements ContextStorageProvider {

  @Override
  public ContextStorage get() {
    return GrpcContextStorage.INSTANCE;
  }

  private enum GrpcContextStorage implements ContextStorage {
    INSTANCE;

    private static final io.grpc.Context.Key<Context> OTEL_CONTEXT =
        io.grpc.Context.key("otel-context");

    @Override
    public Scope attach(Context toAttach) {
      io.grpc.Context grpcContext = io.grpc.Context.current();
      Context current = OTEL_CONTEXT.get(grpcContext);

      if (current == toAttach) {
        return Scope.noop();
      }

      io.grpc.Context newGrpcContext = grpcContext.withValue(OTEL_CONTEXT, toAttach);
      io.grpc.Context toRestore = newGrpcContext.attach();

      return () -> newGrpcContext.detach(toRestore);
    }

    @Override
    public Context current() {
      return OTEL_CONTEXT.get();
    }
  }
}
