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
@JsonPropertyOrder({"attribute_value_length_limit", "attribute_count_limit"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class AttributeLimitsModel {

  /**
   * Configure max attribute value size. Value must be non-negative. If omitted or null, there is no
   * limit.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("attribute_value_length_limit")
  @JsonPropertyDescription(
      "Configure max attribute value size. \nValue must be non-negative.\nIf omitted or null, there is no limit.\n")
  private Integer attributeValueLengthLimit;

  /**
   * Configure max attribute count. Value must be non-negative. If omitted or null, 128 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("attribute_count_limit")
  @JsonPropertyDescription(
      "Configure max attribute count. \nValue must be non-negative.\nIf omitted or null, 128 is used.\n")
  private Integer attributeCountLimit;

  /**
   * Configure max attribute value size. Value must be non-negative. If omitted or null, there is no
   * limit.
   */
  @JsonProperty("attribute_value_length_limit")
  @Nullable
  public Integer getAttributeValueLengthLimit() {
    return attributeValueLengthLimit;
  }

  public AttributeLimitsModel withAttributeValueLengthLimit(Integer attributeValueLengthLimit) {
    this.attributeValueLengthLimit = attributeValueLengthLimit;
    return this;
  }

  /** Configure max attribute count. Value must be non-negative. If omitted or null, 128 is used. */
  @JsonProperty("attribute_count_limit")
  @Nullable
  public Integer getAttributeCountLimit() {
    return attributeCountLimit;
  }

  public AttributeLimitsModel withAttributeCountLimit(Integer attributeCountLimit) {
    this.attributeCountLimit = attributeCountLimit;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(AttributeLimitsModel.class.getName())
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
            + ((this.attributeValueLengthLimit == null)
                ? 0
                : this.attributeValueLengthLimit.hashCode()));
    result =
        ((result * 31)
            + ((this.attributeCountLimit == null) ? 0 : this.attributeCountLimit.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof AttributeLimitsModel) == false) {
      return false;
    }
    AttributeLimitsModel rhs = ((AttributeLimitsModel) other);
    return (((this.attributeValueLengthLimit == rhs.attributeValueLengthLimit)
            || ((this.attributeValueLengthLimit != null)
                && this.attributeValueLengthLimit.equals(rhs.attributeValueLengthLimit)))
        && ((this.attributeCountLimit == rhs.attributeCountLimit)
            || ((this.attributeCountLimit != null)
                && this.attributeCountLimit.equals(rhs.attributeCountLimit))));
  }
}
