/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;

/**
 * A {@link MetricData} which delegates all methods to another {@link MetricData}. Extend this class
 * to modify {@link MetricData} that will be exported, for example by creating a delegating {@link
 * io.opentelemetry.sdk.metrics.export.MetricExporter} which wraps {@link MetricData} with a custom
 * implementation.
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
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return delegate.getInstrumentationLibraryInfo();
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
    if (o instanceof MetricDataImpl) {
      MetricDataImpl that = (MetricDataImpl) o;
      return getResource().equals(that.getResource())
          && getInstrumentationLibraryInfo().equals(that.getInstrumentationLibraryInfo())
          && getName().equals(that.getName())
          && getDescription().equals(that.getDescription())
          && getUnit().equals(that.getUnit())
          && getType().equals(that.getType())
          && getData().equals(that.getData());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int code = 1;
    code *= 1000003;
    code ^= getResource().hashCode();
    code *= 1000003;
    code ^= getInstrumentationLibraryInfo().hashCode();
    code *= 1000003;
    code ^= getName().hashCode();
    code *= 1000003;
    code ^= getDescription().hashCode();
    code *= 1000003;
    code ^= getUnit().hashCode();
    code *= 1000003;
    code ^= getType().hashCode();
    code *= 1000003;
    code ^= getData().hashCode();
    return code;
  }

  @Override
  public String toString() {
    return "DelegatingMetricData{"
        + "resource="
        + getResource()
        + ", "
        + "instrumentationLibraryInfo="
        + getInstrumentationLibraryInfo()
        + ", "
        + "name="
        + getName()
        + ", "
        + "description="
        + getDescription()
        + ", "
        + "unit="
        + getUnit()
        + ", "
        + "type="
        + getType()
        + ", "
        + "data="
        + getData()
        + "}";
  }
}
