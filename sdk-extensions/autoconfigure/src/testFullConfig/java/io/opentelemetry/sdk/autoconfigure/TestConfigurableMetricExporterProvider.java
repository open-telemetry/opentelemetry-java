/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;

public class TestConfigurableMetricExporterProvider implements ConfigurableMetricExporterProvider {

  @Override
  public MetricExporter createExporter(ConfigProperties config) {
    return new TestMetricExporter(config);
  }

  @Override
  public String getName() {
    return "testExporter";
  }

  public static class TestMetricExporter implements MetricExporter {
    private final ConfigProperties config;

    public TestMetricExporter(ConfigProperties config) {
      this.config = config;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }

    public ConfigProperties getConfig() {
      return config;
    }
  }
}
