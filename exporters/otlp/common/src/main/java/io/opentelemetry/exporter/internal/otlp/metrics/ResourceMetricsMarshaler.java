/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import java.io.IOException;
import java.util.List;

/**
 * A Marshaler of ResourceMetrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class ResourceMetricsMarshaler extends Marshaler {

  abstract protected ResourceMarshaler getResourceMarshaler();
  abstract protected String getSchemaUrl();
  abstract protected List<InstrumentationScopeMetricsMarshaler>
    getInstrumentationScopeMetricsMarshalers();

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceMetrics.RESOURCE, getResourceMarshaler());
    output.serializeRepeatedMessage(
        ResourceMetrics.SCOPE_METRICS, getInstrumentationScopeMetricsMarshalers());
    output.serializeString(ResourceMetrics.SCHEMA_URL, getSchemaUrl());
  }

  protected static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      String schemaUrl,
      List<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeStringUtf8(ResourceMetrics.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceMetrics.SCOPE_METRICS, instrumentationScopeMetricsMarshalers);
    return size;
  }
}
