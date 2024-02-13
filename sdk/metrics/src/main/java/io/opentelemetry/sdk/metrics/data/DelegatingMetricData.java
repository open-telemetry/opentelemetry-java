/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.sdk.metrics.data;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;

/**
 * A {@link MetricData} which delegates all methods to another {@link MetricData}. Extend this class to
 * modify the {@link MetricData} that will be exported.
 */
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

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricData) {
      MetricData that = (MetricData) o;
      return getResource().equals(that.getResource()) &&
          getInstrumentationScopeInfo().equals(that.getInstrumentationScopeInfo()) &&
          getName().equals(that.getName()) &&
          getDescription().equals(that.getDescription()) &&
          getUnit().equals(that.getUnit()) &&
          getType().equals(that.getType()) &&
          getData().equals(that.getData());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result *= 1000003;
    result ^= this.getResource().hashCode();
    result *= 1000003;
    result ^= this.getInstrumentationScopeInfo().hashCode();
    result *= 1000003;
    result ^= this.getName().hashCode();
    result *= 1000003;
    result ^= this.getDescription().hashCode();
    result *= 1000003;
    result ^= this.getUnit().hashCode();
    result *= 1000003;
    result ^= this.getType().hashCode();
    result *= 1000003;
    result ^= this.getData().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "DelegatingMetricData{" +
        "resource=" + getResource() + ", " +
        "instrumentationScopeInfo=" + getInstrumentationScopeInfo() + ", " +
        "name=" + getName() + ", " +
        "description=" + getDescription() + ", " +
        "unit=" + getUnit() + ", " +
        "type=" + getType() + ", " +
        "data=" + getData() +
        "}";
  }
}
