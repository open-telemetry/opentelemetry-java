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
  "name",
  "description",
  "aggregation",
  "aggregation_cardinality_limit",
  "attribute_keys"
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ViewStreamModel {

  @Nullable private String name;
  @Nullable private String description;
  @Nullable private AggregationModel aggregation;
  @Nullable private Integer aggregationCardinalityLimit;
  @Nullable private IncludeExcludeModel attributeKeys;

  /**
   * Configure metric name of the resulting stream(s).
   *
   * <p>If omitted or null, the instrument's original name is used.
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public ViewStreamModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Configure metric description of the resulting stream(s).
   *
   * <p>If omitted or null, the instrument's origin description is used.
   */
  @JsonProperty("description")
  @Nullable
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public ViewStreamModel withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Configure aggregation of the resulting stream(s).
   *
   * <p>If omitted, default is used.
   */
  @JsonProperty("aggregation")
  @Nullable
  public AggregationModel getAggregation() {
    return aggregation;
  }

  @JsonProperty("aggregation")
  public ViewStreamModel withAggregation(AggregationModel aggregation) {
    this.aggregation = aggregation;
    return this;
  }

  /**
   * Configure the aggregation cardinality limit.
   *
   * <p>If omitted or null, the metric reader's default cardinality limit is used.
   */
  @JsonProperty("aggregation_cardinality_limit")
  @Nullable
  public Integer getAggregationCardinalityLimit() {
    return aggregationCardinalityLimit;
  }

  @JsonProperty("aggregation_cardinality_limit")
  public ViewStreamModel withAggregationCardinalityLimit(Integer aggregationCardinalityLimit) {
    this.aggregationCardinalityLimit = aggregationCardinalityLimit;
    return this;
  }

  /**
   * Configure attribute keys retained in the resulting stream(s).
   *
   * <p>If omitted, all attribute keys are retained.
   */
  @JsonProperty("attribute_keys")
  @Nullable
  public IncludeExcludeModel getAttributeKeys() {
    return attributeKeys;
  }

  @JsonProperty("attribute_keys")
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
