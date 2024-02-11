package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;

import static java.util.Objects.requireNonNull;

public abstract class DelegatingMetricData implements MetricData {

    private final MetricData delegate;

    protected DelegatingMetricData(MetricData delegate) {
      this.delegate = requireNonNull(delegate, "delegate");
    }

    @Override
    public Resource getResource() {
      return delegate.getResource();
    }
    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
      return delegate.getInstrumentationScopeInfo();
    }

    @Override
    public String getName() {
      return delegate.getName();
    }

    @Override
    public String getDescription() {
      return delegate.getDescription();
    }

    @Override
    public String getUnit() {
      return delegate.getUnit();
    }

    @Override
    public MetricDataType getType() {
      return delegate.getType();
    }

    @Override
    public Data<?> getData() {
      return delegate.getData();
    }
}
