/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"boundaries", "record_min_max"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExplicitBucketHistogramAggregationModel {

  /**
   * Configure bucket boundaries. If omitted, [0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500,
   * 5000, 7500, 10000] is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("boundaries")
  @JsonPropertyDescription(
      "Configure bucket boundaries.\nIf omitted, [0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500, 5000, 7500, 10000] is used.\n")
  private List<Double> boundaries;

  /**
   * Configure record min and max. If omitted or null, true is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("record_min_max")
  @JsonPropertyDescription("Configure record min and max.\nIf omitted or null, true is used.\n")
  private Boolean recordMinMax;

  /**
   * Configure bucket boundaries. If omitted, [0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500,
   * 5000, 7500, 10000] is used.
   */
  @JsonProperty("boundaries")
  @Nullable
  public List<Double> getBoundaries() {
    return boundaries;
  }

  public ExplicitBucketHistogramAggregationModel withBoundaries(List<Double> boundaries) {
    this.boundaries = boundaries;
    return this;
  }

  /** Configure record min and max. If omitted or null, true is used. */
  @JsonProperty("record_min_max")
  @Nullable
  public Boolean getRecordMinMax() {
    return recordMinMax;
  }

  public ExplicitBucketHistogramAggregationModel withRecordMinMax(Boolean recordMinMax) {
    this.recordMinMax = recordMinMax;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExplicitBucketHistogramAggregationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("boundaries");
    sb.append('=');
    sb.append(((this.boundaries == null) ? "<null>" : this.boundaries));
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
    result = ((result * 31) + ((this.boundaries == null) ? 0 : this.boundaries.hashCode()));
    result = ((result * 31) + ((this.recordMinMax == null) ? 0 : this.recordMinMax.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExplicitBucketHistogramAggregationModel) == false) {
      return false;
    }
    ExplicitBucketHistogramAggregationModel rhs = ((ExplicitBucketHistogramAggregationModel) other);
    return (((this.boundaries == rhs.boundaries)
            || ((this.boundaries != null) && this.boundaries.equals(rhs.boundaries)))
        && ((this.recordMinMax == rhs.recordMinMax)
            || ((this.recordMinMax != null) && this.recordMinMax.equals(rhs.recordMinMax))));
  }
}
