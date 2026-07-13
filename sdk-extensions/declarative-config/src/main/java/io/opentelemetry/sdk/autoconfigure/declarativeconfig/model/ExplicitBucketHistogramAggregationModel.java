/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"boundaries", "record_min_max"})
@Generated("jsonschema2pojo")
public class ExplicitBucketHistogramAggregationModel {

  @Nullable private List<Double> boundaries;
  @Nullable private Boolean recordMinMax;

  /**
   * Configure bucket boundaries.
   *
   * <p>If omitted, [0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500, 5000, 7500, 10000] is
   * used.
   */
  @JsonProperty("boundaries")
  @Nullable
  public List<Double> getBoundaries() {
    return boundaries;
  }

  @JsonProperty("boundaries")
  public ExplicitBucketHistogramAggregationModel withBoundaries(List<Double> boundaries) {
    this.boundaries = boundaries;
    return this;
  }

  /**
   * Configure record min and max.
   *
   * <p>If omitted or null, true is used.
   */
  @JsonProperty("record_min_max")
  @Nullable
  public Boolean getRecordMinMax() {
    return recordMinMax;
  }

  @JsonProperty("record_min_max")
  public ExplicitBucketHistogramAggregationModel withRecordMinMax(Boolean recordMinMax) {
    this.recordMinMax = recordMinMax;
    return this;
  }

  @Override
  public String toString() {
    return "ExplicitBucketHistogramAggregationModel{"
        + "boundaries="
        + boundaries
        + ", recordMinMax="
        + recordMinMax
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.boundaries == null) ? 0 : this.boundaries.hashCode();
    h *= 1000003;
    h ^= (this.recordMinMax == null) ? 0 : this.recordMinMax.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExplicitBucketHistogramAggregationModel) {
      ExplicitBucketHistogramAggregationModel that = (ExplicitBucketHistogramAggregationModel) o;
      return (this.boundaries == null
              ? that.boundaries == null
              : this.boundaries.equals(that.boundaries))
          && (this.recordMinMax == null
              ? that.recordMinMax == null
              : this.recordMinMax.equals(that.recordMinMax));
    }
    return false;
  }
}
