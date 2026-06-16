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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class AggregationModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("default")
  private DefaultAggregationModel _default;

  /** (Can be null) */
  @Nullable
  @JsonProperty("drop")
  private DropAggregationModel drop;

  /** (Can be null) */
  @Nullable
  @JsonProperty("explicit_bucket_histogram")
  private ExplicitBucketHistogramAggregationModel explicitBucketHistogram;

  /** (Can be null) */
  @Nullable
  @JsonProperty("base2_exponential_bucket_histogram")
  private Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram;

  /** (Can be null) */
  @Nullable
  @JsonProperty("last_value")
  private LastValueAggregationModel lastValue;

  /** (Can be null) */
  @Nullable
  @JsonProperty("sum")
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
    StringBuilder sb = new StringBuilder();
    sb.append(AggregationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("_default");
    sb.append('=');
    sb.append(((this._default == null) ? "<null>" : this._default));
    sb.append(',');
    sb.append("drop");
    sb.append('=');
    sb.append(((this.drop == null) ? "<null>" : this.drop));
    sb.append(',');
    sb.append("explicitBucketHistogram");
    sb.append('=');
    sb.append(((this.explicitBucketHistogram == null) ? "<null>" : this.explicitBucketHistogram));
    sb.append(',');
    sb.append("base2ExponentialBucketHistogram");
    sb.append('=');
    sb.append(
        ((this.base2ExponentialBucketHistogram == null)
            ? "<null>"
            : this.base2ExponentialBucketHistogram));
    sb.append(',');
    sb.append("lastValue");
    sb.append('=');
    sb.append(((this.lastValue == null) ? "<null>" : this.lastValue));
    sb.append(',');
    sb.append("sum");
    sb.append('=');
    sb.append(((this.sum == null) ? "<null>" : this.sum));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.drop == null) ? 0 : this.drop.hashCode()));
    result =
        ((result * 31)
            + ((this.explicitBucketHistogram == null)
                ? 0
                : this.explicitBucketHistogram.hashCode()));
    result = ((result * 31) + ((this._default == null) ? 0 : this._default.hashCode()));
    result = ((result * 31) + ((this.lastValue == null) ? 0 : this.lastValue.hashCode()));
    result = ((result * 31) + ((this.sum == null) ? 0 : this.sum.hashCode()));
    result =
        ((result * 31)
            + ((this.base2ExponentialBucketHistogram == null)
                ? 0
                : this.base2ExponentialBucketHistogram.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof AggregationModel) == false) {
      return false;
    }
    AggregationModel rhs = ((AggregationModel) other);
    return (((((((this.drop == rhs.drop) || ((this.drop != null) && this.drop.equals(rhs.drop)))
                        && ((this.explicitBucketHistogram == rhs.explicitBucketHistogram)
                            || ((this.explicitBucketHistogram != null)
                                && this.explicitBucketHistogram.equals(
                                    rhs.explicitBucketHistogram))))
                    && ((this._default == rhs._default)
                        || ((this._default != null) && this._default.equals(rhs._default))))
                && ((this.lastValue == rhs.lastValue)
                    || ((this.lastValue != null) && this.lastValue.equals(rhs.lastValue))))
            && ((this.sum == rhs.sum) || ((this.sum != null) && this.sum.equals(rhs.sum))))
        && ((this.base2ExponentialBucketHistogram == rhs.base2ExponentialBucketHistogram)
            || ((this.base2ExponentialBucketHistogram != null)
                && this.base2ExponentialBucketHistogram.equals(
                    rhs.base2ExponentialBucketHistogram))));
  }
}
