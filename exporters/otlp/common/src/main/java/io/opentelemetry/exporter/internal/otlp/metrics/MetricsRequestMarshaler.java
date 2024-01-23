/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.collector.metrics.v1.internal.ExportMetricsServiceRequest;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * {@link Marshaler} to convert SDK {@link MetricData} to OTLP ExportMetricsServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class MetricsRequestMarshaler extends Marshaler {

  abstract List<ResourceMetricsMarshaler> getResourceMetricsMarshalers();

  protected MetricsRequestMarshaler() {
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportMetricsServiceRequest.RESOURCE_METRICS, getResourceMetricsMarshalers());
  }

  protected static int calculateSize(List<ResourceMetricsMarshaler> resourceMetricsMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExportMetricsServiceRequest.RESOURCE_METRICS, resourceMetricsMarshalers);
    return size;
  }
}
