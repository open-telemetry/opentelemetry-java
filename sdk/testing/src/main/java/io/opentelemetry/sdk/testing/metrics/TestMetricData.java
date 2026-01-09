/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link
 * io.opentelemetry.sdk.metrics.data.MetricData} class.
 *
 * @since 1.50.0
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

    public TestMetricData build() {
      return autoBuild();
    }

    public abstract Builder setResource(Resource resource);

    public abstract Builder setName(String name);

    public abstract Builder setDescription(String description);

    public abstract Builder setUnit(String unit);

    // Make setType package-private to restrict direct access
    abstract Builder setType(MetricDataType type);

    // Keep the generic setData method for internal use
    abstract Builder setData(Data<?> data);

    // Add specific setData overloads for each metric data type
    public Builder setExponentialHistogramData(ExponentialHistogramData data) {
      return setType(MetricDataType.EXPONENTIAL_HISTOGRAM).setData(data);
    }

    public Builder setHistogramData(HistogramData data) {
      return setType(MetricDataType.HISTOGRAM).setData(data);
    }

    public Builder setLongSumData(SumData<LongPointData> data) {
      return setType(MetricDataType.LONG_SUM).setData(data);
    }

    public Builder setDoubleSumData(SumData<DoublePointData> data) {
      return setType(MetricDataType.DOUBLE_SUM).setData(data);
    }

    public Builder setDoubleGaugeData(GaugeData<DoublePointData> data) {
      return setType(MetricDataType.DOUBLE_GAUGE).setData(data);
    }

    public Builder setLongGaugeData(GaugeData<LongPointData> data) {
      return setType(MetricDataType.LONG_GAUGE).setData(data);
    }

    public Builder setSummaryData(SummaryData data) {
      return setType(MetricDataType.SUMMARY).setData(data);
    }

    public abstract Builder setInstrumentationScopeInfo(
        InstrumentationScopeInfo instrumentationScopeInfo);
  }
}
