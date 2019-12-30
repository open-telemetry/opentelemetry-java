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
import io.opentelemetry.metrics.DefaultMeterFactoryProvider;
import io.opentelemetry.metrics.MeterFactory;
import io.opentelemetry.metrics.spi.MeterFactoryProvider;
import io.opentelemetry.trace.DefaultTracerFactoryProvider;
import io.opentelemetry.trace.TracerFactory;
import io.opentelemetry.trace.spi.TracerFactoryProvider;
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

  static TracerFactory makeSpiTracerFactory() {
    TracerFactoryProvider tracerFactoryProvider = loadSpi(TracerFactoryProvider.class);
    return tracerFactoryProvider != null
        ? tracerFactoryProvider.create()
        : DefaultTracerFactoryProvider.getInstance().create();
  }

  static MeterFactory makeSpiMeterFactory() {
    MeterFactoryProvider meterFactoryProvider = loadSpi(MeterFactoryProvider.class);
    return meterFactoryProvider != null
        ? meterFactoryProvider.create()
        : DefaultMeterFactoryProvider.getInstance().create();
  }

  static DistributedContextManager makeSpiContextManager() {
    DistributedContextManagerProvider contextManagerProvider =
        loadSpi(DistributedContextManagerProvider.class);
    return contextManagerProvider != null
        ? contextManagerProvider.create()
        : DefaultDistributedContextManager.getInstance();
  }
}
