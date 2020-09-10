package io.opentelemetry.context.interceptor;

import io.grpc.Context;

public interface ContextChangeInterceptor {
  Context interceptUpdate(Context currentContext, Context newContext);
}
