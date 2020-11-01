/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GrpcContextStorageProvider implements ContextStorageProvider {
  private static final io.grpc.Context.Key<Context> OTEL_CONTEXT =
      io.grpc.Context.keyWithDefault("otel-context", Context.root());

  @Override
  public ContextStorage get() {
    return GrpcContextStorage.INSTANCE;
  }

  private enum GrpcContextStorage implements ContextStorage {
    INSTANCE;

    private static final AtomicReference<Consumer<Context>> onAttachConsumer =
        new AtomicReference<>();

    @Override
    public Scope attach(Context toAttach) {
      io.grpc.Context grpcContext = io.grpc.Context.current();
      Context current = OTEL_CONTEXT.get(grpcContext);

      if (current == toAttach) {
        return Scope.noop();
      }

      io.grpc.Context newGrpcContext;
      if (toAttach instanceof GrpcContextWrapper) {
        // This was already constructed with an embedded grpc Context.
        newGrpcContext = ((GrpcContextWrapper) toAttach).toGrpcContext();
      } else {
        newGrpcContext = grpcContext.withValue(OTEL_CONTEXT, toAttach);
      }

      onAttach(newGrpcContext);
      io.grpc.Context toRestore = newGrpcContext.attach();
      return () -> {
        onAttach(toRestore);
        newGrpcContext.detach(toRestore);
      };
    }

    @Override
    public Context current() {
      // We return an object that embeds both the
      io.grpc.Context grpcContext = io.grpc.Context.current();
      return GrpcContextWrapper.wrapperFromGrpc(grpcContext);
    }

    @Override
    public void onAttach(Consumer<Context> contextConsumer) {
      onAttachConsumer.updateAndGet(
          existing -> existing != null ? existing.andThen(contextConsumer) : contextConsumer);
    }

    private static void onAttach(io.grpc.Context toAttach) {
      Consumer<Context> contextConsumer = onAttachConsumer.get();
      if (contextConsumer != null) {
        contextConsumer.accept(GrpcContextWrapper.wrapperFromGrpc(toAttach));
      }
    }
  }

  private static class GrpcContextWrapper implements Context {
    // If otel context changes the grpc Context may be out of sync.
    // There are 2 options here: 1. always update the grpc Context, 2. update only when needed.
    // Currently the second one is implemented.
    private final io.grpc.Context baseGrpcContext;
    private final Context context;

    private GrpcContextWrapper(io.grpc.Context grpcContext, Context context) {
      this.baseGrpcContext = grpcContext;
      this.context = context;
    }

    private static GrpcContextWrapper wrapperFromGrpc(io.grpc.Context grpcContext) {
      return new GrpcContextWrapper(grpcContext, OTEL_CONTEXT.get(grpcContext));
    }

    private io.grpc.Context toGrpcContext() {
      if (OTEL_CONTEXT.get(baseGrpcContext) == context) {
        // No changes to the wrapper
        return baseGrpcContext;
      }
      return baseGrpcContext.withValue(OTEL_CONTEXT, context);
    }

    @Override
    public <V> V get(ContextKey<V> key) {
      return context.get(key);
    }

    @Override
    public <V> Context with(ContextKey<V> k1, V v1) {
      return new GrpcContextWrapper(baseGrpcContext, context.with(k1, v1));
    }
  }
}
