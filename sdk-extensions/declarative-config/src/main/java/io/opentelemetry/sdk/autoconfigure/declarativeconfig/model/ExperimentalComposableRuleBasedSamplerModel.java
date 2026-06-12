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
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"rules"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalComposableRuleBasedSamplerModel {

  /**
   * The rules for the sampler, matched in order. Each rule can have multiple match conditions. All
   * conditions must match for the rule to match. If no conditions are specified, the rule matches
   * all spans that reach it. If no rules match, the span is not sampled. If omitted, no span is
   * sampled.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("rules")
  @JsonPropertyDescription(
      "The rules for the sampler, matched in order.\nEach rule can have multiple match conditions. All conditions must match for the rule to match.\nIf no conditions are specified, the rule matches all spans that reach it.\nIf no rules match, the span is not sampled.\nIf omitted, no span is sampled.\n")
  private List<ExperimentalComposableRuleBasedSamplerRuleModel> rules;

  /**
   * The rules for the sampler, matched in order. Each rule can have multiple match conditions. All
   * conditions must match for the rule to match. If no conditions are specified, the rule matches
   * all spans that reach it. If no rules match, the span is not sampled. If omitted, no span is
   * sampled.
   */
  @JsonProperty("rules")
  @Nullable
  public List<ExperimentalComposableRuleBasedSamplerRuleModel> getRules() {
    return rules;
  }

  public ExperimentalComposableRuleBasedSamplerModel withRules(
      List<ExperimentalComposableRuleBasedSamplerRuleModel> rules) {
    this.rules = rules;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalComposableRuleBasedSamplerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("rules");
    sb.append('=');
    sb.append(((this.rules == null) ? "<null>" : this.rules));
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
    result = ((result * 31) + ((this.rules == null) ? 0 : this.rules.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalComposableRuleBasedSamplerModel) == false) {
      return false;
    }
    ExperimentalComposableRuleBasedSamplerModel rhs =
        ((ExperimentalComposableRuleBasedSamplerModel) other);
    return ((this.rules == rhs.rules) || ((this.rules != null) && this.rules.equals(rhs.rules)));
  }
}
