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
@Generated("jsonschema2pojo")
public class AggregationModel {

  @JsonProperty("default")
  @Nullable
  private DefaultAggregationModel _default;

  @JsonProperty("drop")
  @Nullable
  private DropAggregationModel drop;

  @JsonProperty("explicit_bucket_histogram")
  @Nullable
  private ExplicitBucketHistogramAggregationModel explicitBucketHistogram;

  @JsonProperty("base2_exponential_bucket_histogram")
  @Nullable
  private Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram;

  @JsonProperty("last_value")
  @Nullable
  private LastValueAggregationModel lastValue;

  @JsonProperty("sum")
  @Nullable
  private SumAggregationModel sum;

  @JsonProperty("default")
  @Nullable
  public DefaultAggregationModel getDefault() {
    return _default;
  }

  public AggregationModel withDefault(DefaultAggregationModel _default) {
    this._default = _default;
    return this;
  }

  @JsonProperty("drop")
  @Nullable
  public DropAggregationModel getDrop() {
    return drop;
  }

  public AggregationModel withDrop(DropAggregationModel drop) {
    this.drop = drop;
    return this;
  }

  @JsonProperty("explicit_bucket_histogram")
  @Nullable
  public ExplicitBucketHistogramAggregationModel getExplicitBucketHistogram() {
    return explicitBucketHistogram;
  }

  public AggregationModel withExplicitBucketHistogram(
      ExplicitBucketHistogramAggregationModel explicitBucketHistogram) {
    this.explicitBucketHistogram = explicitBucketHistogram;
    return this;
  }

  @JsonProperty("base2_exponential_bucket_histogram")
  @Nullable
  public Base2ExponentialBucketHistogramAggregationModel getBase2ExponentialBucketHistogram() {
    return base2ExponentialBucketHistogram;
  }

  public AggregationModel withBase2ExponentialBucketHistogram(
      Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram) {
    this.base2ExponentialBucketHistogram = base2ExponentialBucketHistogram;
    return this;
  }

  @JsonProperty("last_value")
  @Nullable
  public LastValueAggregationModel getLastValue() {
    return lastValue;
  }

  public AggregationModel withLastValue(LastValueAggregationModel lastValue) {
    this.lastValue = lastValue;
    return this;
  }

  @JsonProperty("sum")
  @Nullable
  public SumAggregationModel getSum() {
    return sum;
  }

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
