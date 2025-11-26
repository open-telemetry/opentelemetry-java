/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;

public class MetricExporterComponentProvider implements ComponentProvider {
  @Override
  public Class<MetricExporter> getType() {
    return MetricExporter.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public MetricExporter create(DeclarativeConfigProperties config) {
    return new TestMetricExporter(config);
  }

  public static class TestMetricExporter implements MetricExporter {

    public final DeclarativeConfigProperties config;

    private TestMetricExporter(DeclarativeConfigProperties config) {
      this.config = config;
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
      return AggregationTemporalitySelector.alwaysCumulative()
          .getAggregationTemporality(instrumentType);
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
  }
}
