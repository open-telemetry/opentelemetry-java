package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;

public interface MetricFilter {

  InstrumentFilterResult filterInstrument(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      InstrumentType metricDataType,
      String unit);

  boolean allowInstrumentAttributes(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      InstrumentType metricDataType,
      String unit,
      Attributes attributes
  );

  enum InstrumentFilterResult {
    ALLOW_ALL_ATTRIBUTES,
    REJECT_ALL_ATTRIBUTES,
    ALLOW_SOME_ATTRIBUTES
  }
}
