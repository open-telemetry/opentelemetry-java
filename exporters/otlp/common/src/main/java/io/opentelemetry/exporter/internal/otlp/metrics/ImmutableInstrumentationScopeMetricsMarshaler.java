package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import java.util.List;

public class ImmutableInstrumentationScopeMetricsMarshaler extends InstrumentationScopeMetricsMarshaler {
  private final InstrumentationScopeMarshaler instrumentationScope;
  private final List<Marshaler> metricMarshalers;
  private final String schemaUrl;
  private final int size;

  ImmutableInstrumentationScopeMetricsMarshaler(
      InstrumentationScopeMarshaler instrumentationScope,
      String schemaUrl,
      List<Marshaler> metricMarshalers) {
    this.instrumentationScope = instrumentationScope;
    this.schemaUrl = schemaUrl;
    this.metricMarshalers = metricMarshalers;
    this.size = calculateSize(instrumentationScope, schemaUrl, metricMarshalers);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  protected InstrumentationScopeMarshaler getInstrumentationScope() {
    return instrumentationScope;
  }

  @Override
  protected String getSchemaUrl() {
    return schemaUrl;
  }

  @Override
  protected List<Marshaler> getMetricMarshalers() {
    return metricMarshalers;
  }
}
