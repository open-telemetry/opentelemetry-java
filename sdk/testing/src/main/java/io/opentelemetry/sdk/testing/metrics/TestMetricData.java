/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link
 * io.opentelemetry.sdk.metrics.data.MetricData} class.
 */
@Immutable
@AutoValue
public abstract class TestMetricData implements MetricData {
  public static Builder builder() {
    return new AutoValue_TestMetricData.Builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty());
  }

  /** A builder for {@link TestMetricData}. */
  @AutoValue.Builder
  public abstract static class Builder {
    abstract TestMetricData autoBuild();

    /**
     * Builds and returns a new {@link MetricData} instance from the data in {@code this}.
     *
     * @return a new {@link MetricData} instance.
     */
    public TestMetricData build() {
      return autoBuild();
    }

    /**
     * Sets the resource of the metric.
     *
     * @param resource the resource of the metric.
     * @return this.
     */
    public abstract Builder setResource(Resource resource);

    /**
     * Sets the name of the metric.
     *
     * @param name the name of the metric.
     * @return this.
     */
    public abstract Builder setName(String name);

    /**
     * Sets the description of the metric.
     *
     * @param description the description of the metric.
     * @return this.
     */
    public abstract Builder setDescription(String description);

    /**
     * Sets the unit of the metric.
     *
     * @param unit the unit of the metric.
     * @return this.
     */
    public abstract Builder setUnit(String unit);

    /**
     * Sets the type of the metric.
     *
     * @param type the type of the metric.
     * @return this.
     */
    public abstract Builder setType(MetricDataType type);

    /**
     * Sets the data of the metric.
     *
     * @param data the data of the metric.
     * @return this.
     */
    public abstract Builder setData(Data<?> data);

    /**
     * Sets the Instrumentation scope of the metric.
     *
     * @param instrumentationScopeInfo the instrumentation scope of the tracer which created this
     *     metric.
     * @return this.
     */
    public abstract Builder setInstrumentationScopeInfo(
        InstrumentationScopeInfo instrumentationScopeInfo);
  }
}
