/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry;

import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.distributedcontext.spi.DistributedContextManagerProvider;
import io.opentelemetry.metrics.DefaultMeter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.spi.MeterProvider;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.spi.TracerProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link Meter}
 * and {@link DistributedContextManager}.
 *
 * <p>The telemetry objects are resolved via {@link ServiceLoader} mechanism.
 *
 * @see TracerProvider
 * @see MeterProvider
 * @see DistributedContextManagerProvider
 */
@ThreadSafe
public final class OpenTelemetry {

  @Nullable private static volatile OpenTelemetry instance;

  private final DistributedContextManagerProvider contextManagerProvider;
  private final TracerProvider tracerProvider;
  private final MeterProvider meterProvider;

  /**
   * Returns an instance of {@link Tracer}. In a single deployment runtime a singleton is returned,
   * however in an application server with multiple deployments a different instance can be used per
   * deployment.
   *
   * @return registered tracer or default via {@link DefaultTracer#getInstance()}.
   * @throws IllegalStateException if a specified tracer (via system properties) could not be found.
   * @since 0.1.0
   */
  public static Tracer getTracer() {
    return getInstance().tracerProvider.get();
  }

  /**
   * Returns an instance {@link Meter}. In a single deployment runtime a singleton is returned,
   * however in an application server with multiple deployments a different instance can be used per
   * deployment.
   *
   * @return registered meter or default via {@link DefaultMeter#getInstance()}.
   * @throws IllegalStateException if a specified meter (via system properties) could not be found.
   * @since 0.1.0
   */
  public static Meter getMeter() {
    return getInstance().meterProvider.get();
  }

  /**
   * Returns an instance of {@link DistributedContextManager}. In a single deployment runtime a
   * singleton is returned, however in an application server with multiple deployments a different
   * instance can be used per deployment.
   *
   * @return registered manager or default via {@link
   *     DefaultDistributedContextManager#getInstance()}.
   * @throws IllegalStateException if a specified manager (via system properties) could not be
   *     found.
   * @since 0.1.0
   */
  public static DistributedContextManager getDistributedContextManager() {
    return getInstance().contextManagerProvider.get();
  }

  /**
   * Load provider class via {@link ServiceLoader}. A specific provider class can be requested via
   * setting a system property with FQCN.
   *
   * @param providerClass a provider class
   * @param classLoader class loader
   * @param <T> provider type
   * @return a provider or null if not found
   * @throws IllegalStateException if a specified provider is not found
   */
  @Nullable
  private static <T> T loadSpiAndCheckSpecified(
      Class<T> providerClass, final ClassLoader classLoader) {
    T provider = loadSpi(providerClass, classLoader);
    String specifiedProvider = System.getProperty(providerClass.getName());
    if (specifiedProvider != null
        && !specifiedProvider.equals(provider != null ? provider.getClass().getName() : null)) {
      throw new IllegalStateException(String.format("Tracer %s not found", specifiedProvider));
    }
    return provider;
  }

  /**
   * Load provider class via {@link ServiceLoader}.
   *
   * @param providerClass a provider class
   * @param classLoader class loader
   * @param <T> provider type
   */
  @Nullable
  private static <T> T loadSpi(Class<T> providerClass, final ClassLoader classLoader) {
    if (classLoader == null) {
      return null;
    }

    ClassLoader parentCl =
        AccessController.doPrivileged(
            new PrivilegedAction<ClassLoader>() {
              @Override
              public ClassLoader run() {
                return classLoader.getParent();
              }
            });
    // providers packaged in application runtimes have a higher priority
    // than ones packaged in the application archives
    T t = loadSpi(providerClass, parentCl);
    if (t != null) {
      return t;
    }

    String specifiedProvider = System.getProperty(providerClass.getName());
    ServiceLoader<T> providers = ServiceLoader.load(providerClass, classLoader);
    for (T provider : providers) {
      if (specifiedProvider == null || specifiedProvider.equals(provider.getClass().getName())) {
        return provider;
      }
    }
    return null;
  }

  /** Lazy loads an instance. */
  private static OpenTelemetry getInstance() {
    if (instance == null) {
      synchronized (OpenTelemetry.class) {
        if (instance == null) {
          instance = new OpenTelemetry();
        }
      }
    }
    return instance;
  }

  private OpenTelemetry() {
    ClassLoader cl =
        AccessController.doPrivileged(
            new PrivilegedAction<ClassLoader>() {
              @Override
              public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
              }
            });
    if (cl == null) {
      cl = OpenTelemetry.class.getClassLoader();
    }

    TracerProvider tracerProvider = loadSpiAndCheckSpecified(TracerProvider.class, cl);
    this.tracerProvider =
        tracerProvider != null
            ? tracerProvider
            : new TracerProvider() {
              @Override
              public Tracer get() {
                return DefaultTracer.getInstance();
              }
            };
    MeterProvider meterProvider = loadSpiAndCheckSpecified(MeterProvider.class, cl);
    this.meterProvider =
        meterProvider != null
            ? meterProvider
            : new MeterProvider() {
              @Override
              public Meter get() {
                return DefaultMeter.getInstance();
              }
            };
    DistributedContextManagerProvider contextManagerProvider =
        loadSpiAndCheckSpecified(DistributedContextManagerProvider.class, cl);
    this.contextManagerProvider =
        contextManagerProvider != null
            ? contextManagerProvider
            : new DistributedContextManagerProvider() {
              @Override
              public DistributedContextManager get() {
                return DefaultDistributedContextManager.getInstance();
              }
            };
  }

  // for testing
  static void reset() {
    instance = null;
  }
}
