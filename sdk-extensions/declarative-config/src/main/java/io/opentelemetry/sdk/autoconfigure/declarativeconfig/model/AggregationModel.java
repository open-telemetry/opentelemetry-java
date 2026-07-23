/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "default",
  "drop",
  "explicit_bucket_histogram",
  "base2_exponential_bucket_histogram",
  "last_value",
  "sum"
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class AggregationModel {

  @Nullable private DefaultAggregationModel _default;
  @Nullable private DropAggregationModel drop;
  @Nullable private ExplicitBucketHistogramAggregationModel explicitBucketHistogram;
  @Nullable private Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram;
  @Nullable private LastValueAggregationModel lastValue;
  @Nullable private SumAggregationModel sum;

  /**
   * Configures the stream to use the instrument kind to select an aggregation and advisory
   * parameters to influence aggregation configuration parameters. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#default-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("default")
  @Nullable
  public DefaultAggregationModel getDefault() {
    return _default;
  }

  @JsonProperty("default")
  public AggregationModel withDefault(DefaultAggregationModel _default) {
    this._default = _default;
    return this;
  }

  /**
   * Configures the stream to ignore/drop all instrument measurements. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#drop-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("drop")
  @Nullable
  public DropAggregationModel getDrop() {
    return drop;
  }

  @JsonProperty("drop")
  public AggregationModel withDrop(DropAggregationModel drop) {
    this.drop = drop;
    return this;
  }

  /**
   * Configures the stream to collect data for the histogram metric point using a set of explicit
   * boundary values for histogram bucketing. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#explicit-bucket-histogram-aggregation
   * for details
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("explicit_bucket_histogram")
  @Nullable
  public ExplicitBucketHistogramAggregationModel getExplicitBucketHistogram() {
    return explicitBucketHistogram;
  }

  @JsonProperty("explicit_bucket_histogram")
  public AggregationModel withExplicitBucketHistogram(
      ExplicitBucketHistogramAggregationModel explicitBucketHistogram) {
    this.explicitBucketHistogram = explicitBucketHistogram;
    return this;
  }

  /**
   * Configures the stream to collect data for the exponential histogram metric point, which uses a
   * base-2 exponential formula to determine bucket boundaries and an integer scale parameter to
   * control resolution. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#base2-exponential-bucket-histogram-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("base2_exponential_bucket_histogram")
  @Nullable
  public Base2ExponentialBucketHistogramAggregationModel getBase2ExponentialBucketHistogram() {
    return base2ExponentialBucketHistogram;
  }

  @JsonProperty("base2_exponential_bucket_histogram")
  public AggregationModel withBase2ExponentialBucketHistogram(
      Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram) {
    this.base2ExponentialBucketHistogram = base2ExponentialBucketHistogram;
    return this;
  }

  /**
   * Configures the stream to collect data using the last measurement. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#last-value-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("last_value")
  @Nullable
  public LastValueAggregationModel getLastValue() {
    return lastValue;
  }

  @JsonProperty("last_value")
  public AggregationModel withLastValue(LastValueAggregationModel lastValue) {
    this.lastValue = lastValue;
    return this;
  }

  /**
   * Configures the stream to collect the arithmetic sum of measurement values. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#sum-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("sum")
  @Nullable
  public SumAggregationModel getSum() {
    return sum;
  }

  @JsonProperty("sum")
  public AggregationModel withSum(SumAggregationModel sum) {
    this.sum = sum;
    return this;
  }

  @Override
  public String toString() {
    return "AggregationModel{"
        + "_default="
        + _default
        + ", drop="
        + drop
        + ", explicitBucketHistogram="
        + explicitBucketHistogram
        + ", base2ExponentialBucketHistogram="
        + base2ExponentialBucketHistogram
        + ", lastValue="
        + lastValue
        + ", sum="
        + sum
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this._default == null) ? 0 : this._default.hashCode();
    h *= 1000003;
    h ^= (this.drop == null) ? 0 : this.drop.hashCode();
    h *= 1000003;
    h ^= (this.explicitBucketHistogram == null) ? 0 : this.explicitBucketHistogram.hashCode();
    h *= 1000003;
    h ^=
        (this.base2ExponentialBucketHistogram == null)
            ? 0
            : this.base2ExponentialBucketHistogram.hashCode();
    h *= 1000003;
    h ^= (this.lastValue == null) ? 0 : this.lastValue.hashCode();
    h *= 1000003;
    h ^= (this.sum == null) ? 0 : this.sum.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AggregationModel) {
      AggregationModel that = (AggregationModel) o;
      return (this._default == null ? that._default == null : this._default.equals(that._default))
          && (this.drop == null ? that.drop == null : this.drop.equals(that.drop))
          && (this.explicitBucketHistogram == null
              ? that.explicitBucketHistogram == null
              : this.explicitBucketHistogram.equals(that.explicitBucketHistogram))
          && (this.base2ExponentialBucketHistogram == null
              ? that.base2ExponentialBucketHistogram == null
              : this.base2ExponentialBucketHistogram.equals(that.base2ExponentialBucketHistogram))
          && (this.lastValue == null
              ? that.lastValue == null
              : this.lastValue.equals(that.lastValue))
          && (this.sum == null ? that.sum == null : this.sum.equals(that.sum));
    }
    return false;
  }
}
