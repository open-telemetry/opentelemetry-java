/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import com.google.common.base.Joiner;
import io.opencensus.common.Duration;
import io.opencensus.exporter.metrics.util.IntervalMetricReader;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opentelemetry.opencensusshim.internal.metrics.MetricAdapter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Deprecated
public final class OpenTelemetryMetricsExporter extends MetricExporter {
  private static final Logger LOGGER =
      Logger.getLogger(OpenTelemetryMetricsExporter.class.getName());

  private static final String EXPORTER_NAME = "OpenTelemetryMetricExporter";

  private final IntervalMetricReader intervalMetricReader;
  private final io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter;
  // TODO - find this from OTel SDK.
  private final Resource resource = Resource.getDefault();

  public static OpenTelemetryMetricsExporter createAndRegister(
      io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter) {
    return new OpenTelemetryMetricsExporter(otelExporter, Duration.create(60, 0));
  }

  public static OpenTelemetryMetricsExporter createAndRegister(
      io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter, Duration exportInterval) {
    return new OpenTelemetryMetricsExporter(otelExporter, exportInterval);
  }

  private OpenTelemetryMetricsExporter(
      io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter, Duration exportInterval) {
    this.otelExporter = otelExporter;
    IntervalMetricReader.Options.Builder options = IntervalMetricReader.Options.builder();
    MetricReader reader =
        MetricReader.create(
            MetricReader.Options.builder()
                .setMetricProducerManager(Metrics.getExportComponent().getMetricProducerManager())
                .setSpanName(EXPORTER_NAME)
                .build());
    intervalMetricReader =
        IntervalMetricReader.create(
            this, reader, options.setExportInterval(exportInterval).build());
  }

  @Override
  public void export(Collection<Metric> metrics) {
    List<MetricData> metricData = new ArrayList<>();
    Set<MetricDescriptor.Type> unsupportedTypes = new HashSet<>();
    for (Metric metric : metrics) {
      metricData.add(MetricAdapter.convert(resource, metric));
    }
    if (!unsupportedTypes.isEmpty()) {
      LOGGER.warning(
          Joiner.on(",").join(unsupportedTypes)
              + " not supported by OpenCensus to OpenTelemetry migrator.");
    }
    if (!metricData.isEmpty()) {
      otelExporter.export(metricData);
    }
  }

  public void stop() {
    intervalMetricReader.stop();
  }
}
