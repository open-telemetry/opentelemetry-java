/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"key", "values"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel {

  /**
   * The attribute key to match against. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("key")
  @JsonPropertyDescription(
      "The attribute key to match against.\nProperty is required and must be non-null.\n")
  @Nonnull
  private String key;

  /**
   * The attribute values to match against. If the attribute's value matches any of these, it
   * matches. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("values")
  @JsonPropertyDescription(
      "The attribute values to match against. If the attribute's value matches any of these, it matches.\nProperty is required and must be non-null.\n")
  @Nonnull
  private List<String> values;

  /**
   * The attribute key to match against. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("key")
  @Nullable
  public String getKey() {
    return key;
  }

  public ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel withKey(String key) {
    this.key = key;
    return this;
  }

  /**
   * The attribute values to match against. If the attribute's value matches any of these, it
   * matches. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("values")
  @Nullable
  public List<String> getValues() {
    return values;
  }

  public ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel withValues(
      List<String> values) {
    this.values = values;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("key");
    sb.append('=');
    sb.append(((this.key == null) ? "<null>" : this.key));
    sb.append(',');
    sb.append("values");
    sb.append('=');
    sb.append(((this.values == null) ? "<null>" : this.values));
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
    result = ((result * 31) + ((this.key == null) ? 0 : this.key.hashCode()));
    result = ((result * 31) + ((this.values == null) ? 0 : this.values.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel)
        == false) {
      return false;
    }
    ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel rhs =
        ((ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel) other);
    return (((this.key == rhs.key) || ((this.key != null) && this.key.equals(rhs.key)))
        && ((this.values == rhs.values)
            || ((this.values != null) && this.values.equals(rhs.values))));
  }
}
