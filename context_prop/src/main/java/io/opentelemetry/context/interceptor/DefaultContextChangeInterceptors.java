package io.opentelemetry.context.interceptor;

import io.grpc.Context;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DefaultContextChangeInterceptors
    implements ContextChangeInterceptors, ContextChangeInterceptor {
  private final List<ContextChangeInterceptor> interceptors = new CopyOnWriteArrayList<>();

  @Override
  public void addContextChangeInterceptor(ContextChangeInterceptor contextChangeInterceptor) {
    interceptors.add(contextChangeInterceptor);
  }

  @Override
  public ContextChangeInterceptor getActiveContextChangeInterceptor() {
    return this;
  }

  @Override
  public Context interceptUpdate(Context currentContext, Context newContext) {
    try {
      Context updated = newContext;
      for (ContextChangeInterceptor interceptor : interceptors) {
        updated = interceptor.interceptUpdate(currentContext, updated);
      }
      return updated;
    } catch (Error e) {
      throw e;
    } catch (Throwable ignored) {
      // in case any interceptor throws just ignore the interception process
      return newContext;
    }
  }
}
