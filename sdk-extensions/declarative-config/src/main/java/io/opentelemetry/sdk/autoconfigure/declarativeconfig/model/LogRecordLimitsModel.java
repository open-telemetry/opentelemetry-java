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
@JsonPropertyOrder({"attribute_value_length_limit", "attribute_count_limit"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class LogRecordLimitsModel {

  @Nullable private Integer attributeValueLengthLimit;
  @Nullable private Integer attributeCountLimit;

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

  @JsonProperty("attribute_value_length_limit")
  public LogRecordLimitsModel withAttributeValueLengthLimit(Integer attributeValueLengthLimit) {
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

  @JsonProperty("attribute_count_limit")
  public LogRecordLimitsModel withAttributeCountLimit(Integer attributeCountLimit) {
    this.attributeCountLimit = attributeCountLimit;
    return this;
  }

  @Override
  public String toString() {
    return "LogRecordLimitsModel{"
        + "attributeValueLengthLimit="
        + attributeValueLengthLimit
        + ", attributeCountLimit="
        + attributeCountLimit
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.attributeValueLengthLimit == null) ? 0 : this.attributeValueLengthLimit.hashCode();
    h *= 1000003;
    h ^= (this.attributeCountLimit == null) ? 0 : this.attributeCountLimit.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof LogRecordLimitsModel) {
      LogRecordLimitsModel that = (LogRecordLimitsModel) o;
      return (this.attributeValueLengthLimit == null
              ? that.attributeValueLengthLimit == null
              : this.attributeValueLengthLimit.equals(that.attributeValueLengthLimit))
          && (this.attributeCountLimit == null
              ? that.attributeCountLimit == null
              : this.attributeCountLimit.equals(that.attributeCountLimit));
    }
    return false;
  }
}
