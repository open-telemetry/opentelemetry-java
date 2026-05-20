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
  "attribute_value_length_limit",
  "attribute_count_limit",
  "event_count_limit",
  "link_count_limit",
  "event_attribute_count_limit",
  "link_attribute_count_limit"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class SpanLimitsModel {

  /**
   * Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit.
   * Value must be non-negative. If omitted or null, there is no limit.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("attribute_value_length_limit")
  @JsonPropertyDescription(
      "Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit. \nValue must be non-negative.\nIf omitted or null, there is no limit.\n")
  private Integer attributeValueLengthLimit;

  /**
   * Configure max attribute count. Overrides .attribute_limits.attribute_count_limit. Value must be
   * non-negative. If omitted or null, 128 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("attribute_count_limit")
  @JsonPropertyDescription(
      "Configure max attribute count. Overrides .attribute_limits.attribute_count_limit. \nValue must be non-negative.\nIf omitted or null, 128 is used.\n")
  private Integer attributeCountLimit;

  /**
   * Configure max span event count. Value must be non-negative. If omitted or null, 128 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("event_count_limit")
  @JsonPropertyDescription(
      "Configure max span event count. \nValue must be non-negative.\nIf omitted or null, 128 is used.\n")
  private Integer eventCountLimit;

  /**
   * Configure max span link count. Value must be non-negative. If omitted or null, 128 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("link_count_limit")
  @JsonPropertyDescription(
      "Configure max span link count. \nValue must be non-negative.\nIf omitted or null, 128 is used.\n")
  private Integer linkCountLimit;

  /**
   * Configure max attributes per span event. Value must be non-negative. If omitted or null, 128 is
   * used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("event_attribute_count_limit")
  @JsonPropertyDescription(
      "Configure max attributes per span event. \nValue must be non-negative.\nIf omitted or null, 128 is used.\n")
  private Integer eventAttributeCountLimit;

  /**
   * Configure max attributes per span link. Value must be non-negative. If omitted or null, 128 is
   * used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("link_attribute_count_limit")
  @JsonPropertyDescription(
      "Configure max attributes per span link. \nValue must be non-negative.\nIf omitted or null, 128 is used.\n")
  private Integer linkAttributeCountLimit;

  /**
   * Configure max attribute value size. Overrides .attribute_limits.attribute_value_length_limit.
   * Value must be non-negative. If omitted or null, there is no limit.
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
   * Configure max attribute count. Overrides .attribute_limits.attribute_count_limit. Value must be
   * non-negative. If omitted or null, 128 is used.
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
   * Configure max span event count. Value must be non-negative. If omitted or null, 128 is used.
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

  /** Configure max span link count. Value must be non-negative. If omitted or null, 128 is used. */
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
   * Configure max attributes per span event. Value must be non-negative. If omitted or null, 128 is
   * used.
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
   * Configure max attributes per span link. Value must be non-negative. If omitted or null, 128 is
   * used.
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
    StringBuilder sb = new StringBuilder();
    sb.append(SpanLimitsModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("attributeValueLengthLimit");
    sb.append('=');
    sb.append(
        ((this.attributeValueLengthLimit == null) ? "<null>" : this.attributeValueLengthLimit));
    sb.append(',');
    sb.append("attributeCountLimit");
    sb.append('=');
    sb.append(((this.attributeCountLimit == null) ? "<null>" : this.attributeCountLimit));
    sb.append(',');
    sb.append("eventCountLimit");
    sb.append('=');
    sb.append(((this.eventCountLimit == null) ? "<null>" : this.eventCountLimit));
    sb.append(',');
    sb.append("linkCountLimit");
    sb.append('=');
    sb.append(((this.linkCountLimit == null) ? "<null>" : this.linkCountLimit));
    sb.append(',');
    sb.append("eventAttributeCountLimit");
    sb.append('=');
    sb.append(((this.eventAttributeCountLimit == null) ? "<null>" : this.eventAttributeCountLimit));
    sb.append(',');
    sb.append("linkAttributeCountLimit");
    sb.append('=');
    sb.append(((this.linkAttributeCountLimit == null) ? "<null>" : this.linkAttributeCountLimit));
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
    result =
        ((result * 31)
            + ((this.linkAttributeCountLimit == null)
                ? 0
                : this.linkAttributeCountLimit.hashCode()));
    result =
        ((result * 31)
            + ((this.eventAttributeCountLimit == null)
                ? 0
                : this.eventAttributeCountLimit.hashCode()));
    result =
        ((result * 31)
            + ((this.attributeValueLengthLimit == null)
                ? 0
                : this.attributeValueLengthLimit.hashCode()));
    result =
        ((result * 31)
            + ((this.attributeCountLimit == null) ? 0 : this.attributeCountLimit.hashCode()));
    result = ((result * 31) + ((this.linkCountLimit == null) ? 0 : this.linkCountLimit.hashCode()));
    result =
        ((result * 31) + ((this.eventCountLimit == null) ? 0 : this.eventCountLimit.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof SpanLimitsModel) == false) {
      return false;
    }
    SpanLimitsModel rhs = ((SpanLimitsModel) other);
    return (((((((this.linkAttributeCountLimit == rhs.linkAttributeCountLimit)
                            || ((this.linkAttributeCountLimit != null)
                                && this.linkAttributeCountLimit.equals(
                                    rhs.linkAttributeCountLimit)))
                        && ((this.eventAttributeCountLimit == rhs.eventAttributeCountLimit)
                            || ((this.eventAttributeCountLimit != null)
                                && this.eventAttributeCountLimit.equals(
                                    rhs.eventAttributeCountLimit))))
                    && ((this.attributeValueLengthLimit == rhs.attributeValueLengthLimit)
                        || ((this.attributeValueLengthLimit != null)
                            && this.attributeValueLengthLimit.equals(
                                rhs.attributeValueLengthLimit))))
                && ((this.attributeCountLimit == rhs.attributeCountLimit)
                    || ((this.attributeCountLimit != null)
                        && this.attributeCountLimit.equals(rhs.attributeCountLimit))))
            && ((this.linkCountLimit == rhs.linkCountLimit)
                || ((this.linkCountLimit != null)
                    && this.linkCountLimit.equals(rhs.linkCountLimit))))
        && ((this.eventCountLimit == rhs.eventCountLimit)
            || ((this.eventCountLimit != null)
                && this.eventCountLimit.equals(rhs.eventCountLimit))));
  }
}
