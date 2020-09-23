package io.opentelemetry.context;

public class GrpcContextStorageProvider extends io.grpc.Context.Storage implements ContextStorageProvider {

  @Override
  public ContextStorage get() {
    return GrpcContextStorage.INSTANCE;
  }

  @Override
  public io.grpc.Context doAttach(io.grpc.Context toAttach) {

  }

  @Override
  public void detach(io.grpc.Context toDetach, io.grpc.Context toRestore) {

  }

  @Override
  public io.grpc.Context current() {
    return null;
  }

  private enum GrpcContextStorage implements ContextStorage {
    INSTANCE;

    private static final io.grpc.Context.Key<Context> OTEL_CONTEXT = io.grpc.Context.key("otel-context");

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
