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
public class ExperimentalComposableRuleBasedSamplerModel {

  /**
   * The rules for the sampler, matched in order. Each rule can have multiple match conditions. All
   * conditions must match for the rule to match. If no conditions are specified, the rule matches
   * all spans that reach it. If no rules match, the span is not sampled. If omitted, no span is
   * sampled.
   */
  @JsonProperty("rules")
  @JsonPropertyDescription(
      "The rules for the sampler, matched in order.\nEach rule can have multiple match conditions. All conditions must match for the rule to match.\nIf no conditions are specified, the rule matches all spans that reach it.\nIf no rules match, the span is not sampled.\nIf omitted, no span is sampled.\n")
  @Nullable
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
    return "ExperimentalComposableRuleBasedSamplerModel{" + "rules=" + rules + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.rules == null) ? 0 : this.rules.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalComposableRuleBasedSamplerModel) {
      ExperimentalComposableRuleBasedSamplerModel that =
          (ExperimentalComposableRuleBasedSamplerModel) o;
      return (this.rules == null ? that.rules == null : this.rules.equals(that.rules));
    }
    return false;
  }
}
