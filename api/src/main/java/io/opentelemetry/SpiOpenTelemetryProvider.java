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

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.DefaultCorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerProvider;
import io.opentelemetry.metrics.DefaultMeterRegistryProvider;
import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.metrics.spi.MeterRegistryProvider;
import io.opentelemetry.trace.DefaultTracerRegistryProvider;
import io.opentelemetry.trace.TracerRegistry;
import io.opentelemetry.trace.spi.TracerRegistryProvider;
import java.util.ServiceLoader;
import javax.annotation.Nullable;

/** Creates an OpenTelemetry instance via Java's built-in {@link ServiceLoader} SPI capabilities. */
public class SpiOpenTelemetryProvider {

  private SpiOpenTelemetryProvider() {}

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
      throw new IllegalStateException(
          String.format("Service provider %s not found", specifiedProvider));
    }
    return null;
  }

  static TracerRegistry makeSpiTracerRegistry() {
    TracerRegistryProvider tracerRegistryProvider = loadSpi(TracerRegistryProvider.class);
    return tracerRegistryProvider != null
        ? tracerRegistryProvider.create()
        : DefaultTracerRegistryProvider.getInstance().create();
  }

  static MeterRegistry makeSpiMeterRegistry() {
    MeterRegistryProvider meterRegistryProvider = loadSpi(MeterRegistryProvider.class);
    return meterRegistryProvider != null
        ? meterRegistryProvider.create()
        : DefaultMeterRegistryProvider.getInstance().create();
  }

  static CorrelationContextManager makeSpiContextManager() {
    CorrelationContextManagerProvider contextManagerProvider =
        loadSpi(CorrelationContextManagerProvider.class);
    return contextManagerProvider != null
        ? contextManagerProvider.create()
        : DefaultCorrelationContextManager.getInstance();
  }
}
