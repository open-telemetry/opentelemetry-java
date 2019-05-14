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

import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.NoopMetrics;
import io.opentelemetry.spi.MeterProvider;
import io.opentelemetry.spi.TaggerProvider;
import io.opentelemetry.spi.TracerProvider;
import io.opentelemetry.tags.NoopTags;
import io.opentelemetry.tags.Tagger;
import io.opentelemetry.trace.NoopTrace;
import io.opentelemetry.trace.Tracer;
import java.util.ServiceLoader;
import javax.annotation.Nullable;

/**
 * This class provides a static global accessor for telemetry objects {@link Tracer}, {@link
 * io.opentelemetry.metrics.Meter} and {@link io.opentelemetry.tags.Tagger}.
 */
public final class OpenTelemetry {

  @Nullable private static volatile OpenTelemetry instance;

  private final Tracer tracer;
  private final Meter meter;
  private final Tagger tagger;

  /**
   * Returns an instance of a {@link Tracer}.
   *
   * @return registered tracer or noop via {@link NoopTrace#newNoopTracer()}.
   * @throws IllegalStateException if a specified tracer (via system properties) could not be found.
   */
  public static Tracer getTracer() {
    return getInstance().tracer;
  }

  /**
   * Returns an instance of a {@link Meter}.
   *
   * @return registered meter or noop via {@link NoopMetrics#newNoopMeter()}.
   * @throws IllegalStateException if a specified meter (via system properties) could not be found.
   */
  public static Meter getMeter() {
    return getInstance().meter;
  }

  /**
   * Returns an instance of a {@link Tagger}.
   *
   * @return registered meter or noop via {@link NoopTags#newNoopTagger()}.
   * @throws IllegalStateException if a specified meter (via system properties) could not be found.
   */
  public static Tagger getTagger() {
    return getInstance().tagger;
  }

  /**
   * Load provider class via {@link ServiceLoader}. A specific provider class can be requested via
   * setting a system property with FQCN.
   *
   * @param providerClass a provider class
   * @param <T> provider type
   * @return a provider or null if not found
   * @throws IllegalStateException if a specified provider is not found
   */
  @Nullable
  private static <T> T loadSpi(Class<T> providerClass) {
    String specifiedProvider = System.getProperty(providerClass.getName());
    ServiceLoader<T> providers = ServiceLoader.load(providerClass);
    for (T provider : providers) {
      if (specifiedProvider == null || specifiedProvider.equals(provider.getClass().getName())) {
        return provider;
      }
    }
    if (specifiedProvider != null) {
      throw new IllegalStateException(String.format("Tracer %s not found", specifiedProvider));
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
    TracerProvider tracerProvider = loadSpi(TracerProvider.class);
    tracer = tracerProvider != null ? tracerProvider.create() : NoopTrace.newNoopTracer();
    MeterProvider meterProvider = loadSpi(MeterProvider.class);
    meter = meterProvider != null ? meterProvider.create() : NoopMetrics.newNoopMeter();
    TaggerProvider taggerProvider = loadSpi(TaggerProvider.class);
    tagger = taggerProvider != null ? taggerProvider.create() : NoopTags.newNoopTagger();
  }

  // for testing
  static void reset() {
    instance = null;
  }
}
