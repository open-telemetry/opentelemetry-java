/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ScopeMetrics;
import java.io.IOException;
import java.util.List;

public abstract class InstrumentationScopeMetricsMarshaler extends Marshaler {

  abstract protected InstrumentationScopeMarshaler getInstrumentationScope();

  abstract protected String getSchemaUrl();

  abstract protected List<Marshaler> getMetricMarshalers();

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ScopeMetrics.SCOPE, getInstrumentationScope());
    output.serializeRepeatedMessage(ScopeMetrics.METRICS, getMetricMarshalers());
    output.serializeString(ScopeMetrics.SCHEMA_URL, getSchemaUrl());
  }

  protected static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      String schemaUrl,
      List<Marshaler> metricMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeMetrics.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeStringUtf8(ScopeMetrics.SCHEMA_URL, schemaUrl);
    size += MarshalerUtil.sizeRepeatedMessage(ScopeMetrics.METRICS, metricMarshalers);
    return size;
  }
}
