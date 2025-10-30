/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Method;

/**
 * Incubating utilities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class IncubatingUtil {
  private IncubatingUtil() {}

  @SuppressWarnings("unchecked")
  public static <T> T incubatingApiIfAvailable(T stableApi, String incubatingClassName) {
    try {
      Class<?> incubatingClass = Class.forName(incubatingClassName);
      Method getInstance = incubatingClass.getDeclaredMethod("getNoop");
      return (T) getInstance.invoke(null);
    } catch (Exception e) {
      return stableApi;
    }
  }

  public static OpenTelemetry obfuscatedOpenTelemetry(OpenTelemetry openTelemetry) {
    try {
      Class<?> extendedClass =
          Class.forName("io.opentelemetry.api.incubator.ExtendedOpenTelemetry");
      if (extendedClass.isInstance(openTelemetry)) {
        Class<?> incubatingClass =
            Class.forName(
                "io.opentelemetry.api.incubator.internal.ObfuscatedExtendedOpenTelemetry");
        return (OpenTelemetry)
            incubatingClass
                .getDeclaredConstructor(extendedClass)
                .newInstance(extendedClass.cast(openTelemetry));
      }
    } catch (Exception e) {
      // incubator not available
    }
    return new ObfuscatedOpenTelemetry(openTelemetry);
  }

  /**
   * Static global instances are obfuscated when they are returned from the API to prevent users
   * from casting them to their SDK-specific implementation. For example, we do not want users to
   * use patterns like {@code (OpenTelemetrySdk) GlobalOpenTelemetry.get()}.
   */
  @ThreadSafe
  static class ObfuscatedOpenTelemetry implements OpenTelemetry {

    private final OpenTelemetry delegate;

    ObfuscatedOpenTelemetry(OpenTelemetry delegate) {
      this.delegate = delegate;
    }

    @Override
    public TracerProvider getTracerProvider() {
      return delegate.getTracerProvider();
    }

    @Override
    public MeterProvider getMeterProvider() {
      return delegate.getMeterProvider();
    }

    @Override
    public LoggerProvider getLogsBridge() {
      return delegate.getLogsBridge();
    }

    @Override
    public ContextPropagators getPropagators() {
      return delegate.getPropagators();
    }

    @Override
    public TracerBuilder tracerBuilder(String instrumentationScopeName) {
      return delegate.tracerBuilder(instrumentationScopeName);
    }
  }
}
