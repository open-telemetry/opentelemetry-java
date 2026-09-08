/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel.ATTRIBUTE_COUNT_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel.ATTRIBUTE_VALUE_LENGTH_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel.EVENT_ATTRIBUTE_COUNT_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel.EVENT_COUNT_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel.LINK_ATTRIBUTE_COUNT_LIMIT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel.LINK_COUNT_LIMIT;

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
@JsonPropertyOrder({
  ATTRIBUTE_VALUE_LENGTH_LIMIT,
  ATTRIBUTE_COUNT_LIMIT,
  EVENT_COUNT_LIMIT,
  LINK_COUNT_LIMIT,
  EVENT_ATTRIBUTE_COUNT_LIMIT,
  LINK_ATTRIBUTE_COUNT_LIMIT
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class SpanLimitsModel {

  static final String ATTRIBUTE_VALUE_LENGTH_LIMIT = "attribute_value_length_limit";
  static final String ATTRIBUTE_COUNT_LIMIT = "attribute_count_limit";
  static final String EVENT_COUNT_LIMIT = "event_count_limit";
  static final String LINK_COUNT_LIMIT = "link_count_limit";
  static final String EVENT_ATTRIBUTE_COUNT_LIMIT = "event_attribute_count_limit";
  static final String LINK_ATTRIBUTE_COUNT_LIMIT = "link_attribute_count_limit";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ATTRIBUTE_VALUE_LENGTH_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(ATTRIBUTE_COUNT_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(EVENT_COUNT_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(LINK_COUNT_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(EVENT_ATTRIBUTE_COUNT_LIMIT, Integer.class);
    STABLE_PROPERTIES.put(LINK_ATTRIBUTE_COUNT_LIMIT, Integer.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Integer attributeValueLengthLimit;
  @Nullable private Integer attributeCountLimit;
  @Nullable private Integer eventCountLimit;
  @Nullable private Integer linkCountLimit;
  @Nullable private Integer eventAttributeCountLimit;
  @Nullable private Integer linkAttributeCountLimit;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit.
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
  public SpanLimitsModel setAttributeValueLengthLimit(Integer attributeValueLengthLimit) {
    this.attributeValueLengthLimit = attributeValueLengthLimit;
    return this;
  }

  /**
   * Configure max attribute count. Overrides .attribute_limits.attribute_count_limit.
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
  public SpanLimitsModel setAttributeCountLimit(Integer attributeCountLimit) {
    this.attributeCountLimit = attributeCountLimit;
    return this;
  }

  /**
   * Configure max span event count.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 128 is used.
   */
  @JsonProperty(EVENT_COUNT_LIMIT)
  @Nullable
  public Integer getEventCountLimit() {
    if (eventCountLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          EVENT_COUNT_LIMIT, extensionProperties, Integer.class);
    }
    return eventCountLimit;
  }

  @JsonProperty(EVENT_COUNT_LIMIT)
  public SpanLimitsModel setEventCountLimit(Integer eventCountLimit) {
    this.eventCountLimit = eventCountLimit;
    return this;
  }

  /**
   * Configure max span link count.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 128 is used.
   */
  @JsonProperty(LINK_COUNT_LIMIT)
  @Nullable
  public Integer getLinkCountLimit() {
    if (linkCountLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          LINK_COUNT_LIMIT, extensionProperties, Integer.class);
    }
    return linkCountLimit;
  }

  @JsonProperty(LINK_COUNT_LIMIT)
  public SpanLimitsModel setLinkCountLimit(Integer linkCountLimit) {
    this.linkCountLimit = linkCountLimit;
    return this;
  }

  /**
   * Configure max attributes per span event.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 128 is used.
   */
  @JsonProperty(EVENT_ATTRIBUTE_COUNT_LIMIT)
  @Nullable
  public Integer getEventAttributeCountLimit() {
    if (eventAttributeCountLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          EVENT_ATTRIBUTE_COUNT_LIMIT, extensionProperties, Integer.class);
    }
    return eventAttributeCountLimit;
  }

  @JsonProperty(EVENT_ATTRIBUTE_COUNT_LIMIT)
  public SpanLimitsModel setEventAttributeCountLimit(Integer eventAttributeCountLimit) {
    this.eventAttributeCountLimit = eventAttributeCountLimit;
    return this;
  }

  /**
   * Configure max attributes per span link.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 128 is used.
   */
  @JsonProperty(LINK_ATTRIBUTE_COUNT_LIMIT)
  @Nullable
  public Integer getLinkAttributeCountLimit() {
    if (linkAttributeCountLimit == null) {
      return ExtensionPropertyUtil.getGraduated(
          LINK_ATTRIBUTE_COUNT_LIMIT, extensionProperties, Integer.class);
    }
    return linkAttributeCountLimit;
  }

  @JsonProperty(LINK_ATTRIBUTE_COUNT_LIMIT)
  public SpanLimitsModel setLinkAttributeCountLimit(Integer linkAttributeCountLimit) {
    this.linkAttributeCountLimit = linkAttributeCountLimit;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public SpanLimitsModel setExtensionProperty(String name, @Nullable Object value) {
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
    return "SpanLimitsModel{"
        + "attributeValueLengthLimit="
        + attributeValueLengthLimit
        + ", attributeCountLimit="
        + attributeCountLimit
        + ", eventCountLimit="
        + eventCountLimit
        + ", linkCountLimit="
        + linkCountLimit
        + ", eventAttributeCountLimit="
        + eventAttributeCountLimit
        + ", linkAttributeCountLimit="
        + linkAttributeCountLimit
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^=
        (this.getAttributeValueLengthLimit() == null)
            ? 0
            : this.getAttributeValueLengthLimit().hashCode();
    h *= 1000003;
    h ^= (this.getAttributeCountLimit() == null) ? 0 : this.getAttributeCountLimit().hashCode();
    h *= 1000003;
    h ^= (this.getEventCountLimit() == null) ? 0 : this.getEventCountLimit().hashCode();
    h *= 1000003;
    h ^= (this.getLinkCountLimit() == null) ? 0 : this.getLinkCountLimit().hashCode();
    h *= 1000003;
    h ^=
        (this.getEventAttributeCountLimit() == null)
            ? 0
            : this.getEventAttributeCountLimit().hashCode();
    h *= 1000003;
    h ^=
        (this.getLinkAttributeCountLimit() == null)
            ? 0
            : this.getLinkAttributeCountLimit().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpanLimitsModel) {
      SpanLimitsModel that = (SpanLimitsModel) o;
      return (this.getAttributeValueLengthLimit() == null
              ? that.getAttributeValueLengthLimit() == null
              : this.getAttributeValueLengthLimit().equals(that.getAttributeValueLengthLimit()))
          && (this.getAttributeCountLimit() == null
              ? that.getAttributeCountLimit() == null
              : this.getAttributeCountLimit().equals(that.getAttributeCountLimit()))
          && (this.getEventCountLimit() == null
              ? that.getEventCountLimit() == null
              : this.getEventCountLimit().equals(that.getEventCountLimit()))
          && (this.getLinkCountLimit() == null
              ? that.getLinkCountLimit() == null
              : this.getLinkCountLimit().equals(that.getLinkCountLimit()))
          && (this.getEventAttributeCountLimit() == null
              ? that.getEventAttributeCountLimit() == null
              : this.getEventAttributeCountLimit().equals(that.getEventAttributeCountLimit()))
          && (this.getLinkAttributeCountLimit() == null
              ? that.getLinkAttributeCountLimit() == null
              : this.getLinkAttributeCountLimit().equals(that.getLinkAttributeCountLimit()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
