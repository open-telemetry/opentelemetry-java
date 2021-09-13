/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ResourceMarshaler;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import java.io.IOException;

final class ResourceMetricsMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrl;
  private final InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers;

  ResourceMetricsMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationLibraryMetricsMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationLibraryMetricsMarshalers = instrumentationLibraryMetricsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS, instrumentationLibraryMetricsMarshalers);
    output.serializeString(ResourceMetrics.SCHEMA_URL, schemaUrl);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS,
            instrumentationLibraryMetricsMarshalers);
    return size;
  }
}
