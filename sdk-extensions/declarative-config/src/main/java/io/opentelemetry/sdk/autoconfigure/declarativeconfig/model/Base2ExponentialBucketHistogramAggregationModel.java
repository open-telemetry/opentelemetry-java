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
public class Base2ExponentialBucketHistogramAggregationModel {

  /** Configure the max scale factor. If omitted or null, 20 is used. */
  @JsonProperty("max_scale")
  @JsonPropertyDescription("Configure the max scale factor.\nIf omitted or null, 20 is used.\n")
  @Nullable
  private Integer maxScale;

  /**
   * Configure the maximum number of buckets in each of the positive and negative ranges, not
   * counting the special zero bucket. If omitted or null, 160 is used.
   */
  @JsonProperty("max_size")
  @JsonPropertyDescription(
      "Configure the maximum number of buckets in each of the positive and negative ranges, not counting the special zero bucket.\nIf omitted or null, 160 is used.\n")
  @Nullable
  private Integer maxSize;

  /** Configure whether or not to record min and max. If omitted or null, true is used. */
  @JsonProperty("record_min_max")
  @JsonPropertyDescription(
      "Configure whether or not to record min and max.\nIf omitted or null, true is used.\n")
  @Nullable
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
    return "Base2ExponentialBucketHistogramAggregationModel{"
        + "maxScale="
        + maxScale
        + ", maxSize="
        + maxSize
        + ", recordMinMax="
        + recordMinMax
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.maxScale == null) ? 0 : this.maxScale.hashCode();
    h *= 1000003;
    h ^= (this.maxSize == null) ? 0 : this.maxSize.hashCode();
    h *= 1000003;
    h ^= (this.recordMinMax == null) ? 0 : this.recordMinMax.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Base2ExponentialBucketHistogramAggregationModel) {
      Base2ExponentialBucketHistogramAggregationModel that =
          (Base2ExponentialBucketHistogramAggregationModel) o;
      return (this.maxScale == null ? that.maxScale == null : this.maxScale.equals(that.maxScale))
          && (this.maxSize == null ? that.maxSize == null : this.maxSize.equals(that.maxSize))
          && (this.recordMinMax == null
              ? that.recordMinMax == null
              : this.recordMinMax.equals(that.recordMinMax));
    }
    return false;
  }
}
