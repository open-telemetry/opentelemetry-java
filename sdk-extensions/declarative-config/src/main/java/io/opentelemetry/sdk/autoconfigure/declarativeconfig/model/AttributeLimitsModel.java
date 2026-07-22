/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeLimitsModel.ATTRIBUTE_COUNT_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeLimitsModel.ATTRIBUTE_VALUE_LENGTH_LIMIT;

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
@JsonPropertyOrder({ATTRIBUTE_VALUE_LENGTH_LIMIT, ATTRIBUTE_COUNT_LIMIT})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class AttributeLimitsModel {

  static final String ATTRIBUTE_VALUE_LENGTH_LIMIT = "attribute_value_length_limit";
  static final String ATTRIBUTE_COUNT_LIMIT = "attribute_count_limit";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ATTRIBUTE_VALUE_LENGTH_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(ATTRIBUTE_COUNT_LIMIT, Integer.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Integer attributeValueLengthLimit;
  @Nullable private Integer attributeCountLimit;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure max attribute value size.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, there is no limit.
   */
  @JsonProperty(ATTRIBUTE_VALUE_LENGTH_LIMIT)
  @Nullable
  public Integer getAttributeValueLengthLimit() {
    if (attributeValueLengthLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          ATTRIBUTE_VALUE_LENGTH_LIMIT, extensionProperties, Integer.class);
    }
    return attributeValueLengthLimit;
  }

  @JsonProperty(ATTRIBUTE_VALUE_LENGTH_LIMIT)
  public AttributeLimitsModel withAttributeValueLengthLimit(Integer attributeValueLengthLimit) {
    this.attributeValueLengthLimit = attributeValueLengthLimit;
    return this;
  }

  /**
   * Configure max attribute count.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 128 is used.
   */
  @JsonProperty(ATTRIBUTE_COUNT_LIMIT)
  @Nullable
  public Integer getAttributeCountLimit() {
    if (attributeCountLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          ATTRIBUTE_COUNT_LIMIT, extensionProperties, Integer.class);
    }
    return attributeCountLimit;
  }

  @JsonProperty(ATTRIBUTE_COUNT_LIMIT)
  public AttributeLimitsModel withAttributeCountLimit(Integer attributeCountLimit) {
    this.attributeCountLimit = attributeCountLimit;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public AttributeLimitsModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "AttributeLimitsModel{"
        + "attributeValueLengthLimit="
        + attributeValueLengthLimit
        + ", attributeCountLimit="
        + attributeCountLimit
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.attributeValueLengthLimit == null) ? 0 : this.attributeValueLengthLimit.hashCode();
    h *= 1000003;
    h ^= (this.attributeCountLimit == null) ? 0 : this.attributeCountLimit.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AttributeLimitsModel) {
      AttributeLimitsModel that = (AttributeLimitsModel) o;
      return (this.attributeValueLengthLimit == null
              ? that.attributeValueLengthLimit == null
              : this.attributeValueLengthLimit.equals(that.attributeValueLengthLimit))
          && (this.attributeCountLimit == null
              ? that.attributeCountLimit == null
              : this.attributeCountLimit.equals(that.attributeCountLimit))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
