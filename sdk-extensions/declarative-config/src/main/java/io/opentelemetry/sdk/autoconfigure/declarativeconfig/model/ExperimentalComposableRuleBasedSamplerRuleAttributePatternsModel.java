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
@JsonPropertyOrder({"key", "included", "excluded"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel {

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
   * Configure list of value patterns to include. Values are evaluated to match as follows: * If the
   * value exactly matches. * If the value matches the wildcard pattern, where '?' matches any
   * single character and '*' matches any number of characters including none. If omitted, all
   * values are included.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("included")
  @JsonPropertyDescription(
      "Configure list of value patterns to include.\nValues are evaluated to match as follows:\n * If the value exactly matches.\n * If the value matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.\nIf omitted, all values are included.\n")
  private List<String> included;

  /**
   * Configure list of value patterns to exclude. Applies after .included (i.e. excluded has higher
   * priority than included). Values are evaluated to match as follows: * If the value exactly
   * matches. * If the value matches the wildcard pattern, where '?' matches any single character
   * and '*' matches any number of characters including none. If omitted, .included attributes are
   * included.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("excluded")
  @JsonPropertyDescription(
      "Configure list of value patterns to exclude. Applies after .included (i.e. excluded has higher priority than included).\nValues are evaluated to match as follows:\n * If the value exactly matches.\n * If the value matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.\nIf omitted, .included attributes are included.\n")
  private List<String> excluded;

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

  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel withKey(String key) {
    this.key = key;
    return this;
  }

  /**
   * Configure list of value patterns to include. Values are evaluated to match as follows: * If the
   * value exactly matches. * If the value matches the wildcard pattern, where '?' matches any
   * single character and '*' matches any number of characters including none. If omitted, all
   * values are included.
   */
  @JsonProperty("included")
  @Nullable
  public List<String> getIncluded() {
    return included;
  }

  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel withIncluded(
      List<String> included) {
    this.included = included;
    return this;
  }

  /**
   * Configure list of value patterns to exclude. Applies after .included (i.e. excluded has higher
   * priority than included). Values are evaluated to match as follows: * If the value exactly
   * matches. * If the value matches the wildcard pattern, where '?' matches any single character
   * and '*' matches any number of characters including none. If omitted, .included attributes are
   * included.
   */
  @JsonProperty("excluded")
  @Nullable
  public List<String> getExcluded() {
    return excluded;
  }

  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel withExcluded(
      List<String> excluded) {
    this.excluded = excluded;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("key");
    sb.append('=');
    sb.append(((this.key == null) ? "<null>" : this.key));
    sb.append(',');
    sb.append("included");
    sb.append('=');
    sb.append(((this.included == null) ? "<null>" : this.included));
    sb.append(',');
    sb.append("excluded");
    sb.append('=');
    sb.append(((this.excluded == null) ? "<null>" : this.excluded));
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
    result = ((result * 31) + ((this.excluded == null) ? 0 : this.excluded.hashCode()));
    result = ((result * 31) + ((this.included == null) ? 0 : this.included.hashCode()));
    result = ((result * 31) + ((this.key == null) ? 0 : this.key.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel)
        == false) {
      return false;
    }
    ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel rhs =
        ((ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel) other);
    return ((((this.excluded == rhs.excluded)
                || ((this.excluded != null) && this.excluded.equals(rhs.excluded)))
            && ((this.included == rhs.included)
                || ((this.included != null) && this.included.equals(rhs.included))))
        && ((this.key == rhs.key) || ((this.key != null) && this.key.equals(rhs.key))));
  }
}
