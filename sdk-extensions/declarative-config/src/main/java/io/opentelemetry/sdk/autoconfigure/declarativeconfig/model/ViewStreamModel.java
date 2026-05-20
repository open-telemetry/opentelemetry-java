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
@JsonPropertyOrder({
  "name",
  "description",
  "aggregation",
  "aggregation_cardinality_limit",
  "attribute_keys"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ViewStreamModel {

  /**
   * Configure metric name of the resulting stream(s). If omitted or null, the instrument's original
   * name is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("name")
  @JsonPropertyDescription(
      "Configure metric name of the resulting stream(s).\nIf omitted or null, the instrument's original name is used.\n")
  private String name;

  /**
   * Configure metric description of the resulting stream(s). If omitted or null, the instrument's
   * origin description is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("description")
  @JsonPropertyDescription(
      "Configure metric description of the resulting stream(s).\nIf omitted or null, the instrument's origin description is used.\n")
  private String description;

  /** (Can be null) */
  @Nullable
  @JsonProperty("aggregation")
  private AggregationModel aggregation;

  /**
   * Configure the aggregation cardinality limit. If omitted or null, the metric reader's default
   * cardinality limit is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("aggregation_cardinality_limit")
  @JsonPropertyDescription(
      "Configure the aggregation cardinality limit.\nIf omitted or null, the metric reader's default cardinality limit is used.\n")
  private Integer aggregationCardinalityLimit;

  /** (Can be null) */
  @Nullable
  @JsonProperty("attribute_keys")
  private IncludeExcludeModel attributeKeys;

  /**
   * Configure metric name of the resulting stream(s). If omitted or null, the instrument's original
   * name is used.
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

  public ViewStreamModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Configure metric description of the resulting stream(s). If omitted or null, the instrument's
   * origin description is used.
   */
  @JsonProperty("description")
  @Nullable
  public String getDescription() {
    return description;
  }

  public ViewStreamModel withDescription(String description) {
    this.description = description;
    return this;
  }

  @JsonProperty("aggregation")
  @Nullable
  public AggregationModel getAggregation() {
    return aggregation;
  }

  public ViewStreamModel withAggregation(AggregationModel aggregation) {
    this.aggregation = aggregation;
    return this;
  }

  /**
   * Configure the aggregation cardinality limit. If omitted or null, the metric reader's default
   * cardinality limit is used.
   */
  @JsonProperty("aggregation_cardinality_limit")
  @Nullable
  public Integer getAggregationCardinalityLimit() {
    return aggregationCardinalityLimit;
  }

  public ViewStreamModel withAggregationCardinalityLimit(Integer aggregationCardinalityLimit) {
    this.aggregationCardinalityLimit = aggregationCardinalityLimit;
    return this;
  }

  @JsonProperty("attribute_keys")
  @Nullable
  public IncludeExcludeModel getAttributeKeys() {
    return attributeKeys;
  }

  public ViewStreamModel withAttributeKeys(IncludeExcludeModel attributeKeys) {
    this.attributeKeys = attributeKeys;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ViewStreamModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("name");
    sb.append('=');
    sb.append(((this.name == null) ? "<null>" : this.name));
    sb.append(',');
    sb.append("description");
    sb.append('=');
    sb.append(((this.description == null) ? "<null>" : this.description));
    sb.append(',');
    sb.append("aggregation");
    sb.append('=');
    sb.append(((this.aggregation == null) ? "<null>" : this.aggregation));
    sb.append(',');
    sb.append("aggregationCardinalityLimit");
    sb.append('=');
    sb.append(
        ((this.aggregationCardinalityLimit == null) ? "<null>" : this.aggregationCardinalityLimit));
    sb.append(',');
    sb.append("attributeKeys");
    sb.append('=');
    sb.append(((this.attributeKeys == null) ? "<null>" : this.attributeKeys));
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
    result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
    result = ((result * 31) + ((this.description == null) ? 0 : this.description.hashCode()));
    result = ((result * 31) + ((this.aggregation == null) ? 0 : this.aggregation.hashCode()));
    result = ((result * 31) + ((this.attributeKeys == null) ? 0 : this.attributeKeys.hashCode()));
    result =
        ((result * 31)
            + ((this.aggregationCardinalityLimit == null)
                ? 0
                : this.aggregationCardinalityLimit.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ViewStreamModel) == false) {
      return false;
    }
    ViewStreamModel rhs = ((ViewStreamModel) other);
    return ((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                    && ((this.description == rhs.description)
                        || ((this.description != null)
                            && this.description.equals(rhs.description))))
                && ((this.aggregation == rhs.aggregation)
                    || ((this.aggregation != null) && this.aggregation.equals(rhs.aggregation))))
            && ((this.attributeKeys == rhs.attributeKeys)
                || ((this.attributeKeys != null) && this.attributeKeys.equals(rhs.attributeKeys))))
        && ((this.aggregationCardinalityLimit == rhs.aggregationCardinalityLimit)
            || ((this.aggregationCardinalityLimit != null)
                && this.aggregationCardinalityLimit.equals(rhs.aggregationCardinalityLimit))));
  }
}
