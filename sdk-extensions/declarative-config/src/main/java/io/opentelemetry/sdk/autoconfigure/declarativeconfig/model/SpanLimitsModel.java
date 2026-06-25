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
  "attribute_value_length_limit",
  "attribute_count_limit",
  "event_count_limit",
  "link_count_limit",
  "event_attribute_count_limit",
  "link_attribute_count_limit"
})
@Generated("jsonschema2pojo")
public class SpanLimitsModel {

  @JsonProperty("attribute_value_length_limit")
  @Nullable
  private Integer attributeValueLengthLimit;

  @JsonProperty("attribute_count_limit")
  @Nullable
  private Integer attributeCountLimit;

  @JsonProperty("event_count_limit")
  @Nullable
  private Integer eventCountLimit;

  @JsonProperty("link_count_limit")
  @Nullable
  private Integer linkCountLimit;

  @JsonProperty("event_attribute_count_limit")
  @Nullable
  private Integer eventAttributeCountLimit;

  @JsonProperty("link_attribute_count_limit")
  @Nullable
  private Integer linkAttributeCountLimit;

  /**
   * Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, there is no limit.
   */
  @JsonProperty("attribute_value_length_limit")
  @Nullable
  public Integer getAttributeValueLengthLimit() {
    return attributeValueLengthLimit;
  }

  public SpanLimitsModel withAttributeValueLengthLimit(Integer attributeValueLengthLimit) {
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
  @JsonProperty("attribute_count_limit")
  @Nullable
  public Integer getAttributeCountLimit() {
    return attributeCountLimit;
  }

  public SpanLimitsModel withAttributeCountLimit(Integer attributeCountLimit) {
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
  @JsonProperty("event_count_limit")
  @Nullable
  public Integer getEventCountLimit() {
    return eventCountLimit;
  }

  public SpanLimitsModel withEventCountLimit(Integer eventCountLimit) {
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
  @JsonProperty("link_count_limit")
  @Nullable
  public Integer getLinkCountLimit() {
    return linkCountLimit;
  }

  public SpanLimitsModel withLinkCountLimit(Integer linkCountLimit) {
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
  @JsonProperty("event_attribute_count_limit")
  @Nullable
  public Integer getEventAttributeCountLimit() {
    return eventAttributeCountLimit;
  }

  public SpanLimitsModel withEventAttributeCountLimit(Integer eventAttributeCountLimit) {
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
  @JsonProperty("link_attribute_count_limit")
  @Nullable
  public Integer getLinkAttributeCountLimit() {
    return linkAttributeCountLimit;
  }

  public SpanLimitsModel withLinkAttributeCountLimit(Integer linkAttributeCountLimit) {
    this.linkAttributeCountLimit = linkAttributeCountLimit;
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
    h ^= (this.eventCountLimit == null) ? 0 : this.eventCountLimit.hashCode();
    h *= 1000003;
    h ^= (this.linkCountLimit == null) ? 0 : this.linkCountLimit.hashCode();
    h *= 1000003;
    h ^= (this.eventAttributeCountLimit == null) ? 0 : this.eventAttributeCountLimit.hashCode();
    h *= 1000003;
    h ^= (this.linkAttributeCountLimit == null) ? 0 : this.linkAttributeCountLimit.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpanLimitsModel) {
      SpanLimitsModel that = (SpanLimitsModel) o;
      return (this.attributeValueLengthLimit == null
              ? that.attributeValueLengthLimit == null
              : this.attributeValueLengthLimit.equals(that.attributeValueLengthLimit))
          && (this.attributeCountLimit == null
              ? that.attributeCountLimit == null
              : this.attributeCountLimit.equals(that.attributeCountLimit))
          && (this.eventCountLimit == null
              ? that.eventCountLimit == null
              : this.eventCountLimit.equals(that.eventCountLimit))
          && (this.linkCountLimit == null
              ? that.linkCountLimit == null
              : this.linkCountLimit.equals(that.linkCountLimit))
          && (this.eventAttributeCountLimit == null
              ? that.eventAttributeCountLimit == null
              : this.eventAttributeCountLimit.equals(that.eventAttributeCountLimit))
          && (this.linkAttributeCountLimit == null
              ? that.linkAttributeCountLimit == null
              : this.linkAttributeCountLimit.equals(that.linkAttributeCountLimit));
    }
    return false;
  }
}
