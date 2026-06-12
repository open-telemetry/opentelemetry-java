/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"max_scale", "max_size", "record_min_max"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class Base2ExponentialBucketHistogramAggregationModel {

  /**
   * Configure the max scale factor. If omitted or null, 20 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("max_scale")
  @JsonPropertyDescription("Configure the max scale factor.\nIf omitted or null, 20 is used.\n")
  private Integer maxScale;

  /**
   * Configure the maximum number of buckets in each of the positive and negative ranges, not
   * counting the special zero bucket. If omitted or null, 160 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("max_size")
  @JsonPropertyDescription(
      "Configure the maximum number of buckets in each of the positive and negative ranges, not counting the special zero bucket.\nIf omitted or null, 160 is used.\n")
  private Integer maxSize;

  /**
   * Configure whether or not to record min and max. If omitted or null, true is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("record_min_max")
  @JsonPropertyDescription(
      "Configure whether or not to record min and max.\nIf omitted or null, true is used.\n")
  private Boolean recordMinMax;

  /** Configure the max scale factor. If omitted or null, 20 is used. */
  @JsonProperty("max_scale")
  @Nullable
  public Integer getMaxScale() {
    return maxScale;
  }

  public Base2ExponentialBucketHistogramAggregationModel withMaxScale(Integer maxScale) {
    this.maxScale = maxScale;
    return this;
  }

  /**
   * Configure the maximum number of buckets in each of the positive and negative ranges, not
   * counting the special zero bucket. If omitted or null, 160 is used.
   */
  @JsonProperty("max_size")
  @Nullable
  public Integer getMaxSize() {
    return maxSize;
  }

  public Base2ExponentialBucketHistogramAggregationModel withMaxSize(Integer maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  /** Configure whether or not to record min and max. If omitted or null, true is used. */
  @JsonProperty("record_min_max")
  @Nullable
  public Boolean getRecordMinMax() {
    return recordMinMax;
  }

  public Base2ExponentialBucketHistogramAggregationModel withRecordMinMax(Boolean recordMinMax) {
    this.recordMinMax = recordMinMax;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Base2ExponentialBucketHistogramAggregationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("maxScale");
    sb.append('=');
    sb.append(((this.maxScale == null) ? "<null>" : this.maxScale));
    sb.append(',');
    sb.append("maxSize");
    sb.append('=');
    sb.append(((this.maxSize == null) ? "<null>" : this.maxSize));
    sb.append(',');
    sb.append("recordMinMax");
    sb.append('=');
    sb.append(((this.recordMinMax == null) ? "<null>" : this.recordMinMax));
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
    result = ((result * 31) + ((this.maxScale == null) ? 0 : this.maxScale.hashCode()));
    result = ((result * 31) + ((this.recordMinMax == null) ? 0 : this.recordMinMax.hashCode()));
    result = ((result * 31) + ((this.maxSize == null) ? 0 : this.maxSize.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof Base2ExponentialBucketHistogramAggregationModel) == false) {
      return false;
    }
    Base2ExponentialBucketHistogramAggregationModel rhs =
        ((Base2ExponentialBucketHistogramAggregationModel) other);
    return ((((this.maxScale == rhs.maxScale)
                || ((this.maxScale != null) && this.maxScale.equals(rhs.maxScale)))
            && ((this.recordMinMax == rhs.recordMinMax)
                || ((this.recordMinMax != null) && this.recordMinMax.equals(rhs.recordMinMax))))
        && ((this.maxSize == rhs.maxSize)
            || ((this.maxSize != null) && this.maxSize.equals(rhs.maxSize))));
  }
}
