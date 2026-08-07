/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel.AGGREGATION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel.AGGREGATION_CARDINALITY_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel.ATTRIBUTE_KEYS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel.DESCRIPTION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel.NAME;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({NAME, DESCRIPTION, AGGREGATION, AGGREGATION_CARDINALITY_LIMIT, ATTRIBUTE_KEYS})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ViewStreamModel {

  static final String NAME = "name";
  static final String DESCRIPTION = "description";
  static final String AGGREGATION = "aggregation";
  static final String AGGREGATION_CARDINALITY_LIMIT = "aggregation_cardinality_limit";
  static final String ATTRIBUTE_KEYS = "attribute_keys";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(NAME, String.class);
    STABLE_PROPERTIES.put(DESCRIPTION, String.class);
    STABLE_PROPERTIES.put(AGGREGATION, AggregationModel.class);
    STABLE_PROPERTIES.put(AGGREGATION_CARDINALITY_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(ATTRIBUTE_KEYS, IncludeExcludeModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String name;
  @Nullable private String description;
  @Nullable private AggregationModel aggregation;
  @Nullable private Integer aggregationCardinalityLimit;
  @Nullable private IncludeExcludeModel attributeKeys;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure metric name of the resulting stream(s).
   *
   * <p>If omitted or null, the instrument's original name is used.
   */
  @JsonProperty(NAME)
  @Nullable
  public String getName() {
    if (name == null) {
      return ExtensionPropertyUtil.getGraduated(NAME, extensionProperties, String.class);
    }
    return name;
  }

  @JsonProperty(NAME)
  public ViewStreamModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Configure metric description of the resulting stream(s).
   *
   * <p>If omitted or null, the instrument's origin description is used.
   */
  @JsonProperty(DESCRIPTION)
  @Nullable
  public String getDescription() {
    if (description == null) {
      return ExtensionPropertyUtil.getGraduated(DESCRIPTION, extensionProperties, String.class);
    }
    return description;
  }

  @JsonProperty(DESCRIPTION)
  public ViewStreamModel withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Configure aggregation of the resulting stream(s).
   *
   * <p>If omitted, default is used.
   */
  @JsonProperty(AGGREGATION)
  @Nullable
  public AggregationModel getAggregation() {
    if (aggregation == null) {
      return ExtensionPropertyUtil.getGraduated(
          AGGREGATION, extensionProperties, AggregationModel.class);
    }
    return aggregation;
  }

  @JsonProperty(AGGREGATION)
  public ViewStreamModel withAggregation(AggregationModel aggregation) {
    this.aggregation = aggregation;
    return this;
  }

  /**
   * Configure the aggregation cardinality limit.
   *
   * <p>If omitted or null, the metric reader's default cardinality limit is used.
   */
  @JsonProperty(AGGREGATION_CARDINALITY_LIMIT)
  @Nullable
  public Integer getAggregationCardinalityLimit() {
    if (aggregationCardinalityLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          AGGREGATION_CARDINALITY_LIMIT, extensionProperties, Integer.class);
    }
    return aggregationCardinalityLimit;
  }

  @JsonProperty(AGGREGATION_CARDINALITY_LIMIT)
  public ViewStreamModel withAggregationCardinalityLimit(Integer aggregationCardinalityLimit) {
    this.aggregationCardinalityLimit = aggregationCardinalityLimit;
    return this;
  }

  /**
   * Configure attribute keys retained in the resulting stream(s).
   *
   * <p>If omitted, all attribute keys are retained.
   */
  @JsonProperty(ATTRIBUTE_KEYS)
  @Nullable
  public IncludeExcludeModel getAttributeKeys() {
    if (attributeKeys == null) {
      return ExtensionPropertyUtil.getGraduated(
          ATTRIBUTE_KEYS, extensionProperties, IncludeExcludeModel.class);
    }
    return attributeKeys;
  }

  @JsonProperty(ATTRIBUTE_KEYS)
  public ViewStreamModel withAttributeKeys(IncludeExcludeModel attributeKeys) {
    this.attributeKeys = attributeKeys;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ViewStreamModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
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
        + ", extensionProperties="
        + extensionProperties
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
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
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
              : this.attributeKeys.equals(that.attributeKeys))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
