package io.opentelemetry.context;

import io.grpc.Context;
import io.opentelemetry.context.interceptor.ContextChangeInterceptors;
import io.opentelemetry.context.interceptor.DefaultContextChangeInterceptors;

// TODO: better name
public final class ContextManager {
  private static final ContextManager INSTANCE = new ContextManager();

  private final ContextChangeInterceptors contextChangeInterceptors =
      new DefaultContextChangeInterceptors();

  public static ContextManager getInstance() {
    return INSTANCE;
  }

  public ContextChangeInterceptors getContextChangeInterceptors() {
    return contextChangeInterceptors;
  }

  public Scope withScopedContext(Context context) {
    return new ContextInScope(context);
  }

  private final class ContextInScope implements Scope {
    private final Context context;
    private final Context previous;

    private ContextInScope(Context newContext) {
      Context currentContext = Context.current();
      Context interceptedNewContext =
          contextChangeInterceptors
              .getActiveContextChangeInterceptor()
              .interceptUpdate(currentContext, newContext);

      this.context = interceptedNewContext;
      this.previous = interceptedNewContext.attach();
    }

    @Override
    public void close() {
      Context interceptedPreviousContext =
          contextChangeInterceptors
              .getActiveContextChangeInterceptor()
              .interceptUpdate(context, previous);

      context.detach(interceptedPreviousContext);
    }
  }

  private ContextManager() {}
}
