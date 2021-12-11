/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ServiceLoader;

final class MeterProviderConfiguration {

  @SuppressWarnings("deprecation") // Allow call to deprecated GlobalMeterProvider until removed
  static MeterProvider configureMeterProvider(
      Resource resource, ConfigProperties config, ClassLoader serviceClassLoader) {
    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder().setResource(resource);

    // Configure default exemplar filters.
    String exemplarFilter = config.getString("otel.metrics.exemplar.filter");
    if (exemplarFilter == null) {
      exemplarFilter = "with_sampled_trace";
    }
    switch (exemplarFilter) {
      case "none":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.neverSample());
        break;
      case "all":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.alwaysSample());
        break;
      case "with_sampled_trace":
      default:
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.sampleWithTraces());
        break;
    }

    for (SdkMeterProviderConfigurer configurer :
        ServiceLoader.load(SdkMeterProviderConfigurer.class, serviceClassLoader)) {
      configurer.configure(meterProviderBuilder, config);
    }

    String exporterName = config.getString("otel.metrics.exporter");
    if (exporterName == null || exporterName.equals("none")) {
      // In the event no exporters are configured set a noop exporter
      return MeterProvider.noop();
    }
    MetricExporterConfiguration.configureExporter(
        exporterName, config, serviceClassLoader, meterProviderBuilder);

    SdkMeterProvider meterProvider = meterProviderBuilder.buildAndRegisterGlobal();

    // Make sure metrics shut down when JVM shuts down.
    Runtime.getRuntime().addShutdownHook(new Thread(meterProvider::close));

    return meterProvider;
  }

  private MeterProviderConfiguration() {}
}
