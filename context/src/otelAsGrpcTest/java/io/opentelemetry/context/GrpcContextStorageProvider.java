/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcContextStorageProvider implements ContextStorageProvider {

  private static final Logger log = Logger.getLogger(GrpcContextStorageProvider.class.getName());

  @Override
  public ContextStorage get() {
    return GrpcContextStorage.INSTANCE;
  }

  private enum GrpcContextStorage implements ContextStorage {
    INSTANCE;

    @Override
    public Scope attach(Context toAttach) {
      if (!(toAttach instanceof GrpcContextWrapper)) {
        log.log(
            Level.SEVERE,
            "Context not created by GrpcContextStorageProvider. This is not "
                + "allowed when using GrpcContextStorageProvider. Did you create this context "
                + "using Context.current()?");
        return Scope.noop();
      }

      io.grpc.Context grpcContextToAttach = ((GrpcContextWrapper) toAttach).grpcContext;

      io.grpc.Context currentGrpcContext = io.grpc.Context.current();

      if (grpcContextToAttach == currentGrpcContext) {
        return Scope.noop();
      }

      io.grpc.Context toRestore = grpcContextToAttach.attach();
      return () -> grpcContextToAttach.detach(toRestore);
    }

    @Override
    public Context current() {
      return new GrpcContextWrapper(io.grpc.Context.current());
    }

    @Override
    public <T> Context.Key<T> contextKey(String name) {
      return new GrpcContextKeyWrapper<>(io.grpc.Context.key(name));
    }
  }

  private static class GrpcContextWrapper implements Context {
    private final io.grpc.Context grpcContext;

    private GrpcContextWrapper(io.grpc.Context grpcContext) {
      this.grpcContext = grpcContext;
    }

    @Override
    public <V> V get(Context.Key<V> key) {
      return grpcKey(key).get(grpcContext);
    }

    @Override
    public <V> Context with(Context.Key<V> k1, V v1) {
      return new GrpcContextWrapper(grpcContext.withValue(grpcKey(k1), v1));
    }

    @Override
    public <V1, V2> Context with(Context.Key<V1> k1, V1 v1, Context.Key<V2> k2, V2 v2) {
      return new GrpcContextWrapper(grpcContext.withValues(grpcKey(k1), v1, grpcKey(k2), v2));
    }

    @Override
    public <V1, V2, V3> Context with(
        Context.Key<V1> k1, V1 v1, Context.Key<V2> k2, V2 v2, Context.Key<V3> k3, V3 v3) {
      return new GrpcContextWrapper(
          grpcContext.withValues(grpcKey(k1), v1, grpcKey(k2), v2, grpcKey(k3), v3));
    }

    @Override
    public <V1, V2, V3, V4> Context with(
        Context.Key<V1> k1,
        V1 v1,
        Context.Key<V2> k2,
        V2 v2,
        Context.Key<V3> k3,
        V3 v3,
        Context.Key<V4> k4,
        V4 v4) {
      return new GrpcContextWrapper(
          grpcContext.withValues(
              grpcKey(k1), v1, grpcKey(k2), v2, grpcKey(k3), v3, grpcKey(k4), v4));
    }
  }

  static class GrpcContextKeyWrapper<T> implements Context.Key<T> {

    private final io.grpc.Context.Key<T> grpcContextKey;

    private GrpcContextKeyWrapper(io.grpc.Context.Key<T> grpcContextKey) {
      this.grpcContextKey = grpcContextKey;
    }
  }

  static <T> io.grpc.Context.Key<T> grpcKey(Context.Key<T> key) {
    if (key instanceof GrpcContextKeyWrapper) {
      return ((GrpcContextKeyWrapper<T>) key).grpcContextKey;
    }
    log.log(
        Level.SEVERE,
        "ContextKey not created by GrpcContextStorageProvider, "
            + "this is not allowed when using GrpcContextStorageProvider. Did you create this "
            + "key using ContextKey.named()?");
    // This ephemereal key is invalid but the best we can fallback to.
    return io.grpc.Context.key("invalid-context-key-" + key);
  }
}
