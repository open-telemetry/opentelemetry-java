package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.sdk.internal.DynamicList;
import java.util.Collections;
import java.util.List;

public class MutableInstrumentationScopeMetricsMarshaler extends InstrumentationScopeMetricsMarshaler {
  private InstrumentationScopeMarshaler instrumentationScope;

  // TODO Asaf: Change to nullable DynamicList
  private List<Marshaler> metricMarshalers = Collections.emptyList();

  private String schemaUrl;
  private int size;

  public MutableInstrumentationScopeMetricsMarshaler() {
  }

  void set(
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
