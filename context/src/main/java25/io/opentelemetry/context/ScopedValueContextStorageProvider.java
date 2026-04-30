package io.opentelemetry.context;

public class ScopedValueContextStorageProvider implements ContextStorageProvider {
  @Override
  public ContextStorage get() {
    return ScopedValueContextStorage.getInstance();
  }
}
