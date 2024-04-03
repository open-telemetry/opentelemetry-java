/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConfigurableMetricReaderProvider;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SPI implementation for {@link PrometheusHttpServer}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class PrometheusMetricReaderProvider implements ConfigurableMetricReaderProvider {

  @Override
  public MetricReader createMetricReader(ConfigProperties config) {
    PrometheusHttpServerBuilder prometheusBuilder = PrometheusHttpServer.builder();

    Integer port = config.getInt("otel.exporter.prometheus.port");
    if (port != null) {
      prometheusBuilder.setPort(port);
    }
    String host = config.getString("otel.exporter.prometheus.host");
    if (host != null) {
      prometheusBuilder.setHost(host);
    }

    ExporterBuilderUtil.configureExporterMemoryMode(
        config, memoryMode -> setMemoryMode(prometheusBuilder, memoryMode));

    return prometheusBuilder.build();
  }

  /**
   * Calls {@code #setMemoryMode} on the {@link PrometheusHttpServerBuilder} with the {@code
   * memoryMode}.
   */
  public static void setMemoryMode(PrometheusHttpServerBuilder builder, MemoryMode memoryMode) {
    try {
      Method method = builder.getClass().getDeclaredMethod("setMemoryMode", MemoryMode.class);
      method.setAccessible(true);
      method.invoke(builder, memoryMode);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException("Error calling setMemoryMode.", e);
    }
  }

  @Override
  public String getName() {
    return "prometheus";
  }
}
