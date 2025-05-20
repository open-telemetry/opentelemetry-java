/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;

/**
 * A {@link MetricData} which delegates all methods to another {@link MetricData}. Extend this class
 * to modify the {@link MetricData} that will be exported, for example by creating a delegating
 * {@link io.opentelemetry.sdk.metrics.export.MetricExporter} which wraps {@link MetricData} with a
 * custom implementation.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // class MetricDataWithCustomDescription extends DelegatingMetricData {
 * //
 * //   private final String description;
 * //
 * //   MetricDataWithCustomDescription(MetricData delegate) {
 * //     super(delegate);
 * //     this.description = delegate.getDescription() + " (custom)";
 * //   }
 * //
 * //   @Override
 * //   public String getDescription() {
 * //     return description;
 * //   }
 * // }
 * }</pre>
 *
 * @since 1.50.0
 */
public abstract class DelegatingMetricData implements MetricData {

  private final MetricData delegate;

  protected DelegatingMetricData(MetricData delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  /**
   * Returns the resource associated with this metric data.
   *
   * @return the {@link Resource} instance.
   */
  @Override
  public Resource getResource() {
    return delegate.getResource();
  }

  /**
   * Returns the instrumentation library information associated with this metric data.
   *
   * @return the {@link InstrumentationScopeInfo} instance.
   */
  @Override
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return delegate.getInstrumentationScopeInfo();
  }

  /**
   * Returns the name of the metric.
   *
   * @return the name of the metric.
   */
  @Override
  public String getName() {
    return delegate.getName();
  }

  /**
   * Returns the description of the metric.
   *
   * @return the description of the metric.
   */
  @Override
  public String getDescription() {
    return delegate.getDescription();
  }

  /**
   * Returns the unit of the metric.
   *
   * @return the unit of the metric.
   */
  @Override
  public String getUnit() {
    return delegate.getUnit();
  }

  /**
   * Returns the type of the metric.
   *
   * @return the type of the metric.
   */
  @Override
  public MetricDataType getType() {
    return delegate.getType();
  }

  /**
   * Returns the data of the metric.
   *
   * @return the data of the metric.
   */
  @Override
  public Data<?> getData() {
    return delegate.getData();
  }

  /**
   * Returns a boolean indicating whether the delegate {@link MetricData} is equal to this {@code
   * MetricData}.
   *
   * @param o the object to compare to.
   * @return a boolean indicating whether the delegate {@link MetricData} is equal to this {@code
   *     MetricData}.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricData) {
      MetricData that = (MetricData) o;
      return getResource().equals(that.getResource())
          && getInstrumentationScopeInfo().equals(that.getInstrumentationScopeInfo())
          && getName().equals(that.getName())
          && getDescription().equals(that.getDescription())
          && getUnit().equals(that.getUnit())
          && getType().equals(that.getType())
          && getData().equals(that.getData());
    }
    return false;
  }

  /**
   * Returns a hash code value for the delegate {@link MetricData}.
   *
   * @return a hash code value for the delegate {@link MetricData}.
   */
  @Override
  public int hashCode() {
    int code = 1;
    code *= 1000003;
    code ^= getResource().hashCode();
    code *= 1000003;
    code ^= getInstrumentationScopeInfo().hashCode();
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

  /** Returns a string representation of the delegate {@link MetricData}. */
  @Override
  public String toString() {
    return "DelegatingMetricData{"
        + "resource="
        + getResource()
        + ", instrumentationScopeInfo="
        + getInstrumentationScopeInfo()
        + ", name="
        + getName()
        + ", description="
        + getDescription()
        + ", unit="
        + getUnit()
        + ", type="
        + getType()
        + ", data="
        + getData()
        + "}";
  }
}
