package io.opentelemetry.context.interceptor;

public interface ContextChangeInterceptors {
  void addContextChangeInterceptor(ContextChangeInterceptor contextChangeInterceptor);

  ContextChangeInterceptor getActiveContextChangeInterceptor();
}
