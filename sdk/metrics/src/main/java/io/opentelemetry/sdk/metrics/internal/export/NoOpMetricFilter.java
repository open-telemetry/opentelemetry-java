package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.MetricFilter;

public class NoOpMetricFilter implements MetricFilter {

  public final static MetricFilter INSTANCE = new NoOpMetricFilter();

  @Override
  public InstrumentFilterResult filterInstrument(InstrumentationScopeInfo instrumentationScopeInfo,
      String name, InstrumentType instrumentType, String unit) {
    return InstrumentFilterResult.ALLOW_ALL_ATTRIBUTES;
  }

  @Override
  public boolean allowInstrumentAttributes(InstrumentationScopeInfo instrumentationScopeInfo,
      String name, InstrumentType instrumentType, String unit, Attributes attributes) {
    return true;
  }
}
