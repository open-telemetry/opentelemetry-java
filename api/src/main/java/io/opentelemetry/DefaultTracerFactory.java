package io.opentelemetry;

import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.spi.TracerFactory;

public class DefaultTracerFactory implements TracerFactory {

  private static final TracerFactory instance = new DefaultTracerFactory();

  public static TracerFactory getInstance() {
    return instance;
  }

  @Override
  public Tracer create() {
    return DefaultTracer.getInstance();
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    return create();
  }
}
