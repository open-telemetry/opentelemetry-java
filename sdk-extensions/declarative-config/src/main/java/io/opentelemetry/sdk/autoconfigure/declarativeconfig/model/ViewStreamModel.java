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
public class ViewStreamModel {

  /**
   * Configure metric name of the resulting stream(s). If omitted or null, the instrument's original
   * name is used.
   */
  @JsonProperty("name")
  @JsonPropertyDescription(
      "Configure metric name of the resulting stream(s).\nIf omitted or null, the instrument's original name is used.\n")
  @Nullable
  private String name;

  /**
   * Configure metric description of the resulting stream(s). If omitted or null, the instrument's
   * origin description is used.
   */
  @JsonProperty("description")
  @JsonPropertyDescription(
      "Configure metric description of the resulting stream(s).\nIf omitted or null, the instrument's origin description is used.\n")
  @Nullable
  private String description;

  @JsonProperty("aggregation")
  @Nullable
  private AggregationModel aggregation;

  /**
   * Configure the aggregation cardinality limit. If omitted or null, the metric reader's default
   * cardinality limit is used.
   */
  @JsonProperty("aggregation_cardinality_limit")
  @JsonPropertyDescription(
      "Configure the aggregation cardinality limit.\nIf omitted or null, the metric reader's default cardinality limit is used.\n")
  @Nullable
  private Integer aggregationCardinalityLimit;

  @JsonProperty("attribute_keys")
  @Nullable
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
    return "ViewStreamModel{"
        + "name="
        + name
        + ", description="
        + description
        + ", aggregation="
        + aggregation
        + ", aggregationCardinalityLimit="
        + aggregationCardinalityLimit
        + ", attributeKeys="
        + attributeKeys
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.name == null) ? 0 : this.name.hashCode();
    h *= 1000003;
    h ^= (this.description == null) ? 0 : this.description.hashCode();
    h *= 1000003;
    h ^= (this.aggregation == null) ? 0 : this.aggregation.hashCode();
    h *= 1000003;
    h ^=
        (this.aggregationCardinalityLimit == null)
            ? 0
            : this.aggregationCardinalityLimit.hashCode();
    h *= 1000003;
    h ^= (this.attributeKeys == null) ? 0 : this.attributeKeys.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ViewStreamModel) {
      ViewStreamModel that = (ViewStreamModel) o;
      return (this.name == null ? that.name == null : this.name.equals(that.name))
          && (this.description == null
              ? that.description == null
              : this.description.equals(that.description))
          && (this.aggregation == null
              ? that.aggregation == null
              : this.aggregation.equals(that.aggregation))
          && (this.aggregationCardinalityLimit == null
              ? that.aggregationCardinalityLimit == null
              : this.aggregationCardinalityLimit.equals(that.aggregationCardinalityLimit))
          && (this.attributeKeys == null
              ? that.attributeKeys == null
              : this.attributeKeys.equals(that.attributeKeys));
    }
    return false;
  }
}
